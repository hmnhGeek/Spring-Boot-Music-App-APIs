package com.musicapp.music_app.config;

import com.musicapp.music_app.model.Config;
import com.musicapp.music_app.repositories.ConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppCache {
    @Getter
    private Map<String, String> appCache;

    @Autowired
    private ConfigRepository configRepository;

    @PostConstruct
    public void init() {
        appCache = new HashMap<>();
        List<Config> configs = configRepository.findAll();
        for (Config config : configs) {
            appCache.put(config.getKey(), config.getValue());
        }
    }
}
