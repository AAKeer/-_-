package com.example.dailyhot.source;

import com.example.dailyhot.model.HotItem;
import com.example.dailyhot.model.HotSourceResult;
import com.example.dailyhot.service.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.Map;

@Component
public class SteamHotSource implements HotSource {

    private static final String TOPSELLERS_URL = "https://store.steampowered.com/search/?filter=topsellers";
    private static final String MOSTPLAYED_URL = "https://api.steampowered.com/ISteamChartsService/GetGamesByConcurrentPlayers/v1/";
    private static final String APP_DETAILS_URL = "https://store.steampowered.com/api/appdetails?appids=%d&filters=basic";
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
        return "steam";
    }

    @Override
    public String displayName() {
        return "Steam 游戏社区";
    }

    @Override
    public HotSourceResult fetch() {
        return fetch(Map.of());
    }

    @Override
    public HotSourceResult fetch(Map<String, String> params) {
        String type = normalizeType(params.get("type"));
        return type.equals("mostplayed") ? fetchMostPlayed() : fetchTopSellers();
    }

    private HotSourceResult fetchTopSellers() {
        try {
            Document document = Jsoup.connect(TOPSELLERS_URL)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            List<HotItem> items = new ArrayList<>();
            OffsetDateTime fetchedAt = OffsetDateTime.now(ZONE);

            for (Element row : document.select("a.search_result_row[href]")) {
                String title = row.selectFirst(".title") == null ? "" : row.selectFirst(".title").text().trim();
                String url = row.absUrl("href");
                if (title.isBlank() || url.isBlank()) {
                    continue;
                }

                items.add(HotItem.builder()
                        .title(title)
                        .url(url)
                        .source(source())
                        .rank(items.size() + 1)
                        .heat(null)
                        .fetchedAt(fetchedAt)
                        .build());

                if (items.size() >= 20) {
                    break;
                }
            }

            if (items.isEmpty()) {
                return failed("Steam 热销榜", "未能从 Steam 官方热销页面解析到真实数据");
            }

            return success("Steam 热销榜", items);
        } catch (IOException ex) {
            return failed("Steam 热销榜", "请求 Steam 热销榜失败：" + ex.getMessage());
        }
    }

    private HotSourceResult fetchMostPlayed() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(MOSTPLAYED_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return failed("Steam 热玩榜", "Steam 热玩榜接口返回 HTTP " + response.statusCode());
            }

            JsonNode ranks = objectMapper.readTree(response.body()).path("response").path("ranks");
            if (!ranks.isArray()) {
                return failed("Steam 热玩榜", "Steam 热玩榜接口未返回榜单数组");
            }

            List<HotItem> items = new ArrayList<>();
            OffsetDateTime fetchedAt = OffsetDateTime.now(ZONE);

            for (JsonNode rankNode : ranks) {
                long appId = rankNode.path("appid").asLong(0);
                if (appId <= 0) {
                    continue;
                }

                String title = fetchAppName(appId);
                if (title.isBlank()) {
                    title = "Steam App " + appId;
                }

                long concurrent = rankNode.path("concurrent_in_game").asLong(0);
                if (concurrent <= 0) {
                    concurrent = rankNode.path("peak_in_game").asLong(0);
                }

                items.add(HotItem.builder()
                        .title(title)
                        .url("https://store.steampowered.com/app/" + appId)
                        .source(source())
                        .rank(items.size() + 1)
                        .heat(concurrent > 0 ? concurrent + " online" : null)
                        .fetchedAt(fetchedAt)
                        .build());

                if (items.size() >= 20) {
                    break;
                }
            }

            if (items.isEmpty()) {
                return failed("Steam 热玩榜", "未能从 Steam 官方热玩接口解析到真实数据");
            }

            return success("Steam 热玩榜", items);
        } catch (IOException ex) {
            return failed("Steam 热玩榜", "请求 Steam 热玩榜失败：" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failed("Steam 热玩榜", "请求 Steam 热玩榜被中断");
        }
    }

    private String fetchAppName(long appId) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(APP_DETAILS_URL.formatted(appId)))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "";
            }
            JsonNode data = objectMapper.readTree(response.body()).path(String.valueOf(appId)).path("data");
            return data.path("name").asText("");
        } catch (IOException ex) {
            return "";
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "topsellers";
        }
        String normalized = type.trim().toLowerCase();
        return normalized.equals("mostplayed") ? "mostplayed" : "topsellers";
    }

    private HotSourceResult success(String displayName, List<HotItem> items) {
        return HotSourceResult.builder()
                .source(source())
                .displayName(displayName)
                .success(true)
                .items(items)
                .build();
    }

    private HotSourceResult failed(String displayName, String message) {
        return HotSourceResult.builder()
                .source(source())
                .displayName(displayName)
                .success(false)
                .errorMessage(message)
                .build();
    }
}
