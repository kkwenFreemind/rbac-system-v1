package com.rbac.common.database.entity;

import com.rbac.common.core.exception.TenantException;
import com.rbac.common.database.context.TenantContextHolder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

/**
 * 租戶實體類別
 * 所有租戶範圍實體的基礎類別，確保租戶隔離。
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {

    /**
     * Tenant ID for multi-tenancy isolation
     * Automatically injected by TenantInterceptor
     */
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    /**
     * Pre-persist hook to validate tenant context
     */
    @PrePersist
    public void prePersist() {
        if (tenantId == null) {
            String contextTenantId = TenantContextHolder.getTenantId();
            if (contextTenantId == null) {
                throw new TenantException("Tenant context not set during entity creation");
            }
            tenantId = Long.parseLong(contextTenantId);
        }
    }
}