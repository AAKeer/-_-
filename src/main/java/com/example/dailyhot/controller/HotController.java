package com.example.dailyhot.controller;

import com.example.dailyhot.model.DailyHotResponse;
import com.example.dailyhot.model.HotSourceResult;
import com.example.dailyhot.service.HotAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/hot", produces = "application/json;charset=UTF-8")
public class HotController {

    private final HotAggregationService hotAggregationService;

    public HotController(HotAggregationService hotAggregationService) {
        this.hotAggregationService = hotAggregationService;
    }

    @GetMapping("/daily")
    public DailyHotResponse daily() {
        return hotAggregationService.fetchDaily();
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<HotSourceResult> source(@PathVariable String source,
                                                  @RequestParam Map<String, String> params) {
        return hotAggregationService.fetchSource(source, params)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
