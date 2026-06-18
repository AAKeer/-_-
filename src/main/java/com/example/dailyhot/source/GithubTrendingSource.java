package com.example.dailyhot.source;

import com.example.dailyhot.model.HotItem;
import com.example.dailyhot.model.HotSourceResult;
import com.example.dailyhot.service.HotSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GithubTrendingSource implements HotSource {

    private static final String BASE_URL = "https://github.com/trending";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0 Safari/537.36";
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public String source() {
        return "github";
    }

    @Override
    public String displayName() {
        return "GitHub 热门项目";
    }

    @Override
    public HotSourceResult fetch() {
        return fetch(Map.of());
    }

    @Override
    public HotSourceResult fetch(Map<String, String> params) {
        String since = normalizeSince(params.get("since"));

        try {
            Document document = Jsoup.connect(BASE_URL)
                    .data("since", since)
                    .userAgent(USER_AGENT)
                    .timeout(8000)
                    .get();

            List<HotItem> items = parseItems(document);
            if (items.isEmpty()) {
                return failed(since, "未能从 GitHub Trending 页面解析到真实项目数据");
            }

            return HotSourceResult.builder()
                    .source(source())
                    .displayName(displayNameFor(since))
                    .success(true)
                    .items(items)
                    .build();
        } catch (IOException ex) {
            return failed(since, "请求 GitHub Trending 失败：" + ex.getMessage());
        }
    }

    private String normalizeSince(String since) {
        if (since == null || since.isBlank()) {
            return "daily";
        }
        String normalized = since.trim().toLowerCase();
        return normalized.equals("weekly") ? "weekly" : "daily";
    }

    private List<HotItem> parseItems(Document document) {
        List<HotItem> items = new ArrayList<>();
        OffsetDateTime fetchedAt = OffsetDateTime.now(ZONE);

        for (Element article : document.select("article.Box-row")) {
            Element link = article.selectFirst("h2 a[href]");
            if (link == null) {
                continue;
            }

            String title = link.text().replaceAll("\\s+", " ").trim();
            String url = link.absUrl("href");
            String heat = "";
            Element starsToday = article.selectFirst("span.d-inline-block.float-sm-right");
            if (starsToday != null) {
                heat = starsToday.text().replaceAll("\\s+", " ").trim();
            }

            items.add(HotItem.builder()
                    .title(title)
                    .url(url)
                    .source(source())
                    .rank(items.size() + 1)
                    .heat(heat.isBlank() ? null : heat)
                    .fetchedAt(fetchedAt)
                    .build());

            if (items.size() >= 20) {
                break;
            }
        }

        return items;
    }

    private String displayNameFor(String since) {
        return since.equals("weekly") ? "GitHub 每周热榜" : "GitHub 每日热榜";
    }

    private HotSourceResult failed(String since, String message) {
        return HotSourceResult.builder()
                .source(source())
                .displayName(displayNameFor(since))
                .success(false)
                .errorMessage(message)
                .build();
    }
}
