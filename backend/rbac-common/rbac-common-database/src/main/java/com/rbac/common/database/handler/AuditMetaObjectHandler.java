package com.rbac.common.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.rbac.common.core.context.UserContext;
import com.rbac.common.database.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自動填充處理器
 *
 * 負責自動填充實體的公共欄位：
 * - tenant_id：從 TenantContextHolder 獲取
 * - created_by/created_at：插入時自動填充
 * - updated_by/updated_at：插入和更新時自動填充
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Component
@Slf4j
public class AuditMetaObjectHandler implements MetaObjectHandler {

    /**
     * 使用者上下文介面 - 可選注入，因為 Common Layer 不依賴業務模組
     * 如果沒有 Auth 模組實作此介面，則審計欄位將為 null
     */
    @Autowired(required = false)
    private UserContext userContext;

    /**
     * 插入操作的自動填充
     *
     * @param metaObject 元物件
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("開始處理插入操作的自動填充");

        LocalDateTime now = LocalDateTime.now();

        // 1. 填充租戶 ID（如果欄位存在）
        fillTenantIdIfPresent(metaObject);

        // 2. 填充創建時間
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);

        // 3. 填充創建者（如果有使用者上下文）
        if (userContext != null && userContext.isAuthenticated()) {
            Long userId = userContext.getCurrentUserId();
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            log.debug("插入操作：設定 createdBy = {}", userId);
        } else {
            log.debug("插入操作：沒有使用者上下文，createdBy 保持為 null");
        }

        // 4. 填充更新時間和更新者（與創建時相同）
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        if (userContext != null && userContext.isAuthenticated()) {
            Long userId = userContext.getCurrentUserId();
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
            log.debug("插入操作：設定 updatedBy = {}", userId);
        }

        log.debug("插入操作自動填充完成");
    }

    /**
     * 更新操作的自動填充
     *
     * @param metaObject 元物件
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("開始處理更新操作的自動填充");

        LocalDateTime now = LocalDateTime.now();

        // 1. 填充更新時間
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 2. 填充更新者（如果有使用者上下文）
        if (userContext != null && userContext.isAuthenticated()) {
            Long userId = userContext.getCurrentUserId();
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
            log.debug("更新操作：設定 updatedBy = {}", userId);
        } else {
            log.debug("更新操作：沒有使用者上下文，updatedBy 保持不變");
        }

        log.debug("更新操作自動填充完成");
    }

    /**
     * 填充租戶 ID（如果實體有此欄位）
     *
     * @param metaObject 元物件
     */
    private void fillTenantIdIfPresent(MetaObject metaObject) {
        // 檢查實體是否有 tenantId 欄位
        if (metaObject.hasGetter("tenantId") || metaObject.hasGetter("tenant_id")) {
            String tenantId = TenantContextHolder.getTenantId();
            if (tenantId != null) {
                try {
                    Long tenantIdLong = Long.parseLong(tenantId);
                    // 嘗試填充 tenantId（駝峰命名）
                    this.strictInsertFill(metaObject, "tenantId", Long.class, tenantIdLong);
                    // 也嘗試填充 tenant_id（下劃線命名）
                    this.strictInsertFill(metaObject, "tenant_id", Long.class, tenantIdLong);
                    log.debug("自動填充 tenantId = {}", tenantIdLong);
                } catch (NumberFormatException e) {
                    log.error("無效的租戶 ID 格式: {}", tenantId, e);
                    throw new IllegalArgumentException("無效的租戶 ID 格式: " + tenantId);
                }
            } else {
                log.warn("租戶上下文未設定，但在嘗試填充 tenantId。實體：{}", metaObject.getOriginalObject().getClass().getSimpleName());
            }
        }
    }
}