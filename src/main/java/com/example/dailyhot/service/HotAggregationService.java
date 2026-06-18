package com.example.dailyhot.service;

import com.example.dailyhot.model.DailyHotResponse;
import com.example.dailyhot.model.HotSourceResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class HotAggregationService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private final List<HotSource> sources;

    public HotAggregationService(List<HotSource> sources) {
        this.sources = sources;
    }

    public DailyHotResponse fetchDaily() {
        OffsetDateTime fetchedAt = OffsetDateTime.now(DEFAULT_ZONE);
        List<HotSourceResult> results = sources.stream()
                .map(this::fetchSafely)
                .toList();

        return DailyHotResponse.builder()
                .date(LocalDate.now(DEFAULT_ZONE))
                .fetchedAt(fetchedAt)
                .sources(results)
                .build();
    }

    public Optional<HotSourceResult> fetchSource(String source) {
        return sources.stream()
                .filter(candidate -> candidate.source().equalsIgnoreCase(source))
                .findFirst()
                .map(this::fetchSafely);
    }

    private HotSourceResult fetchSafely(HotSource source) {
        try {
            HotSourceResult result = source.fetch();
            if (result.getSource() == null) {
                result.setSource(source.source());
            }
            if (result.getDisplayName() == null) {
                result.setDisplayName(source.displayName());
            }
            return result;
        } catch (Exception ex) {
            return HotSourceResult.builder()
                    .source(source.source())
                    .displayName(source.displayName())
                    .success(false)
                    .errorMessage("来源暂时不可用：" + ex.getMessage())
                    .build();
        }
    }
}
