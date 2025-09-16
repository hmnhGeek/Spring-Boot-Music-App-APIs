package com.musicapp.music_app.config;

import com.musicapp.music_app.services.RedisService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CacheStartupRunner implements CommandLineRunner {
    @Autowired
    private RedisService redisService;

    @Override
    public void run(String... args) {
        redisService.clearCache();
        System.out.println("[INFO] Redis cache cleared on application startup.");
    }
}