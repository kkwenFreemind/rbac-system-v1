package com.rbac.common.redis.util;

import com.rbac.common.redis.config.RedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 快取服務實作
 *
 * 使用 RedisTemplate 實作 CacheService 介面
 * 提供完整的快取操作功能
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Service
public class RedisCacheService implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisProperties redisProperties;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate,
                           RedisProperties redisProperties) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss for key: {}", key);
                return null;
            }

            log.debug("Cache hit for key: {}", key);
            return type.cast(value);
        } catch (Exception e) {
            log.error("Failed to get cache value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void set(String key, Object value, long ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            log.debug("Set cache key: {} with TTL: {} seconds", key, ttl);
        } catch (Exception e) {
            log.error("Failed to set cache value for key: {}", key, e);
        }
    }

    @Override
    public void set(String key, Object value) {
        set(key, value, redisProperties.getDefaultTtl());
    }

    @Override
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            boolean deleted = Boolean.TRUE.equals(result);
            if (deleted) {
                log.debug("Deleted cache key: {}", key);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete cache key: {}", key, e);
            return false;
        }
    }

    @Override
    public long deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                log.debug("No keys found for pattern: {}", pattern);
                return 0;
            }

            Long deletedCount = redisTemplate.delete(keys);
            long count = deletedCount != null ? deletedCount : 0;
            log.debug("Deleted {} keys for pattern: {}", count, pattern);
            return count;
        } catch (Exception e) {
            log.error("Failed to delete keys for pattern: {}", pattern, e);
            return 0;
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to check existence of key: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean expire(String key, long ttl) {
        try {
            Boolean result = redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            boolean expired = Boolean.TRUE.equals(result);
            if (expired) {
                log.debug("Set expiration for key: {} to {} seconds", key, ttl);
            }
            return expired;
        } catch (Exception e) {
            log.error("Failed to set expiration for key: {}", key, e);
            return false;
        }
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Incremented key: {} by {} to {}", key, delta, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to increment key: {}", key, e);
            return null;
        }
    }

    @Override
    public Long decrement(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            log.debug("Decremented key: {} by {} to {}", key, delta, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to decrement key: {}", key, e);
            return null;
        }
    }
}