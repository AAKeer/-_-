package com.example.dailyhot.service;

import com.example.dailyhot.model.HotSourceResult;

public interface HotSource {

    String source();

    String displayName();

    HotSourceResult fetch();
}
