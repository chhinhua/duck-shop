package com.hdshop.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdshop.entity.Product;
import com.hdshop.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl<T> implements RedisService<T> {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);
    @Value("${spring.data.redis.use-redis-cache}")
    private boolean useRedisCache;

    private <E> String getKeyPrefixFromEntityClass(E entity) {
        if (entity instanceof Product) {
            return AppUtils.KEY_PREFIX_GET_ALL_PRODUCT;
        }
        return AppUtils.KEY_PREFIX_GET_ALL_ORDER;
    }

    @SafeVarargs
    @Override
    public final <K> String getKeyFrom(String keyPrefix, K... keyComponents) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(keyPrefix);
        for (K keyComponent : keyComponents) {
            keyBuilder.append(":").append(keyComponent);
        }
        logger.info("Key: " + keyBuilder);
        return keyBuilder.toString();
    }

    @Override
    public void clearCache(T entity) {
        logger.info("run clear method");
        String keyPrefix = getKeyPrefixFromEntityClass(entity);
        logger.info("keyPrefix: " + keyPrefix);
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");

        if (keys != null && !keys.isEmpty()) {
            logger.info("set keys" + keys);
            logger.info(String.valueOf(redisTemplate.delete(keys)));
        }
    }

    @Override
    public <R> R getAll(String key, Class<R> responseType) throws JsonProcessingException {
        if (!useRedisCache) {
            return null;
        }
        String json = (String) redisTemplate.opsForValue().get(key);
        R response = json != null ? redisObjectMapper.readValue(json, responseType) : null;
        String haveData = json != null ? "true" : "false";
        logger.info(String.format("Get cache vs key: %s, have data: %s", key, haveData));
        return response;
    }

    @Override
    public <R> void saveAll(String key, R data) throws JsonProcessingException {
        String json = redisObjectMapper.writeValueAsString(data);
        assert json != null;
        redisTemplate.opsForValue().set(key, json);
        logger.info(String.format("Save cache successfully, key: %s", key));
    }
}