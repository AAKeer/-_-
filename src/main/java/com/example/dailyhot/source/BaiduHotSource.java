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
public class BaiduHotSource implements HotSource {

    private static final String URL = "https://top.baidu.com/board?tab=realtime";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0 Safari/537.36";
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public String source() {
        return "baidu";
    }

    @Override
    public String displayName() {
        return "百度热搜";
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
                return failed("未能从百度热搜页面解析到榜单数据");
            }

            return HotSourceResult.builder()
                    .source(source())
                    .displayName(displayName())
                    .success(true)
                    .items(items)
                    .build();
        } catch (IOException ex) {
            return failed("请求百度热搜失败：" + ex.getMessage());
        }
    }

    private List<HotItem> parseItems(Document document) {
        List<HotItem> items = new ArrayList<>();
        OffsetDateTime fetchedAt = OffsetDateTime.now(ZONE);

        for (Element row : document.select("div[class*=category-wrap]")) {
            String title = firstText(row, ".c-single-text-ellipsis", "div[class*=content] a", "a");
            if (title.isBlank() || isNavigationText(title)) {
                continue;
            }

            Element link = row.selectFirst("a[href]");
            String url = link == null ? URL : link.absUrl("href");
            String heat = firstText(row, "div[class*=hot-index]", ".hot-index_1Bl1a");

            items.add(HotItem.builder()
                    .title(title)
                    .url(url.isBlank() ? URL : url)
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

    private String firstText(Element root, String... selectors) {
        for (String selector : selectors) {
            Element element = root.selectFirst(selector);
            if (element != null && !element.text().isBlank()) {
                return element.text().trim();
            }
        }
        return "";
    }

    private boolean isNavigationText(String title) {
        return title.equals("首页") || title.equals("热搜") || title.equals("小说") || title.equals("电影");
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
