package com.rbac.common.redis.lock;

import com.rbac.common.core.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基於 Redis 的分散式鎖實作
 * 使用簡化的 Redlock 演算法，適用於單一 Redis 實例
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLock implements DistributedLock {

    private final StringRedisTemplate redisTemplate;

    // ThreadLocal 用於儲存鎖的值，確保只有鎖的持有者能釋放鎖
    private final ThreadLocal<String> lockValueHolder = new ThreadLocal<>();

    // Lua 腳本：原子性檢查並刪除鎖
    private static final String UNLOCK_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    private final DefaultRedisScript<Long> unlockScript = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);

    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Lock key cannot be null or empty");
        }

        String value = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, value, timeout, unit);

        if (Boolean.TRUE.equals(success)) {
            lockValueHolder.set(value);
            log.debug("Successfully acquired lock: {} with timeout: {} {}", key, timeout, unit);
            return true;
        }

        log.debug("Failed to acquire lock: {}", key);
        return false;
    }

    @Override
    public void unlock(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Lock key cannot be null or empty");
        }

        String value = lockValueHolder.get();
        if (value == null) {
            log.warn("Attempting to unlock without holding the lock: {}", key);
            return;
        }

        try {
            // 使用 Lua 腳本確保原子性檢查與刪除
            Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(key),
                value
            );

            if (result != null && result == 1L) {
                log.debug("Successfully released lock: {}", key);
            } else {
                log.warn("Failed to release lock: {} (possibly expired or not owned)", key);
            }
        } catch (Exception e) {
            log.error("Error releasing lock: {}", key, e);
            throw new SystemException("Failed to release distributed lock: " + key, e);
        } finally {
            lockValueHolder.remove();
        }
    }

    @Override
    public <T> T executeWithLock(String key, long timeout, TimeUnit unit, Supplier<T> action) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Lock key cannot be null or empty");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        if (!tryLock(key, timeout, unit)) {
            throw new SystemException("Failed to acquire distributed lock: " + key +
                " (timeout: " + timeout + " " + unit + ")");
        }

        try {
            log.debug("Executing action with lock: {}", key);
            return action.get();
        } catch (Exception e) {
            log.error("Error executing action with lock: {}", key, e);
            throw e;
        } finally {
            try {
                unlock(key);
            } catch (Exception e) {
                log.error("Error releasing lock after action execution: {}", key, e);
                // 不重新拋出，避免覆蓋原始異常
            }
        }
    }
}