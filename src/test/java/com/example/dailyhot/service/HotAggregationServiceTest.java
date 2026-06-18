package com.example.dailyhot.service;

import com.example.dailyhot.model.HotItem;
import com.example.dailyhot.model.HotSourceResult;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotAggregationServiceTest {

    @Test
    void fetchDailyIncludesAllSourcesAndConvertsFailures() {
        HotAggregationService service = new HotAggregationService(List.of(
                successfulSource("baidu", "百度热搜"),
                failingSource("github", "GitHub 热门项目")
        ));

        var response = service.fetchDaily();

        assertThat(response.getDate()).isNotNull();
        assertThat(response.getFetchedAt()).isNotNull();
        assertThat(response.getSources()).hasSize(2);
        assertThat(response.getSources().get(0).isSuccess()).isTrue();
        assertThat(response.getSources().get(0).getItems()).hasSize(1);
        assertThat(response.getSources().get(1).isSuccess()).isFalse();
        assertThat(response.getSources().get(1).getErrorMessage()).contains("来源暂时不可用");
    }

    @Test
    void fetchSourceReturnsMatchingSourceOnly() {
        HotAggregationService service = new HotAggregationService(List.of(
                successfulSource("baidu", "百度热搜"),
                successfulSource("github", "GitHub 热门项目")
        ));

        var result = service.fetchSource("github");

        assertThat(result).isPresent();
        assertThat(result.get().getSource()).isEqualTo("github");
        assertThat(result.get().getDisplayName()).isEqualTo("GitHub 热门项目");
    }

    @Test
    void fetchSourceReturnsEmptyForUnknownSource() {
        HotAggregationService service = new HotAggregationService(List.of(
                successfulSource("baidu", "百度热搜")
        ));

        assertThat(service.fetchSource("weibo")).isEmpty();
    }

    private HotSource successfulSource(String source, String displayName) {
        return new HotSource() {
            @Override
            public String source() {
                return source;
            }

            @Override
            public String displayName() {
                return displayName;
            }

            @Override
            public HotSourceResult fetch() {
                return HotSourceResult.builder()
                        .source(source)
                        .displayName(displayName)
                        .success(true)
                        .items(List.of(HotItem.builder()
                                .title("测试标题")
                                .url("https://example.com")
                                .source(source)
                                .rank(1)
                                .heat("100")
                                .fetchedAt(OffsetDateTime.now())
                                .build()))
                        .build();
            }
        };
    }

    private HotSource failingSource(String source, String displayName) {
        return new HotSource() {
            @Override
            public String source() {
                return source;
            }

            @Override
            public String displayName() {
                return displayName;
            }

            @Override
            public HotSourceResult fetch() {
                throw new IllegalStateException("network failed");
            }
        };
    }
}
