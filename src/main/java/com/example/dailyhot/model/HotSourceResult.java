package com.example.dailyhot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotSourceResult {

    private String source;
    private String displayName;
    private boolean success;
    @Builder.Default
    private List<HotItem> items = new ArrayList<>();
    private String errorMessage;
}
