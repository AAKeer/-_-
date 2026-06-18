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

@Component
public class GithubTrendingSource implements HotSource {

    private static final String URL = "https://github.com/trending";
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
        try {
            Document document = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .timeout(8000)
                    .get();

            List<HotItem> items = parseItems(document);
            if (items.isEmpty()) {
                return failed("未能从 GitHub Trending 页面解析到项目数据");
            }

            return HotSourceResult.builder()
                    .source(source())
                    .displayName(displayName())
                    .success(true)
                    .items(items)
                    .build();
        } catch (IOException ex) {
            return failed("请求 GitHub Trending 失败：" + ex.getMessage());
        }
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

    private HotSourceResult failed(String message) {
        return HotSourceResult.builder()
                .source(source())
                .displayName(displayName())
                .success(false)
                .errorMessage(message)
                .build();
    }
}
