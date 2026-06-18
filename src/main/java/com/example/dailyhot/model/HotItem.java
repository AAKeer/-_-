package com.example.dailyhot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotItem {

    private String title;
    private String url;
    private String source;
    private Integer rank;
    private String heat;
    private OffsetDateTime fetchedAt;
}
