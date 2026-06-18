package com.example.dailyhot.service;

import com.example.dailyhot.model.HotSourceResult;

import java.util.Map;

public interface HotSource {

    String source();

    String displayName();

    HotSourceResult fetch();

    default HotSourceResult fetch(Map<String, String> params) {
        return fetch();
    }
}
