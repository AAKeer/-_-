package com.example.dailyhot.source;

import com.example.dailyhot.model.HotItem;
import com.example.dailyhot.model.HotSourceResult;
import com.example.dailyhot.service.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BilibiliHotSource implements HotSource {

    private static final String RANKING_URL = "https://api.bilibili.com/x/web-interface/ranking/v2?rid=%d&type=all";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0 Safari/537.36";
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Map<String, Integer> CATEGORY_RIDS = Map.of(
            "all", 0,
            "technology", 188,
            "game", 4,
            "food", 211
    );
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "all", "Bilibili 总榜",
            "technology", "Bilibili 科技榜",
            "game", "Bilibili 游戏榜",
            "food", "Bilibili 美食榜"
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String source() {
        return "bilibili";
    }

    @Override
    public String displayName() {
        return "Bilibili 热榜";
    }

    @Override
    public HotSourceResult fetch() {
        return fetch(Map.of());
    }

    @Override
    public HotSourceResult fetch(Map<String, String> params) {
        String category = normalizeCategory(params.get("category"));
        int rid = CATEGORY_RIDS.get(category);

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(RANKING_URL.formatted(rid)))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", "https://www.bilibili.com/v/popular/rank/all")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return failed(category, "Bilibili 接口返回 HTTP " + response.statusCode());
            }

            List<HotItem> items = parseItems(response.body(), category);
            if (items.isEmpty()) {
                return failed(category, "未能从 Bilibili 排行榜接口解析到真实数据");
            }

            return HotSourceResult.builder()
                    .source(source())
                    .displayName(CATEGORY_NAMES.get(category))
                    .success(true)
                    .items(items)
                    .build();
        } catch (IOException ex) {
            return failed(category, "请求 Bilibili 排行榜失败：" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failed(category, "请求 Bilibili 排行榜被中断");
        }
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "all";
        }
        String normalized = category.trim().toLowerCase();
        return CATEGORY_RIDS.containsKey(normalized) ? normalized : "all";
    }

    private List<HotItem> parseItems(String body, String category) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        if (root.path("code").asInt(-1) != 0) {
            return List.of();
        }

        JsonNode list = root.path("data").path("list");
        if (!list.isArray()) {
            return List.of();
        }

        List<HotItem> items = new ArrayList<>();
        OffsetDateTime fetchedAt = OffsetDateTime.now(ZONE);

        for (JsonNode node : list) {
            String title = node.path("title").asText("");
            String bvid = node.path("bvid").asText("");
            if (title.isBlank() || bvid.isBlank()) {
                continue;
            }

            String heat = null;
            JsonNode view = node.path("stat").path("view");
            if (view.isNumber()) {
                heat = String.valueOf(view.asLong());
            }

            items.add(HotItem.builder()
                    .title(title)
                    .url("https://www.bilibili.com/video/" + URLEncoder.encode(bvid, StandardCharsets.UTF_8))
                    .source(source())
                    .rank(items.size() + 1)
                    .heat(heat)
                    .fetchedAt(fetchedAt)
                    .build());

            if (items.size() >= 20) {
                break;
            }
        }

        return items;
    }

    private HotSourceResult failed(String category, String message) {
        return HotSourceResult.builder()
                .source(source())
                .displayName(CATEGORY_NAMES.getOrDefault(category, displayName()))
                .success(false)
                .errorMessage(message)
                .build();
    }
}
