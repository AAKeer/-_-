package com.example.dailyhot.source;

import com.example.dailyhot.model.HotSourceResult;
import com.example.dailyhot.service.HotSource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SkillsHotSource implements HotSource {

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
        return HotSourceResult.builder()
                .source(source())
                .displayName(period.equals("daily") ? "Skills 每日榜单" : "Skills 全时间榜单")
                .success(false)
                .errorMessage("暂未接入真实数据源")
                .build();
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "all";
        }
        return period.trim().equalsIgnoreCase("daily") ? "daily" : "all";
    }
}
