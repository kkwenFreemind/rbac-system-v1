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
        return generateKey(module, type, id, getCurrentTenantId());
    }

    /**
     * 生成帶有指定租戶的快取鍵
     *
     * @param module 模組名稱（例如："user", "role"）
     * @param type 資料類型（例如："info", "permissions"）
     * @param id 資源 ID
     * @param tenantId 租戶 ID
     * @return 格式化的快取鍵
     */
    public String generateKey(String module, String type, String id, String tenantId) {
        // 格式: {prefix}:{module}:{tenantId}:{type}:{id}
        return String.format("%s:%s:%s:%s:%s",
            redisProperties.getKeyPrefix(),
            module,
            tenantId,
            type,
            id);
    }

    /**
     * 生成使用者快取鍵
     *
     * @param userId 使用者 ID
     * @return 鍵格式："{prefix}:user:{tenantId}:info:{userId}"
     */
    public String userKey(Long userId) {
        return generateKey("user", "info", userId.toString());
    }

    /**
     * 生成角色快取鍵
     *
     * @param roleId 角色 ID
     * @return 鍵格式："{prefix}:role:{tenantId}:info:{roleId}"
     */
    public String roleKey(Long roleId) {
        return generateKey("role", "info", roleId.toString());
    }

    /**
     * 生成使用者權限快取鍵
     *
     * @param userId 使用者 ID
     * @return 鍵格式："{prefix}:user:{tenantId}:permissions:{userId}"
     */
    public String userPermissionsKey(Long userId) {
        return generateKey("user", "permissions", userId.toString());
    }

    /**
     * 生成角色權限快取鍵
     *
     * @param roleId 角色 ID
     * @return 鍵格式："{prefix}:role:{tenantId}:permissions:{roleId}"
     */
    public String rolePermissionsKey(Long roleId) {
        return generateKey("role", "permissions", roleId.toString());
    }

    /**
     * 生成權限快取鍵
     *
     * @param permissionId 權限 ID
     * @return 鍵格式："{prefix}:permission:{tenantId}:info:{permissionId}"
     */
    public String permissionKey(Long permissionId) {
        return generateKey("permission", "info", permissionId.toString());
    }

    /**
     * 生成用於刪除某類型所有鍵的模式
     *
     * @param module 模組名稱
     * @param type 資料類型
     * @return 模式格式："{prefix}:{module}:{tenantId}:{type}:*"
     */
    public String pattern(String module, String type) {
        return pattern(module, type, getCurrentTenantId());
    }

    /**
     * 生成用於刪除某類型所有鍵的模式（指定租戶）
     *
     * @param module 模組名稱
     * @param type 資料類型
     * @param tenantId 租戶 ID
     * @return 模式格式："{prefix}:{module}:{tenantId}:{type}:*"
     */
    public String pattern(String module, String type, String tenantId) {
        return String.format("%s:%s:%s:%s:*",
            redisProperties.getKeyPrefix(),
            module,
            tenantId,
            type);
    }

    /**
     * 生成使用者相關鍵的模式
     *
     * @return 模式格式："{prefix}:user:{tenantId}:*:*"
     */
    public String userPattern() {
        return String.format("%s:user:%s:*:*",
            redisProperties.getKeyPrefix(),
            getCurrentTenantId());
    }

    /**
     * 生成角色相關鍵的模式
     *
     * @return 模式格式："{prefix}:role:{tenantId}:*:*"
     */
    public String rolePattern() {
        return String.format("%s:role:%s:*:*",
            redisProperties.getKeyPrefix(),
            getCurrentTenantId());
    }

    /**
     * 生成權限相關鍵的模式
     *
     * @return 模式格式："{prefix}:permission:{tenantId}:*:*"
     */
    public String permissionPattern() {
        return String.format("%s:permission:%s:*:*",
            redisProperties.getKeyPrefix(),
            getCurrentTenantId());
    }

    /**
     * 獲取當前租戶 ID
     *
     * 注意：此方法應從 TenantContextHolder 獲取租戶 ID
     * 為了避免循環依賴，這裡使用簡單的實現
     * 在實際使用時，應注入 TenantContextHolder
     *
     * @return 當前租戶 ID，預設為 "default"
     */
    private String getCurrentTenantId() {
        // TODO: 注入 TenantContextHolder 並呼叫 getTenantId()
        // 目前返回預設值以避免編譯錯誤
        return "default";
    }
}