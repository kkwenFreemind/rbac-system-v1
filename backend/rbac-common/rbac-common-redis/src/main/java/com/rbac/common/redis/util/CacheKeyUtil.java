package com.rbac.common.redis.util;

import com.rbac.common.redis.config.RedisProperties;
import org.springframework.stereotype.Component;

/**
 * 快取鍵生成工具類
 *
 * 生成標準化的快取鍵，包含租戶資訊
 * 確保快取鍵的一致性和可預測性
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Component
public class CacheKeyUtil {

    private final RedisProperties redisProperties;

    public CacheKeyUtil(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * 生成帶有租戶上下文的快取鍵
     *
     * @param module 模組名稱（例如："user", "role"）
     * @param type 資料類型（例如："info", "permissions"）
     * @param id 資源 ID
     * @return 格式化的快取鍵
     */
    public String generateKey(String module, String type, String id) {
        if (module == null || module.trim().isEmpty()) {
            throw new IllegalArgumentException("Module cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        // 格式: {prefix}:{module}:{type}:{id}
        return String.format("%s:%s:%s:%s",
            redisProperties.getKeyPrefix(),
            module,
            type,
            id);
    }

    /**
     * 生成帶有指定租戶的快取鍵
     *
     * @param tenantId 租戶 ID
     * @param module 模組名稱（例如："user", "role"）
     * @param type 資料類型（例如："info", "permissions"）
     * @param id 資源 ID
     * @return 格式化的快取鍵
     */
    public String generateKey(String tenantId, String module, String type, String id) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }
        if (module == null || module.trim().isEmpty()) {
            throw new IllegalArgumentException("Module cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        // 格式: {prefix}:{tenantId}:{module}:{type}:{id}
        return String.format("%s:%s:%s:%s:%s",
            redisProperties.getKeyPrefix(),
            tenantId,
            module,
            type,
            id);
    }

    /**
     * 生成使用者快取鍵
     *
     * @param userId 使用者 ID
     * @return 鍵格式："{prefix}:user:info:{userId}"
     */
    public String userKey(Long userId) {
        return generateKey("user", "info", userId.toString());
    }

    /**
     * 生成使用者快取鍵（指定租戶和類型）
     *
     * @param tenantId 租戶 ID
     * @param type 資料類型
     * @param userId 使用者 ID
     * @return 鍵格式："{prefix}:{tenantId}:user:{type}:{userId}"
     */
    public String userKey(String tenantId, String type, Long userId) {
        return generateKey(tenantId, "user", type, userId.toString());
    }

    /**
     * 生成角色快取鍵
     *
     * @param roleId 角色 ID
     * @return 鍵格式："{prefix}:role:info:{roleId}"
     */
    public String roleKey(Long roleId) {
        return generateKey("role", "info", roleId.toString());
    }

    /**
     * 生成角色快取鍵（指定租戶和類型）
     *
     * @param tenantId 租戶 ID
     * @param type 資料類型
     * @param roleId 角色 ID
     * @return 鍵格式："{prefix}:{tenantId}:role:{type}:{roleId}"
     */
    public String roleKey(String tenantId, String type, Long roleId) {
        return generateKey(tenantId, "role", type, roleId.toString());
    }

    /**
     * 生成使用者權限快取鍵
     *
     * @param userId 使用者 ID
     * @return 鍵格式："{prefix}:user:permissions:{userId}"
     */
    public String userPermissionsKey(Long userId) {
        return generateKey("user", "permissions", userId.toString());
    }

    /**
     * 生成角色權限快取鍵
     *
     * @param roleId 角色 ID
     * @return 鍵格式："{prefix}:role:permissions:{roleId}"
     */
    public String rolePermissionsKey(Long roleId) {
        return generateKey("role", "permissions", roleId.toString());
    }

    /**
     * 生成權限快取鍵
     *
     * @param permissionId 權限 ID
     * @return 鍵格式："{prefix}:permission:info:{permissionId}"
     */
    public String permissionKey(Long permissionId) {
        return generateKey("permission", "info", permissionId.toString());
    }

    /**
     * 生成用於刪除某類型所有鍵的模式（指定租戶）
     *
     * @param tenantId 租戶 ID
     * @param module 模組名稱
     * @param type 資料類型
     * @return 模式格式："{prefix}:{tenantId}:{module}:{type}:*"
     */
    public String pattern(String tenantId, String module, String type) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }
        if (module == null || module.trim().isEmpty()) {
            throw new IllegalArgumentException("Module cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        return String.format("%s:%s:%s:%s:*",
            redisProperties.getKeyPrefix(),
            tenantId,
            module,
            type);
    }

    /**
     * 生成用於刪除某類型所有鍵的模式
     *
     * @param module 模組名稱
     * @param type 資料類型
     * @return 模式格式："{prefix}:{module}:{type}:*"
     */
    public String pattern(String module, String type) {
        if (module == null || module.trim().isEmpty()) {
            throw new IllegalArgumentException("Module cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        return String.format("%s:%s:%s:*",
            redisProperties.getKeyPrefix(),
            module,
            type);
    }

    /**
     * 生成使用者相關鍵的模式
     *
     * @return 模式格式："{prefix}:user:*:*"
     */
    public String userPattern() {
        return String.format("%s:user:*:*",
            redisProperties.getKeyPrefix());
    }

    /**
     * 生成角色相關鍵的模式
     *
     * @return 模式格式："{prefix}:role:*:*"
     */
    public String rolePattern() {
        return String.format("%s:role:*:*",
            redisProperties.getKeyPrefix());
    }

    /**
     * 生成權限相關鍵的模式
     *
     * @return 模式格式："{prefix}:permission:*:*"
     */
    public String permissionPattern() {
        return String.format("%s:permission:*:*",
            redisProperties.getKeyPrefix());
    }
}