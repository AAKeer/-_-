package com.example.dailyhot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyHotResponse {

    private LocalDate date;
    private OffsetDateTime fetchedAt;
    @Builder.Default
    private List<HotSourceResult> sources = new ArrayList<>();
}
