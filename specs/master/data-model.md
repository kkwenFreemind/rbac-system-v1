# 數據模型：001-common-layer

**日期**: 2025-11-24 | **階段**: Phase 1 - 設計與契約

## 概述

本文檔定義了公共層的數據模型和實體。這些模型作為多租戶 RBAC 系統中所有業務模組的基礎。

## 基礎實體類別

### 1. BaseEntity

**目的**: 所有資料庫實體的基礎類別，提供通用欄位和稽核軌跡。

**欄位**:

| 欄位 | 類型 | 必填 | 描述 | 驗證 |
|-------|------|----------|-------------|------------|
| id | Long | 是 | 主鍵，自動生成 | 正數 |
| deleted | Boolean | 是 | 軟刪除標記 | 預設: false |
| version | Integer | 否 | 樂觀鎖版本 | 預設: 0 |

**Java 定義**:

```java
@Data
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    
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
```

**使用範例**:

```java
@Entity
@Table(name = "sys_user")
public class User extends TenantEntity {
    private String username;
    private String email;
    // ... 其他欄位
}
```

---

### 2. TenantEntity

**目的**: 所有租戶範圍實體的基礎類別，確保租戶隔離。

**欄位** (繼承自 BaseEntity):

| 欄位 | 類型 | 必填 | 描述 | 驗證 |
|-------|------|----------|-------------|------------|
| tenantId | Long | 是 | 租戶識別符 | 必須匹配當前租戶上下文 |

**Java 定義**:

```java
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
```

**資料庫約束**:

```sql
-- 所有租戶範圍的表格必須有此索引
CREATE INDEX idx_tenant_id ON table_name(tenant_id);

-- 對於唯一約束，始終包含 tenant_id
CREATE UNIQUE INDEX uk_tenant_username ON sys_user(tenant_id, username);
```

**使用範例**:

```java
@Entity
@Table(name = "sys_role")
public class Role extends TenantEntity {
    private String roleCode;
    private String roleName;
    // tenantId is inherited and auto-populated
}
```

---

### 2.5. UserContext 介面（解耦機制）

**目的**: 提供使用者上下文抽象介面，解決 Common Layer 與 Auth/Security 模組的循環依賴問題。

**設計原則**: Common Layer 不能依賴業務模組（如 Auth），但需要獲取當前使用者資訊用於審計。透過定義介面，Common Layer 僅依賴抽象，Auth 模組稍後實作此介面。

**Java 定義**:

```java
package com.rbac.common.core.context;

/**
 * 使用者上下文介面
 * 
 * 此介面由 Auth/Security 模組實作，Common Layer 僅依賴此抽象。
 * 用於獲取當前請求的使用者資訊，主要用於審計追蹤。
 * 
 * 實作注意事項:
 * 1. 實作類應該從 SecurityContext、JWT Token 或 Session 中提取使用者資訊
 * 2. 如果無法獲取使用者（如匿名請求），應返回 null 或系統預設值
 * 3. 實作類必須註冊為 Spring Bean
 * 
 * @author RBAC System
 * @since 1.0.0
 */
public interface UserContext {
    
    /**
     * 獲取當前使用者ID
     * 
     * @return 使用者ID，如果未登入則返回 null
     */
    Long getCurrentUserId();
    
    /**
     * 獲取當前使用者名稱
     * 
     * @return 使用者名稱，如果未登入則返回 null
     */
    String getCurrentUsername();
    
    /**
     * 檢查是否已認證
     * 
     * @return true 如果使用者已登入
     */
    default boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}
```

**使用範例**（在 Auth 模組中實作）:

```java
package com.rbac.auth.security;

import com.rbac.common.core.context.UserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * UserContext 實作 - 從 Spring Security 中提取使用者資訊
 */
@Component
public class SecurityUserContext implements UserContext {
    
    @Override
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails user = (UserDetails) auth.getPrincipal();
            return user.getUserId();
        }
        return null;
    }
    
    @Override
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
```

**依賴倒置原理**:

- Common Layer 定義介面（高層策略）
- Auth Module 提供實作（低層細節）
- 符合依賴倒置原則（DIP）和開閉原則（OCP）

---

### 3. AuditEntity

**目的**: 擴展 TenantEntity 並包含稽核軌跡欄位（誰和何時）。透過 UserContext 介面實現與 Auth 模組的解耦。

**欄位** (繼承自 TenantEntity):

| 欄位 | 類型 | 必填 | 描述 | 驗證 |
|-------|------|----------|-------------|------------|
| createdBy | Long | 否 | 創建此記錄的使用者 ID | 使用者必須存在 |
| createdAt | LocalDateTime | 是 | 創建時間戳 | 自動生成 |
| updatedBy | Long | 否 | 最後更新此記錄的使用者 ID | 使用者必須存在 |
| updatedAt | LocalDateTime | 是 | 最後更新時間戳 | 自動更新 |

**Java 定義**:

```java
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
    
    /**
     * 審計字段自動填充處理器
     * 
     * 注意: 此類需要訪問 UserContext 介面來獲取當前使用者
     * UserContext 由 Auth 模組實作並注入
     * 
     * 位置: com.rbac.common.database.handler.AuditMetaObjectHandler
     */
    @Component
    public static class AuditMetaObjectHandler implements MetaObjectHandler {
        
        @Autowired(required = false) // required=false 防止啟動時找不到實作
        private UserContext userContext;
        
        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            
            // 填充時間字段
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
            
            // 填充使用者字段（透過 UserContext 介面）
            if (userContext != null && userContext.isAuthenticated()) {
                Long userId = userContext.getCurrentUserId();
                this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
                this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
            }
        }
        
        @Override
        public void updateFill(MetaObject metaObject) {
            // 填充更新時間
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            
            // 填充更新者（透過 UserContext 介面）
            if (userContext != null && userContext.isAuthenticated()) {
                Long userId = userContext.getCurrentUserId();
                this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
            }
        }
    }
}
```

**依賴關係圖**:

```text
┌─────────────────────────┐
│  Common Core Module     │
│  - UserContext (介面)   │  ← 高層策略（抽象）
└─────────┬───────────────┘
          │ depends on
          │
┌─────────▼───────────────┐
│  Common Database Module │
│  - AuditMetaObjectHandler│  ← 使用 UserContext 介面
│  - AuditEntity           │
└─────────────────────────┘
          ▲ implements
          │
┌─────────┴───────────────┐
│  Auth/Security Module   │
│  - SecurityUserContext   │  ← 低層實作（具體）
│  (implements UserContext)│
└─────────────────────────┘
```

**關鍵設計點**:

1. **無循環依賴**: Common 層定義介面，Auth 層實作介面
2. **可選注入**: `@Autowired(required = false)` 允許系統在沒有 Auth 模組時啟動
3. **自動填充**: MyBatis-Plus 的 `MetaObjectHandler` 自動處理 INSERT/UPDATE
4. **執行緒安全**: UserContext 實作應使用 ThreadLocal 或 SecurityContext

**使用範例**:

```java
@Entity
@Table(name = "sys_permission")
public class Permission extends AuditEntity {
    private String permissionCode;
    private String permissionName;
    // 繼承: id, tenantId, deleted, createdBy, createdAt, updatedBy, updatedAt
}
```

---

## 數據傳輸物件 (DTOs)

### 4. Result<T>

**目的**: 統一的 API 響應包裝器，用於一致的客戶端處理。

**欄位**:

| 欄位 | 類型 | 必填 | 描述 |
|-------|------|----------|-------------|
| code | String | 是 | 響應碼（成功或錯誤碼） |
| message | String | 是 | 人類可讀的消息 |
| data | T | 否 | 響應載荷（泛型類型） |
| timestamp | Long | 是 | 響應時間戳（Unix epoch ms） |

**Java 定義**:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result<T> {
    
    /**
     * 響應碼
     * "200" = 成功
     * 錯誤碼遵循模式: [模組]-[類型]-[序列]
     */
    private String code;
    
    /**
     * 響應消息
     */
    private String message;
    
    /**
     * 響應數據（錯誤響應或無返回值操作為 null）
     */
    private T data;
    
    /**
     * 響應時間戳（Unix epoch 毫秒）
     */
    private Long timestamp;
    
    // ============= 靜態工廠方法 =============
    
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(ResultCode.SUCCESS)
            .message("Success")
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> Result<T> success() {
        return success(null);
    }
    
    public static <T> Result<T> error(String code, String message) {
        return Result.<T>builder()
            .code(code)
            .message(message)
            .data(null)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> Result<T> error(String message) {
        return error(ResultCode.ERROR, message);
    }
    
    // ============= 輔助方法 =============
    
    public boolean isSuccess() {
        return ResultCode.SUCCESS.equals(code);
    }
    
    public boolean isError() {
        return !isSuccess();
    }
}
```

**響應範例**:

```json
// 成功並帶有數據
{
  "code": "200",
  "message": "Success",
  "data": {
    "id": 123,
    "username": "john_doe"
  },
  "timestamp": 1700000000000
}

// 成功但無數據（例如刪除操作）
{
  "code": "200",
  "message": "Success",
  "data": null,
  "timestamp": 1700000000000
}

// 錯誤響應
{
  "code": "00-1-001",
  "message": "Validation failed: username cannot be empty",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5. PageRequest

**目的**: 封裝列表查詢的分頁參數。

**欄位**:

| 欄位 | 類型 | 必填 | 預設值 | 描述 | 驗證 |
|-------|------|----------|---------|-------------|------------|
| pageNum | Integer | 否 | 1 | 當前頁碼（1 起始） | >= 1 |
| pageSize | Integer | 否 | 10 | 每頁記錄數 | 1 <= size <= 100 |
| sortBy | String | 否 | null | 排序欄位名稱 | 必須是有效欄位 |
| sortOrder | String | 否 | "ASC" | 排序方向 | "ASC" 或 "DESC" |

**Java 定義**:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    /**
     * 當前頁碼（1 起始）
     */
    @Min(value = 1, message = "Page number must be >= 1")
    @Builder.Default
    private Integer pageNum = 1;
    
    /**
     * 每頁記錄數
     */
    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    @Builder.Default
    private Integer pageSize = 10;
    
    /**
     * 排序欄位名稱（例如 "createdAt", "username"）
     */
    private String sortBy;
    
    /**
     * 排序方向: ASC 或 DESC
     */
    @Pattern(regexp = "ASC|DESC", message = "Sort order must be ASC or DESC")
    @Builder.Default
    private String sortOrder = "ASC";
    
    // ============= 輔助方法 =============
    
    /**
     * 計算 SQL LIMIT 子句的偏移量
     * @return 偏移值
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 檢查是否啟用排序
     */
    public boolean hasSort() {
        return sortBy != null && !sortBy.isEmpty();
    }
}
```

**使用範例**:

```java
@GetMapping("/users")
public Result<PageResponse<UserDTO>> listUsers(
    @Valid @ModelAttribute PageRequest pageRequest,
    @RequestParam(required = false) String username
) {
    PageResponse<UserDTO> users = userService.listUsers(pageRequest, username);
    return Result.success(users);
}
```

---

### 6. PageResponse<T>

**目的**: 封裝帶有元數據的分頁查詢結果。

**欄位**:

| 欄位 | 類型 | 必填 | 描述 |
|-------|------|----------|-------------|
| records | List<T> | 是 | 當前頁記錄 |
| total | Long | 是 | 總記錄數 |
| pageNum | Integer | 是 | 當前頁碼 |
| pageSize | Integer | 是 | 每頁記錄數 |
| totalPages | Integer | 是 | 總頁數 |
| hasNext | Boolean | 是 | 是否有下一頁 |
| hasPrevious | Boolean | 是 | 是否有上一頁 |

**Java 定義**:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    
    /**
     * 當前頁記錄
     */
    private List<T> records;
    
    /**
     * 總記錄數
     */
    private Long total;
    
    /**
     * 當前頁碼（1 起始）
     */
    private Integer pageNum;
    
    /**
     * 每頁記錄數
     */
    private Integer pageSize;
    
    /**
     * 總頁數
     */
    private Integer totalPages;
    
    /**
     * 是否有下一頁
     */
    private Boolean hasNext;
    
    /**
     * 是否有上一頁
     */
    private Boolean hasPrevious;
    
    // ============= 靜態工廠方法 =============
    
    public static <T> PageResponse<T> of(List<T> records, Long total, PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        return PageResponse.<T>builder()
            .records(records)
            .total(total)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .totalPages(totalPages)
            .hasNext(pageNum < totalPages)
            .hasPrevious(pageNum > 1)
            .build();
    }
    
    public static <T> PageResponse<T> empty(PageRequest pageRequest) {
        return of(Collections.emptyList(), 0L, pageRequest);
    }
}
```

**響應範例**:

```json
{
  "code": "200",
  "message": "Success",
  "data": {
    "records": [
      {"id": 1, "username": "user1"},
      {"id": 2, "username": "user2"}
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": 1700000000000
}
```

---

## 常量和枚舉

### 7. ResultCode

**目的**: 定義標準響應碼。

```java
public class ResultCode {
    // 成功
    public static final String SUCCESS = "200";
    
    // 通用錯誤
    public static final String ERROR = "500";
    public static final String VALIDATION_ERROR = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    
    // 業務錯誤（模組特定代碼在各自模組中定義）
    // 通用模組: 00-xxx
    // 認證模組: 01-xxx
    // 用戶模組: 02-xxx
    // 租戶模組: 03-xxx
    // 等等
}
```

---

### 8. TenantIsolationLevel

**目的**: 定義租戶隔離策略。

```java
public enum TenantIsolationLevel {
    /**
     * 行級隔離: 共享表帶有 tenant_id 欄位
     */
    ROW("ROW", "行級隔離"),
    
    /**
     * 架構級隔離: 每個租戶單獨的資料庫架構
     */
    SCHEMA("SCHEMA", "架構級隔離"),
    
    /**
     * 資料庫級隔離: 每個租戶單獨的物理資料庫
     */
    DATABASE("DATABASE", "資料庫級隔離");
    
    private final String code;
    private final String description;
    
    TenantIsolationLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    // Getters...
}
```

---

## 實體關係圖

```
┌─────────────────┐
│   BaseEntity    │ (Abstract)
│─────────────────│
│ - id: Long      │
│ - deleted: Bool │
│ - version: Int  │
└────────┬────────┘
         │
         │ extends
         │
┌────────▼────────┐
│  TenantEntity   │ (Abstract)
│─────────────────│
│ - tenantId: Long│
└────────┬────────┘
         │
         │ extends
         │
┌────────▼────────┐
│  AuditEntity    │ (Abstract)
│─────────────────│
│ - createdBy     │
│ - createdAt     │
│ - updatedBy     │
│ - updatedAt     │
└─────────────────┘
         │
         │ extends
         ▼
  [Business Entities]
  (User, Role, Permission, etc.)
```

---

## 驗證規則

### 欄位級驗證

| 實體 | 欄位 | 驗證規則 |
|--------|-------|------------------|
| **所有實體** | id | 自動生成，正數，唯一 |
| **TenantEntity** | tenantId | 必填，必須匹配上下文，建立後不可變 |
| **AuditEntity** | createdAt | 自動生成，不可變 |
| **AuditEntity** | updatedAt | 修改時自動更新 |

### 業務規則

1. **租戶隔離**: 所有對 `TenantEntity` 子類的查詢必須包含 `tenant_id` 過濾器（由 MyBatis 攔截器強制執行）

2. **軟刪除**: 已刪除記錄（`deleted = true`）預設從查詢中排除（由 MyBatis `@TableLogic` 強制執行）

3. **樂觀鎖定**: 版本欄位防止並發更新衝突（由 MyBatis-Plus `@Version` 強制執行）

4. **審計追蹤**: `createdBy`、`createdAt`、`updatedBy`、`updatedAt` 自動管理（業務邏輯中永遠不手動設定）

---

## 索引設計指南

### 所有表必需的索引

```sql
-- 1. 主鍵（自動建立）
CREATE PRIMARY KEY ON table_name(id);

-- 2. 租戶 ID 索引（行級隔離關鍵）
CREATE INDEX idx_tenant_id ON table_name(tenant_id);

-- 3. 軟刪除 + 租戶複合索引
CREATE INDEX idx_tenant_deleted ON table_name(tenant_id, deleted);

-- 4. 唯一約束必須包含 tenant_id
CREATE UNIQUE INDEX uk_tenant_unique_field ON table_name(tenant_id, unique_field);
```

### 效能考量

- **tenant_id 必須是所有複合索引的第一欄** 以進行查詢優化
- `deleted` 欄位的索引改善軟刪除查詢效能
- 經常查詢的欄位（例如 `status`、`created_at`）應該有索引

---

## 遷移策略

### 向基礎實體新增欄位

當向 `BaseEntity`、`TenantEntity` 或 `AuditEntity` 新增欄位時：

1. **向抽象類新增欄位**（例如向 `AuditEntity` 新增 `clientIp`）
2. **為所有使用該基礎類的表生成遷移腳本**
3. **更新現有數據**（如果欄位不可為空，提供預設值）
4. **更新文檔**（此檔案和資料庫設計文檔）

### 遷移範例

```sql
-- 向所有啟用審計的表新增審計欄位
ALTER TABLE sys_user ADD COLUMN client_ip VARCHAR(64);
ALTER TABLE sys_role ADD COLUMN client_ip VARCHAR(64);
ALTER TABLE sys_permission ADD COLUMN client_ip VARCHAR(64);
```

---

## Web層上下文管理

### 9. TraceContext (Trace ID 管理)

**目的**: 提供請求追蹤ID的生成和傳遞機制,用於日誌串聯和分散式追蹤。

**位置**: `com.rbac.common.web.context.TraceContext`

**Java 定義**:

```java
package com.rbac.common.web.context;

import org.slf4j.MDC;
import java.util.UUID;

/**
 * Trace ID 上下文管理器
 * 
 * 使用 SLF4J MDC (Mapped Diagnostic Context) 存儲 Trace ID
 * Trace ID 會自動傳遞到所有日誌輸出中
 */
public class TraceContext {
    
    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    
    /**
     * 生成新的 Trace ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 設置 Trace ID 到 MDC
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }
    
    /**
     * 獲取當前 Trace ID
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * 清除 Trace ID
     * 必須在請求結束時調用以防止線程池污染
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
    
    /**
     * 獲取 Trace ID Header 名稱
     */
    public static String getTraceIdHeader() {
        return TRACE_ID_HEADER;
    }
}
```

**TraceIdFilter 實作**:

```java
package com.rbac.common.web.filter;

import com.rbac.common.web.context.TraceContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Trace ID 過濾器
 * 
 * 優先級最高,在所有過濾器之前執行
 * 從請求頭提取或生成 Trace ID,並設置到 MDC
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // 從請求頭提取 Trace ID,如果不存在則生成新的
            String traceId = httpRequest.getHeader(TraceContext.getTraceIdHeader());
            if (traceId == null || traceId.isEmpty()) {
                traceId = TraceContext.generateTraceId();
            }
            
            // 設置到 MDC
            TraceContext.setTraceId(traceId);
            
            // 將 Trace ID 添加到響應頭
            httpResponse.setHeader(TraceContext.getTraceIdHeader(), traceId);
            
            // 繼續過濾鏈
            chain.doFilter(request, response);
        } finally {
            // 清理 MDC,防止線程池污染
            TraceContext.clear();
        }
    }
}
```

**Logback 配置**:

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- 在日誌格式中包含 traceId -->
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/rbac-system.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

**使用範例**:

```java
// 日誌輸出會自動包含 Trace ID
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        // Trace ID 自動包含在日誌中
        log.info("查詢使用者: {}", id);
        
        // 業務邏輯...
        
        return Result.success(userDTO);
    }
}

// 日誌輸出:
// 2025-11-24 10:30:15.123 [http-nio-8080-exec-1] [a1b2c3d4e5f6] INFO  c.r.user.controller.UserController - 查詢使用者: 123
```

**跨服務傳遞** (未來微服務架構):

```java
// 使用 RestTemplate 或 Feign 時自動添加 Trace ID 到請求頭
@Component
public class TraceIdInterceptor implements ClientHttpRequestInterceptor {
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                        ClientHttpRequestExecution execution) throws IOException {
        String traceId = TraceContext.getTraceId();
        if (traceId != null) {
            request.getHeaders().add(TraceContext.getTraceIdHeader(), traceId);
        }
        return execution.execute(request, body);
    }
}
```

**關鍵設計點**:

1. **執行緒安全**: MDC 使用 ThreadLocal,自動處理多執行緒環境
2. **自動清理**: finally 區塊確保 MDC 被清理
3. **優先級最高**: @Order(HIGHEST_PRECEDENCE) 確保在其他過濾器前執行
4. **可追蹤**: Trace ID 在響應頭中返回,客戶端可用於查詢日誌
5. **擴展性**: 支援未來微服務間的 Trace ID 傳遞

---

## 測試數據模型

### 單元測試

```java
@Test
public void testTenantEntityAutoPopulation() {
    // 設定
    TenantContextHolder.setTenantId("123");
    
    // 建立實體
    Role role = new Role();
    role.setRoleCode("ADMIN");
    role.prePersist(); // 模擬 JPA 生命週期
    
    // 驗證租戶 ID 已自動填充
    assertEquals(123L, role.getTenantId());
    
    // 清理
    TenantContextHolder.clear();
}

@Test
public void testSoftDelete() {
    User user = new User();
    user.setDeleted(false);
    
    // 刪除
    user.setDeleted(true);
    
    // 驗證標記已設定
    assertTrue(user.getDeleted());
}
```

### 整合測試

```java
@SpringBootTest
@Transactional
public class BaseEntityIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testOptimisticLocking() {
        // 建立用戶
        User user = new User();
        user.setUsername("test");
        userRepository.save(user);
        
        // 在兩個會話中載入相同用戶
        User user1 = userRepository.findById(user.getId()).get();
        User user2 = userRepository.findById(user.getId()).get();
        
        // 在會話 1 中更新
        user1.setEmail("user1@example.com");
        userRepository.save(user1); // 版本增加到 1
        
        // 在會話 2 中更新（應該失敗，因為版本不匹配）
        user2.setEmail("user2@example.com");
        
        assertThrows(OptimisticLockException.class, () -> {
            userRepository.save(user2); // 版本仍為 0，偵測到衝突
        });
    }
}
```

---

## 總結

此數據模型設計提供：

1. **可重用性**: 三層繼承（BaseEntity → TenantEntity → AuditEntity）以進行漸進式功能新增
2. **租戶隔離**: 在實體級別強制執行，具有 `tenantId` 欄位和預持久化驗證
3. **審計追蹤**: 自動追蹤所有變更的誰和何時
4. **軟刪除**: 非破壞性刪除以符合規範和恢復
5. **樂觀鎖定**: 防止並發更新衝突
6. **標準化響應**: 統一的 `Result<T>` 和分頁模型

所有業務模組將擴展這些基礎類以繼承通用行為，同時保持模組特定欄位的靈活性。

**下一步**: 第一階段將繼續使用 `quickstart.md` 提供設定說明和使用範例。
