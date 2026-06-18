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
public class SkillsHotSource implements HotSource {

    private static final String BASE_URL = "https://www.skills.sh";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0 Safari/537.36";
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public String source() {
        return "skills";
    }

    @Override
    public String displayName() {
        return "Skills 榜单";
    }

    @Override
    public HotSourceResult fetch() {
        return fetch(Map.of());
    }

    @Override
    public HotSourceResult fetch(Map<String, String> params) {
        String period = normalizePeriod(params.get("period"));
        String url = urlFor(period);
        String displayName = displayNameFor(period);

        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            List<HotItem> items = parseItems(document);
            if (items.isEmpty()) {
                return failed(displayName, "未能从 Skills 榜单页面解析到真实数据");
            }

            return HotSourceResult.builder()
                    .source(source())
                    .displayName(displayName)
                    .success(true)
                    .items(items)
                    .build();
        } catch (IOException ex) {
            return failed(displayName, "请求 Skills 榜单失败：" + ex.getMessage());
        }
    }

    private List<HotItem> parseItems(Document document) {
        List<HotItem> items = new ArrayList<>();
        OffsetDateTime fetchedAt = OffsetDateTime.now(ZONE);

        for (Element link : document.select("a[href^=/]")) {
            String href = link.attr("href");
            if (!isSkillHref(href)) {
                continue;
            }

            Element rankElement = link.selectFirst("span.font-mono");
            Element titleElement = link.selectFirst("h3");
            Element repoElement = link.selectFirst("p.font-mono");
            Element installElement = link.select("span.font-mono").last();
            if (rankElement == null || titleElement == null || repoElement == null || installElement == null) {
                continue;
            }

            String rankText = rankElement.text().trim();
            String title = titleElement.text().trim();
            String repo = repoElement.text().trim();
            String installs = installElement.text().trim();
            if (title.isBlank() || repo.isBlank() || installs.isBlank()) {
                continue;
            }

            items.add(HotItem.builder()
                    .title(title + " · " + repo)
                    .url(BASE_URL + href)
                    .source(source())
                    .rank(parseRank(rankText, items.size() + 1))
                    .heat(installs)
                    .fetchedAt(fetchedAt)
                    .build());

            if (items.size() >= 20) {
                break;
            }
        }

        return items;
    }

    private boolean isSkillHref(String href) {
        if (href == null || href.isBlank()) {
            return false;
        }
        String[] parts = href.split("/");
        return parts.length == 4
                && !parts[1].isBlank()
                && !parts[2].isBlank()
                && !parts[3].isBlank()
                && !href.equals("/trending")
                && !href.equals("/hot");
    }

    private int parseRank(String rankText, int fallback) {
        try {
            return Integer.parseInt(rankText.replaceAll("\\D+", ""));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "all";
        }
        String normalized = period.trim().toLowerCase();
        if (normalized.equals("daily") || normalized.equals("hot")) {
            return normalized;
        }
        return "all";
    }

    private String urlFor(String period) {
        return switch (period) {
            case "daily" -> BASE_URL + "/trending";
            case "hot" -> BASE_URL + "/hot";
            default -> BASE_URL + "/";
        };
    }

    private String displayNameFor(String period) {
        return switch (period) {
            case "daily" -> "Skills 每日榜单";
            case "hot" -> "Skills 实时热榜";
            default -> "Skills 全时间榜单";
        };
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
