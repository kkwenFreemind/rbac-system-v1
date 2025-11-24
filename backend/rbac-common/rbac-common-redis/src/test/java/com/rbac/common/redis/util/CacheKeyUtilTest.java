package com.rbac.common.redis.util;

import com.rbac.common.redis.config.RedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CacheKeyUtil 單元測試
 *
 * @author RBAC System
 * @since 1.0.0
 */
class CacheKeyUtilTest {

    private CacheKeyUtil cacheKeyUtil;
    private RedisProperties redisProperties;

    @BeforeEach
    void setUp() {
        redisProperties = new RedisProperties();
        redisProperties.setKeyPrefix("rbac");
        cacheKeyUtil = new CacheKeyUtil(redisProperties);
    }

    @Test
    void generateKey_WithValidParameters_ShouldReturnFormattedKey() {
        // Given
        String module = "user";
        String type = "info";
        String id = "123";

        // When
        String result = cacheKeyUtil.generateKey(module, type, id);

        // Then
        assertEquals("rbac:user:info:123", result);
    }

    @Test
    void generateKey_WithNullModule_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey(null, "type", "id"));
    }

    @Test
    void generateKey_WithEmptyModule_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey("", "type", "id"));
    }

    @Test
    void generateKey_WithBlankModule_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey("   ", "type", "id"));
    }

    @Test
    void generateKey_WithNullType_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey("module", null, "id"));
    }

    @Test
    void generateKey_WithEmptyType_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey("module", "", "id"));
    }

    @Test
    void generateKey_WithNullId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey("module", "type", null));
    }

    @Test
    void generateKey_WithEmptyId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey("module", "type", ""));
    }

    @Test
    void generateKey_WithTenantId_ShouldReturnTenantSpecificKey() {
        // Given
        String tenantId = "tenant_123";
        String module = "user";
        String type = "info";
        String id = "456";

        // When
        String result = cacheKeyUtil.generateKey(tenantId, module, type, id);

        // Then
        assertEquals("rbac:tenant_123:user:info:456", result);
    }

    @Test
    void generateKey_WithNullTenantId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey(null, "module", "type", "id"));
    }

    @Test
    void generateKey_WithEmptyTenantId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.generateKey("", "module", "type", "id"));
    }

    @Test
    void userKey_WithValidUserId_ShouldReturnUserKey() {
        // Given
        Long userId = 123L;

        // When
        String result = cacheKeyUtil.userKey(userId);

        // Then
        assertEquals("rbac:user:info:123", result);
    }

    @Test
    void userKey_WithNullUserId_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            cacheKeyUtil.userKey(null));
    }

    @Test
    void roleKey_WithValidRoleId_ShouldReturnRoleKey() {
        // Given
        Long roleId = 456L;

        // When
        String result = cacheKeyUtil.roleKey(roleId);

        // Then
        assertEquals("rbac:role:info:456", result);
    }

    @Test
    void roleKey_WithNullRoleId_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            cacheKeyUtil.roleKey(null));
    }

    @Test
    void userPermissionsKey_WithValidUserId_ShouldReturnPermissionsKey() {
        // Given
        Long userId = 789L;

        // When
        String result = cacheKeyUtil.userPermissionsKey(userId);

        // Then
        assertEquals("rbac:user:permissions:789", result);
    }

    @Test
    void userPermissionsKey_WithNullUserId_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            cacheKeyUtil.userPermissionsKey(null));
    }

    @Test
    void pattern_WithValidParameters_ShouldReturnPattern() {
        // Given
        String module = "user";
        String type = "info";

        // When
        String result = cacheKeyUtil.pattern(module, type);

        // Then
        assertEquals("rbac:user:info:*", result);
    }

    @Test
    void pattern_WithNullModule_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.pattern(null, "type"));
    }

    @Test
    void pattern_WithEmptyModule_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.pattern("", "type"));
    }

    @Test
    void pattern_WithNullType_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.pattern("module", null));
    }

    @Test
    void pattern_WithEmptyType_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.pattern("module", ""));
    }

    @Test
    void overloadedMethods_ShouldWorkWithTenantId() {
        // Given
        String tenantId = "tenant_123";
        Long userId = 456L;
        Long roleId = 789L;

        // When
        String userKey = cacheKeyUtil.userKey(tenantId, "info", userId);
        String roleKey = cacheKeyUtil.roleKey(tenantId, "permissions", roleId);
        String pattern = cacheKeyUtil.pattern(tenantId, "user", "info");

        // Then
        assertEquals("rbac:tenant_123:user:info:456", userKey);
        assertEquals("rbac:tenant_123:role:permissions:789", roleKey);
        assertEquals("rbac:tenant_123:user:info:*", pattern);
    }

    @Test
    void overloadedMethods_WithNullTenantId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.userKey(null, "info", 123L));
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.roleKey(null, "permissions", 456L));
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.pattern(null, "user", "info"));
    }

    @Test
    void overloadedMethods_WithEmptyTenantId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.userKey("", "info", 123L));
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.roleKey("", "permissions", 456L));
        assertThrows(IllegalArgumentException.class, () ->
            cacheKeyUtil.pattern("", "user", "info"));
    }
}