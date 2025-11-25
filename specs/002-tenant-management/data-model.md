# 資料模型設計 - 租戶管理模組

> **版本**: 1.0.0
> **更新日期**: 2025-01-XX
> **狀態**: Phase 1 設計

---

## 概述

本文件定義租戶管理模組的完整資料模型，包括實體設計、DTO 結構、資料庫 Schema、以及狀態轉換規則。

---

## 核心實體設計

### Tenant 實體

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenants")
public class Tenant extends TenantEntity {
    
    /**
     * 租戶名稱（唯一）
     * 用於顯示和識別，不可重複
     */
    @TableField(value = "name")
    @NotBlank(message = "租戶名稱不可為空")
    @Size(min = 2, max = 100, message = "租戶名稱長度須在 2-100 字元之間")
    private String name;
    
    /**
     * 聯絡人郵箱（唯一）
     * 用於重要通知和帳戶管理
     */
    @TableField(value = "contact_email")
    @NotBlank(message = "聯絡郵箱不可為空")
    @Email(message = "郵箱格式不正確")
    private String contactEmail;
    
    /**
     * 方案類型
     * FREE: 免費版, BASIC: 基礎版, PRO: 專業版, ENTERPRISE: 企業版
     */
    @TableField(value = "plan_type")
    @NotNull(message = "方案類型不可為空")
    private PlanType planType;
    
    /**
     * 租戶狀態
     * ACTIVE: 啟用, SUSPENDED: 暫停, INACTIVE: 停用
     */
    @TableField(value = "status")
    @NotNull(message = "租戶狀態不可為空")
    private TenantStatus status;
    
    /**
     * 備註說明
     */
    @TableField(value = "description")
    @Size(max = 500, message = "備註長度不可超過 500 字元")
    private String description;
    
    // 繼承自 TenantEntity:
    // - Long tenantId (自身租戶 ID)
    // - LocalDateTime createdAt
    // - Long createdBy
    // - LocalDateTime updatedAt
    // - Long updatedBy
    // - Boolean deleted (@TableLogic)
}
```

### 枚舉類型

#### PlanType (方案類型)

```java
@Getter
@AllArgsConstructor
public enum PlanType {
    FREE("免費版", 10),
    BASIC("基礎版", 50),
    PRO("專業版", 200),
    ENTERPRISE("企業版", -1); // -1 表示無限制
    
    private final String description;
    private final int maxUsers; // 最大使用者數
}
```

#### TenantStatus (租戶狀態)

```java
@Getter
@AllArgsConstructor
public enum TenantStatus {
    ACTIVE("啟用"),
    SUSPENDED("暫停"),
    INACTIVE("停用");
    
    private final String description;
}
```

---

## DTO 設計

### 請求 DTO

#### CreateTenantRequest

```java
@Data
@Schema(description = "建立租戶請求")
public class CreateTenantRequest {
    
    @Schema(description = "租戶名稱", example = "Acme Corporation", required = true)
    @NotBlank(message = "租戶名稱不可為空")
    @Size(min = 2, max = 100, message = "租戶名稱長度須在 2-100 字元之間")
    private String name;
    
    @Schema(description = "聯絡人郵箱", example = "admin@acme.com", required = true)
    @NotBlank(message = "聯絡郵箱不可為空")
    @Email(message = "郵箱格式不正確")
    private String contactEmail;
    
    @Schema(description = "方案類型", example = "BASIC", required = true, 
            allowableValues = {"FREE", "BASIC", "PRO", "ENTERPRISE"})
    @NotNull(message = "方案類型不可為空")
    private PlanType planType;
    
    @Schema(description = "備註說明", example = "重要客戶")
    @Size(max = 500, message = "備註長度不可超過 500 字元")
    private String description;
}
```

#### UpdateTenantRequest

```java
@Data
@Schema(description = "更新租戶請求")
public class UpdateTenantRequest {
    
    @Schema(description = "租戶名稱", example = "Acme Corporation Updated")
    @Size(min = 2, max = 100, message = "租戶名稱長度須在 2-100 字元之間")
    private String name;
    
    @Schema(description = "聯絡人郵箱", example = "new-admin@acme.com")
    @Email(message = "郵箱格式不正確")
    private String contactEmail;
    
    @Schema(description = "方案類型", example = "PRO",
            allowableValues = {"FREE", "BASIC", "PRO", "ENTERPRISE"})
    private PlanType planType;
    
    @Schema(description = "租戶狀態", example = "ACTIVE",
            allowableValues = {"ACTIVE", "SUSPENDED", "INACTIVE"})
    private TenantStatus status;
    
    @Schema(description = "備註說明", example = "已升級至專業版")
    @Size(max = 500, message = "備註長度不可超過 500 字元")
    private String description;
}
```

#### QueryTenantRequest

```java
@Data
@Schema(description = "查詢租戶請求")
public class QueryTenantRequest {
    
    @Schema(description = "租戶名稱（模糊搜尋）", example = "Acme")
    private String name;
    
    @Schema(description = "聯絡人郵箱（模糊搜尋）", example = "admin@")
    private String contactEmail;
    
    @Schema(description = "方案類型", example = "BASIC",
            allowableValues = {"FREE", "BASIC", "PRO", "ENTERPRISE"})
    private PlanType planType;
    
    @Schema(description = "租戶狀態", example = "ACTIVE",
            allowableValues = {"ACTIVE", "SUSPENDED", "INACTIVE"})
    private TenantStatus status;
    
    @Schema(description = "頁碼（從 1 開始）", example = "1", defaultValue = "1")
    @Min(value = 1, message = "頁碼須大於 0")
    private Integer pageNum = 1;
    
    @Schema(description = "每頁筆數", example = "20", defaultValue = "20")
    @Min(value = 1, message = "每頁筆數須大於 0")
    @Max(value = 100, message = "每頁筆數不可超過 100")
    private Integer pageSize = 20;
    
    @Schema(description = "排序欄位", example = "created_at", 
            allowableValues = {"created_at", "updated_at", "name"})
    private String orderBy = "created_at";
    
    @Schema(description = "排序方向", example = "desc", 
            allowableValues = {"asc", "desc"})
    private String orderDirection = "desc";
}
```

### 回應 DTO

#### TenantResponse

```java
@Data
@Schema(description = "租戶回應")
public class TenantResponse {
    
    @Schema(description = "租戶 ID", example = "1234567890123456789")
    private Long id;
    
    @Schema(description = "租戶名稱", example = "Acme Corporation")
    private String name;
    
    @Schema(description = "聯絡人郵箱", example = "admin@acme.com")
    private String contactEmail;
    
    @Schema(description = "方案類型", example = "BASIC")
    private PlanType planType;
    
    @Schema(description = "方案類型描述", example = "基礎版")
    private String planTypeDescription;
    
    @Schema(description = "租戶狀態", example = "ACTIVE")
    private TenantStatus status;
    
    @Schema(description = "租戶狀態描述", example = "啟用")
    private String statusDescription;
    
    @Schema(description = "備註說明", example = "重要客戶")
    private String description;
    
    @Schema(description = "建立時間", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "建立人 ID", example = "1000000000000000001")
    private Long createdBy;
    
    @Schema(description = "更新時間", example = "2025-01-16T14:20:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "更新人 ID", example = "1000000000000000001")
    private Long updatedBy;
}
```

#### TenantListResponse

```java
@Data
@Schema(description = "租戶清單回應")
public class TenantListResponse {
    
    @Schema(description = "租戶清單")
    private List<TenantResponse> tenants;
    
    @Schema(description = "總筆數", example = "150")
    private Long total;
    
    @Schema(description = "當前頁碼", example = "1")
    private Integer pageNum;
    
    @Schema(description = "每頁筆數", example = "20")
    private Integer pageSize;
    
    @Schema(description = "總頁數", example = "8")
    private Integer totalPages;
}
```

---

## 資料庫 Schema

### 表結構

```sql
-- 租戶表
CREATE TABLE tenants (
    -- 主鍵（Snowflake ID）
    id BIGINT PRIMARY KEY,
    
    -- 業務欄位
    name VARCHAR(100) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(500),
    
    -- 多租戶欄位（指向自身）
    tenant_id BIGINT NOT NULL,
    
    -- 稽核欄位
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 唯一約束
    CONSTRAINT uk_tenants_name UNIQUE (name),
    CONSTRAINT uk_tenants_contact_email UNIQUE (contact_email),
    
    -- 檢查約束
    CONSTRAINT chk_tenants_plan_type CHECK (plan_type IN ('FREE', 'BASIC', 'PRO', 'ENTERPRISE')),
    CONSTRAINT chk_tenants_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE'))
);

-- 索引
CREATE INDEX idx_tenants_tenant_id ON tenants(tenant_id) WHERE deleted = FALSE;
CREATE INDEX idx_tenants_status ON tenants(status) WHERE deleted = FALSE;
CREATE INDEX idx_tenants_plan_type ON tenants(plan_type) WHERE deleted = FALSE;
CREATE INDEX idx_tenants_created_at ON tenants(created_at DESC);

-- 註解
COMMENT ON TABLE tenants IS '租戶表';
COMMENT ON COLUMN tenants.id IS '租戶 ID（Snowflake）';
COMMENT ON COLUMN tenants.name IS '租戶名稱';
COMMENT ON COLUMN tenants.contact_email IS '聯絡人郵箱';
COMMENT ON COLUMN tenants.plan_type IS '方案類型：FREE/BASIC/PRO/ENTERPRISE';
COMMENT ON COLUMN tenants.status IS '租戶狀態：ACTIVE/SUSPENDED/INACTIVE';
COMMENT ON COLUMN tenants.description IS '備註說明';
COMMENT ON COLUMN tenants.tenant_id IS '多租戶 ID（指向自身）';
COMMENT ON COLUMN tenants.deleted IS '軟刪除標記';
```

### 初始資料

```sql
-- 系統預設租戶（用於系統管理）
INSERT INTO tenants (
    id, name, contact_email, plan_type, status, tenant_id, 
    created_by, updated_by, deleted
) VALUES (
    1, 'System', 'system@rbac.local', 'ENTERPRISE', 'ACTIVE', 1,
    1, 1, FALSE
);
```

---

## 實體關係

```
┌─────────────────────────────────────────┐
│              Tenant                      │
├─────────────────────────────────────────┤
│ PK: id (BIGINT)                         │
│ UK: name, contact_email                 │
├─────────────────────────────────────────┤
│ name (VARCHAR 100) NOT NULL             │
│ contact_email (VARCHAR 255) NOT NULL    │
│ plan_type (ENUM) NOT NULL               │
│ status (ENUM) NOT NULL                  │
│ description (VARCHAR 500)               │
│ tenant_id (BIGINT) NOT NULL (自身)      │
│ created_at, created_by                  │
│ updated_at, updated_by                  │
│ deleted (BOOLEAN)                       │
└─────────────────────────────────────────┘
          │
          │ 1:N (未來擴展)
          ▼
┌─────────────────────────────────────────┐
│              User                        │
│         (未來實作)                       │
└─────────────────────────────────────────┘
```

**說明**:
- `tenant_id` 指向自身的 `id`，實現自我隔離
- 未來 User 實體將透過 `tenant_id` 關聯到 Tenant
- 所有查詢自動透過 MyBatis 攔截器過濾 `tenant_id`

---

## 狀態轉換規則

### 租戶狀態機

```
          建立
           │
           ▼
       ┌────────┐
   ┌───│ ACTIVE │◄───┐
   │   └────────┘    │
   │        │         │
暫停│        │停用    │恢復
   │        ▼         │
   │   ┌──────────┐  │
   └──►│SUSPENDED │──┘
       └──────────┘
            │
            │停用
            ▼
       ┌──────────┐
       │ INACTIVE │
       └──────────┘
            │
            │軟刪除
            ▼
       [deleted=true]
```

### 狀態轉換矩陣

| 當前狀態 | 可轉換至 | 觸發條件 | 驗證規則 |
|---------|---------|---------|---------|
| ACTIVE | SUSPENDED | 管理員暫停 | 無特殊限制 |
| ACTIVE | INACTIVE | 管理員停用 | 確認無活躍使用者 |
| SUSPENDED | ACTIVE | 管理員恢復 | 無特殊限制 |
| SUSPENDED | INACTIVE | 管理員停用 | 確認無活躍使用者 |
| INACTIVE | ACTIVE | 管理員啟用 | 確認帳戶狀態正常 |
| ANY | DELETED | 管理員刪除 | 軟刪除，保留資料 |

### 業務規則

1. **建立租戶**: 預設狀態為 `ACTIVE`
2. **暫停租戶**: 暫時禁止登入，保留資料
3. **停用租戶**: 長期停用，可重新啟用
4. **刪除租戶**: 軟刪除，不可恢復（資料保留用於稽核）
5. **恢復租戶**: 僅 `SUSPENDED` 和 `INACTIVE` 可恢復至 `ACTIVE`

---

## 驗證規則

### 欄位驗證

| 欄位 | 規則 | 錯誤訊息 |
|-----|------|---------|
| name | NOT NULL, 2-100 字元 | 租戶名稱不可為空，長度須在 2-100 字元之間 |
| contact_email | NOT NULL, Email 格式 | 聯絡郵箱不可為空，郵箱格式不正確 |
| plan_type | NOT NULL, 枚舉值 | 方案類型不可為空，須為有效值 |
| status | NOT NULL, 枚舉值 | 租戶狀態不可為空，須為有效值 |
| description | 0-500 字元 | 備註長度不可超過 500 字元 |

### 業務驗證

1. **唯一性檢查**:
   - `name` 必須唯一（區分大小寫）
   - `contact_email` 必須唯一（不區分大小寫）

2. **狀態轉換檢查**:
   - 驗證目標狀態是否合法
   - 檢查是否滿足轉換前提條件

3. **刪除檢查**:
   - 確認租戶下無活躍使用者
   - 記錄刪除操作至稽核日誌

---

## 效能考量

### 索引策略

1. **主鍵索引**: `id` (自動建立)
2. **唯一索引**: `name`, `contact_email`
3. **過濾索引**: `tenant_id`, `status`, `plan_type` (WHERE deleted = FALSE)
4. **排序索引**: `created_at DESC`

### 查詢優化

1. **分頁查詢**: 使用 `LIMIT` 和 `OFFSET`
2. **條件過濾**: 使用索引欄位進行過濾
3. **軟刪除過濾**: 所有查詢自動加上 `deleted = FALSE`
4. **多租戶過濾**: MyBatis 攔截器自動注入 `tenant_id` 條件

### 快取策略

1. **租戶資訊快取**: Redis, TTL = 1 小時
2. **快取鍵格式**: `tenant:{id}`
3. **快取更新**: 寫入時主動失效
4. **快取預熱**: 系統啟動時載入熱點租戶

---

## 安全性考量

### 資料隔離

1. **自動租戶過濾**: MyBatis 攔截器確保查詢僅返回當前租戶資料
2. **ThreadLocal 清理**: 請求完成後自動清理租戶上下文
3. **跨租戶防護**: 禁止跨租戶查詢或修改

### 輸入驗證

1. **參數驗證**: 使用 Jakarta Validation (@NotNull, @Size, @Email)
2. **SQL 注入防護**: MyBatis 參數綁定
3. **XSS 防護**: 輸出時自動轉義

### 稽核日誌

1. **自動記錄**: `created_by`, `created_at`, `updated_by`, `updated_at`
2. **操作記錄**: 重要操作寫入稽核表（未來實作）
3. **刪除記錄**: 軟刪除保留資料用於稽核

---

## 擴展性設計

### 未來擴展點

1. **租戶配額管理**: 根據 `plan_type` 限制使用者數、儲存空間等
2. **租戶統計資訊**: 使用者數、資料量、API 調用次數
3. **租戶分層**: 支援父子租戶關係
4. **租戶自訂設定**: 主題、語言、功能開關

### 相容性保證

1. **向後相容**: 新增欄位使用 `DEFAULT` 值
2. **版本控制**: 使用 Flyway 管理資料庫遷移
3. **軟刪除**: 刪除操作不破壞歷史資料

---

## 總結

租戶管理模組的資料模型設計遵循以下原則:

1. ✅ **簡潔**: 僅包含核心欄位，避免過度設計
2. ✅ **安全**: 多租戶隔離、輸入驗證、稽核日誌
3. ✅ **高效**: 合理的索引策略、快取策略
4. ✅ **可擴展**: 預留擴展點，向後相容
5. ✅ **可測試**: 清晰的驗證規則、狀態轉換規則

下一步將在 `contracts/` 中定義 API 規格。
