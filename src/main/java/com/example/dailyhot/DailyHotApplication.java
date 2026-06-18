package com.example.dailyhot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DailyHotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyHotApplication.class, args);
    }
}
