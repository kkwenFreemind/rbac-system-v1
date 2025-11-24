package com.rbac.common.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * 基礎實體類別
 * 所有資料庫實體的基礎類別，提供通用欄位和稽核軌跡。
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
@Data
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵ID - 使用雪花算法生成
     * 注意: 使用 ASSIGN_ID 而非 IDENTITY 以支持分布式部署和數據遷移
     * MyBatis-Plus 會自動使用雪花算法生成全局唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 軟刪除標記
     * 0 = 未刪除, 1 = 已刪除
     */
    @TableLogic
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version")
    private Integer version = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}