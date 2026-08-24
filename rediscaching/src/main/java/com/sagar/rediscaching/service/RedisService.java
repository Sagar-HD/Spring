package com.sagar.rediscaching.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final RedisTemplate<String,Object> redisTemplate;

    private final ObjectMapper objectMapper;
    public RedisService(RedisTemplate<String,Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> void set(String key, T value,Long ttl) {
        redisTemplate.opsForValue().set(key, value,ttl, TimeUnit.SECONDS);
    }

    public <T> T get(String key, Class<T> type) {

        Object value = redisTemplate
                .opsForValue()
                .get(key);

        if (value == null) {
            return null;
        }

        return objectMapper.convertValue(value, type);
    }
    public <T> List<T> getList(String key, Class<T> elementType) {

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return objectMapper.convertValue(
                value,
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, elementType)
        );
    }

    public void remove(String key) {
        redisTemplate.delete(key);
    }

}
