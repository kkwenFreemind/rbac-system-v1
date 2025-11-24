package com.rbac.common.database.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 稽核實體類別
 * 擴展 TenantEntity 並包含稽核軌跡欄位（誰和何時）。
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditEntity extends TenantEntity {

    /**
     * 創建者ID - 透過 MetaObjectHandler 自動填充
     */
    @TableField(fill = FieldFill.INSERT)
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    /**
     * 創建時間 - 透過 MetaObjectHandler 自動填充
     */
    @CreatedDate
    @TableField(fill = FieldFill.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最後更新者ID - 透過 MetaObjectHandler 自動填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 最後更新時間 - 透過 MetaObjectHandler 自動填充
     */
    @LastModifiedDate
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}