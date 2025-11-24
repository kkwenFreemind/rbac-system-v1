package com.rbac.common.redis.lock;

import com.rbac.common.redis.config.RedisProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 分散式鎖鍵生成器
 * 提供標準化的鎖鍵生成方法，支援租戶隔離
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Component
public class LockKeyGenerator {

    private final RedisProperties redisProperties;

    public LockKeyGenerator(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * 生成標準化的鎖鍵
     *
     * @param module    模組名稱（例如："user", "role", "permission"）
     * @param operation 操作名稱（例如："create", "update", "delete", "assign"）
     * @param resource  資源識別符（例如：userId, roleId）
     * @return 格式化的鎖鍵
     */
    public String generateKey(String module, String operation, String resource) {
        validateParameters(module, operation, resource);

        String prefix = redisProperties.getKeyPrefix();
        return String.format("%s:lock:%s:%s:%s", prefix, module, operation, resource);
    }

    /**
     * 生成標準化的鎖鍵（帶租戶上下文）
     *
     * @param tenantId  租戶 ID
     * @param module    模組名稱
     * @param operation 操作名稱
     * @param resource  資源識別符
     * @return 格式化的鎖鍵
     */
    public String generateKey(String tenantId, String module, String operation, String resource) {
        validateParameters(module, operation, resource);
        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }

        String prefix = redisProperties.getKeyPrefix();
        return String.format("%s:lock:%s:%s:%s:%s", prefix, tenantId, module, operation, resource);
    }

    /**
     * 生成使用者相關操作的鎖鍵
     *
     * @param userId 使用者 ID
     * @return 鎖鍵格式："{prefix}:lock:user:{operation}:{userId}"
     */
    public String userLock(String operation, Long userId) {
        return generateKey("user", operation, userId.toString());
    }

    /**
     * 生成使用者相關操作的鎖鍵（帶租戶）
     *
     * @param tenantId 租戶 ID
     * @param operation 操作名稱
     * @param userId   使用者 ID
     * @return 鎖鍵格式："{prefix}:lock:{tenantId}:user:{operation}:{userId}"
     */
    public String userLock(String tenantId, String operation, Long userId) {
        return generateKey(tenantId, "user", operation, userId.toString());
    }

    /**
     * 生成角色相關操作的鎖鍵
     *
     * @param operation 操作名稱
     * @param roleId   角色 ID
     * @return 鎖鍵格式："{prefix}:lock:role:{operation}:{roleId}"
     */
    public String roleLock(String operation, Long roleId) {
        return generateKey("role", operation, roleId.toString());
    }

    /**
     * 生成角色相關操作的鎖鍵（帶租戶）
     *
     * @param tenantId 租戶 ID
     * @param operation 操作名稱
     * @param roleId   角色 ID
     * @return 鎖鍵格式："{prefix}:lock:{tenantId}:role:{operation}:{roleId}"
     */
    public String roleLock(String tenantId, String operation, Long roleId) {
        return generateKey(tenantId, "role", operation, roleId.toString());
    }

    /**
     * 生成權限相關操作的鎖鍵
     *
     * @param operation   操作名稱
     * @param permissionId 權限 ID
     * @return 鎖鍵格式："{prefix}:lock:permission:{operation}:{permissionId}"
     */
    public String permissionLock(String operation, Long permissionId) {
        return generateKey("permission", operation, permissionId.toString());
    }

    /**
     * 生成權限相關操作的鎖鍵（帶租戶）
     *
     * @param tenantId     租戶 ID
     * @param operation    操作名稱
     * @param permissionId 權限 ID
     * @return 鎖鍵格式："{prefix}:lock:{tenantId}:permission:{operation}:{permissionId}"
     */
    public String permissionLock(String tenantId, String operation, Long permissionId) {
        return generateKey(tenantId, "permission", operation, permissionId.toString());
    }

    /**
     * 生成資源分配操作的鎖鍵
     *
     * @param resourceType 資源類型（例如："role_permission", "user_role"）
     * @param sourceId     來源資源 ID
     * @param targetId     目標資源 ID
     * @return 鎖鍵格式："{prefix}:lock:{resourceType}:assign:{sourceId}_{targetId}"
     */
    public String assignmentLock(String resourceType, Long sourceId, Long targetId) {
        String resource = sourceId + "_" + targetId;
        return generateKey(resourceType, "assign", resource);
    }

    /**
     * 生成資源分配操作的鎖鍵（帶租戶）
     *
     * @param tenantId     租戶 ID
     * @param resourceType 資源類型
     * @param sourceId     來源資源 ID
     * @param targetId     目標資源 ID
     * @return 鎖鍵格式："{prefix}:lock:{tenantId}:{resourceType}:assign:{sourceId}_{targetId}"
     */
    public String assignmentLock(String tenantId, String resourceType, Long sourceId, Long targetId) {
        String resource = sourceId + "_" + targetId;
        return generateKey(tenantId, resourceType, "assign", resource);
    }

    /**
     * 生成全域操作的鎖鍵（不帶租戶，適用於系統級操作）
     *
     * @param operation 操作名稱
     * @param resource  資源識別符
     * @return 鎖鍵格式："{prefix}:lock:global:{operation}:{resource}"
     */
    public String globalLock(String operation, String resource) {
        validateParameters("global", operation, resource);
        String prefix = redisProperties.getKeyPrefix();
        return String.format("%s:lock:global:%s:%s", prefix, operation, resource);
    }

    /**
     * 驗證參數
     */
    private void validateParameters(String module, String operation, String resource) {
        if (!StringUtils.hasText(module)) {
            throw new IllegalArgumentException("Module cannot be null or empty");
        }
        if (!StringUtils.hasText(operation)) {
            throw new IllegalArgumentException("Operation cannot be null or empty");
        }
        if (!StringUtils.hasText(resource)) {
            throw new IllegalArgumentException("Resource cannot be null or empty");
        }
    }
}