package com.example.dailyhot.task;

import com.example.dailyhot.service.HotAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HotListUpdateTask {

    private static final Logger log = LoggerFactory.getLogger(HotListUpdateTask.class);

    private final HotAggregationService hotAggregationService;

    public HotListUpdateTask(HotAggregationService hotAggregationService) {
        this.hotAggregationService = hotAggregationService;
    }

    @Scheduled(fixedRate = 43_200_000, initialDelay = 10_000)
    public void verifyHotSources() {
        log.info("开始执行榜单更新任务...");
        hotAggregationService.fetchSource("steam", Map.of("type", "topsellers"))
                .ifPresent(result -> log.info("Steam 热销榜更新完成，success={}, items={}",
                        result.isSuccess(), result.getItems().size()));
        hotAggregationService.fetchSource("github", Map.of("since", "daily"))
                .ifPresent(result -> log.info("GitHub 热榜更新完成，success={}, items={}",
                        result.isSuccess(), result.getItems().size()));
    }
}
