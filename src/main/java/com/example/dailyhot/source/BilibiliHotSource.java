package com.example.dailyhot.source;

import com.example.dailyhot.model.HotItem;
import com.example.dailyhot.model.HotSourceResult;
import com.example.dailyhot.service.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class BilibiliHotSource implements HotSource {

    private static final String URL = "https://api.bilibili.com/x/web-interface/popular?ps=20&pn=1";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0 Safari/537.36";
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

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
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", "https://www.bilibili.com/v/popular/all")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return failed("Bilibili 接口返回 HTTP " + response.statusCode());
            }

            List<HotItem> items = parseItems(response.body());
            if (items.isEmpty()) {
                return failed("未能从 Bilibili 接口解析到热门视频数据");
            }

            return HotSourceResult.builder()
                    .source(source())
                    .displayName(displayName())
                    .success(true)
                    .items(items)
                    .build();
        } catch (IOException ex) {
            return failed("请求 Bilibili 热门失败：" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failed("请求 Bilibili 热门被中断");
        }
    }

    private List<HotItem> parseItems(String body) throws IOException {
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
                    .url("https://www.bilibili.com/video/" + bvid)
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

    private HotSourceResult failed(String message) {
        return HotSourceResult.builder()
                .source(source())
                .displayName(displayName())
                .success(false)
                .errorMessage(message)
                .build();
    }
}
