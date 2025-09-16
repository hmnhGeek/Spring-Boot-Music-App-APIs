package com.musicapp.music_app.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {
    @Autowired
    public RedisTemplate redisTemplate;

    public <T> T get(String key, Class<T> entityClass) {
        try {
            Object object = redisTemplate.opsForValue().get(key);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(object.toString(), entityClass);
        } catch (Exception e) {
            log.error("RedisService::get: " + e);
            return null;
        }
    }

    public void set(String key, Object o, Long ttl) {
        try {
            redisTemplate.opsForValue().set(key, o.toString(), ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("RedisService::set: " + e);
        }
    }

    public void setByteArrayResource(String key, ByteArrayResource resource, Long ttl) {
        try {
            String base64Data = Base64.getEncoder().encodeToString(resource.getByteArray());
            redisTemplate.opsForValue().set(key, base64Data, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("RedisService::setByteArrayResource: " + e);
        }
    }

    public ByteArrayResource getByteArrayResource(String key) {
        try {
            String base64Data = (String) redisTemplate.opsForValue().get(key);
            if (base64Data == null) return null;
            byte[] data = Base64.getDecoder().decode(base64Data);
            return new ByteArrayResource(data);
        } catch (Exception e) {
            log.error("RedisService::getByteArrayResource: " + e);
            return null;
        }
    }

    public void clearCache() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}
