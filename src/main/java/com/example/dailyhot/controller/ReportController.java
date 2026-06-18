package com.example.dailyhot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/report", produces = "application/json;charset=UTF-8")
public class ReportController {

    @GetMapping("/summary")
    public ResponseEntity<Map<String, String>> summary() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "AI summary is reserved for future versions."));
    }
}
