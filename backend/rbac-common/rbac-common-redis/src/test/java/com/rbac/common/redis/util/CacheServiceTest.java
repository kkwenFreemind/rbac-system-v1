package com.rbac.common.redis.util;

import com.rbac.common.core.exception.SystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CacheService 單元測試
 * 使用 Mockito 模擬 Redis 操作
 *
 * @author RBAC System
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheService = new RedisCacheService(redisTemplate);
    }

    @Test
    void get_WithExistingKey_ShouldReturnValue() {
        // Given
        String key = "test:key";
        String expectedValue = "test-value";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // When
        String result = cacheService.get(key, String.class);

        // Then
        assertEquals(expectedValue, result);
        verify(valueOperations).get(key);
    }

    @Test
    void get_WithNonExistingKey_ShouldReturnNull() {
        // Given
        String key = "test:key";
        when(valueOperations.get(key)).thenReturn(null);

        // When
        String result = cacheService.get(key, String.class);

        // Then
        assertNull(result);
        verify(valueOperations).get(key);
    }

    @Test
    void get_WithNullKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.get(null, String.class));
    }

    @Test
    void get_WithEmptyKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.get("", String.class));
    }

    @Test
    void set_WithKeyAndValue_ShouldStoreWithDefaultTtl() {
        // Given
        String key = "test:key";
        String value = "test-value";
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);

        // When
        cacheService.set(key, value);

        // Then
        verify(valueOperations).set(eq(key), eq(value), any(Duration.class));
    }

    @Test
    void set_WithKeyValueAndTtl_ShouldStoreWithSpecifiedTtl() {
        // Given
        String key = "test:key";
        String value = "test-value";
        long ttl = 300L; // 5 minutes

        // When
        cacheService.set(key, value, ttl);

        // Then
        verify(valueOperations).set(eq(key), eq(value), eq(Duration.ofSeconds(ttl)));
    }

    @Test
    void set_WithNullKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.set(null, "value"));
    }

    @Test
    void set_WithEmptyKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.set("", "value"));
    }

    @Test
    void delete_WithExistingKey_ShouldReturnTrue() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(1L);

        // When
        boolean result = cacheService.delete(key);

        // Then
        assertTrue(result);
        verify(redisTemplate).delete(key);
    }

    @Test
    void delete_WithNonExistingKey_ShouldReturnFalse() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(0L);

        // When
        boolean result = cacheService.delete(key);

        // Then
        assertFalse(result);
        verify(redisTemplate).delete(key);
    }

    @Test
    void delete_WithNullKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.delete(null));
    }

    @Test
    void deletePattern_ShouldUseKeysAndDelete() {
        // Given
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(java.util.Set.of("test:key1", "test:key2"));
        when(redisTemplate.delete(anyCollection())).thenReturn(2L);

        // When
        long result = cacheService.deletePattern(pattern);

        // Then
        assertEquals(2L, result);
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(anyCollection());
    }

    @Test
    void deletePattern_WithNullPattern_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.deletePattern(null));
    }

    @Test
    void exists_WithExistingKey_ShouldReturnTrue() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // When
        boolean result = cacheService.exists(key);

        // Then
        assertTrue(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void exists_WithNonExistingKey_ShouldReturnFalse() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(false);

        // When
        boolean result = cacheService.exists(key);

        // Then
        assertFalse(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void exists_WithNullKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.exists(null));
    }

    @Test
    void expire_WithExistingKey_ShouldReturnTrue() {
        // Given
        String key = "test:key";
        long ttl = 300L;
        when(redisTemplate.expire(key, ttl, TimeUnit.SECONDS)).thenReturn(true);

        // When
        boolean result = cacheService.expire(key, ttl);

        // Then
        assertTrue(result);
        verify(redisTemplate).expire(key, ttl, TimeUnit.SECONDS);
    }

    @Test
    void expire_WithNonExistingKey_ShouldReturnFalse() {
        // Given
        String key = "test:key";
        long ttl = 300L;
        when(redisTemplate.expire(key, ttl, TimeUnit.SECONDS)).thenReturn(false);

        // When
        boolean result = cacheService.expire(key, ttl);

        // Then
        assertFalse(result);
        verify(redisTemplate).expire(key, ttl, TimeUnit.SECONDS);
    }

    @Test
    void expire_WithNullKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.expire(null, 300L));
    }

    @Test
    void increment_ShouldReturnIncrementedValue() {
        // Given
        String key = "test:counter";
        long delta = 5L;
        when(valueOperations.increment(key, delta)).thenReturn(10L);

        // When
        Long result = cacheService.increment(key, delta);

        // Then
        assertEquals(10L, result);
        verify(valueOperations).increment(key, delta);
    }

    @Test
    void increment_WithNullKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.increment(null, 1L));
    }

    @Test
    void decrement_ShouldReturnDecrementedValue() {
        // Given
        String key = "test:counter";
        long delta = 3L;
        when(valueOperations.decrement(key, delta)).thenReturn(7L);

        // When
        Long result = cacheService.decrement(key, delta);

        // Then
        assertEquals(7L, result);
        verify(valueOperations).decrement(key, delta);
    }

    @Test
    void decrement_WithNullKey_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheService.decrement(null, 1L));
    }

    @Test
    void redisOperationFailure_ShouldThrowSystemException() {
        // Given
        String key = "test:key";
        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis connection failed"));

        // When & Then
        assertThrows(SystemException.class, () ->
            cacheService.get(key, String.class));
    }
}