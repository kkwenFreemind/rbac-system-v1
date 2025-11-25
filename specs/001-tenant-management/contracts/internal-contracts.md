# 內部契約 - 租戶管理模組

> **版本**: 1.0.0
> **更新日期**: 2025-01-XX
> **狀態**: Phase 1 設計

---

## 概述

本文件定義租戶管理模組與系統其他模組之間的內部契約，包括服務介面、事件通知、以及資料交換格式。

---

## 服務介面契約

### ITenantService

租戶服務的核心介面，供其他模組調用。

```java
public interface ITenantService {
    
    /**
     * 建立租戶
     * 
     * @param request 建立請求
     * @return 租戶回應
     * @throws BusinessException 當租戶名稱或郵箱重複時
     */
    TenantResponse createTenant(CreateTenantRequest request);
    
    /**
     * 根據 ID 取得租戶
     * 
     * @param id 租戶 ID
     * @return 租戶回應
     * @throws ResourceNotFoundException 當租戶不存在時
     */
    TenantResponse getTenantById(Long id);
    
    /**
     * 查詢租戶清單
     * 
     * @param request 查詢請求
     * @return 租戶清單回應
     */
    TenantListResponse listTenants(QueryTenantRequest request);
    
    /**
     * 更新租戶
     * 
     * @param id 租戶 ID
     * @param request 更新請求
     * @return 租戶回應
     * @throws ResourceNotFoundException 當租戶不存在時
     * @throws BusinessException 當租戶名稱或郵箱衝突時
     */
    TenantResponse updateTenant(Long id, UpdateTenantRequest request);
    
    /**
     * 刪除租戶（軟刪除）
     * 
     * @param id 租戶 ID
     * @throws ResourceNotFoundException 當租戶不存在時
     * @throws BusinessException 當刪除條件不滿足時
     */
    void deleteTenant(Long id);
    
    /**
     * 變更租戶狀態
     * 
     * @param id 租戶 ID
     * @param status 目標狀態
     * @param reason 變更原因
     * @return 租戶回應
     * @throws ResourceNotFoundException 當租戶不存在時
     * @throws BusinessException 當狀態轉換不合法時
     */
    TenantResponse changeTenantStatus(Long id, TenantStatus status, String reason);
    
    /**
     * 檢查租戶是否存在
     * 
     * @param id 租戶 ID
     * @return 是否存在
     */
    boolean existsTenantById(Long id);
    
    /**
     * 檢查租戶名稱是否已存在
     * 
     * @param name 租戶名稱
     * @return 是否存在
     */
    boolean existsTenantByName(String name);
    
    /**
     * 檢查郵箱是否已存在
     * 
     * @param email 聯絡郵箱
     * @return 是否存在
     */
    boolean existsTenantByEmail(String email);
    
    /**
     * 驗證租戶狀態是否為啟用
     * 
     * @param id 租戶 ID
     * @return 是否啟用
     * @throws ResourceNotFoundException 當租戶不存在時
     */
    boolean isTenantActive(Long id);
}
```

---

## 依賴的 Common Layer 介面

### TenantContextHolder

租戶上下文管理（來自 `rbac-common-database`）。

```java
public class TenantContextHolder {
    
    /**
     * 設定當前執行緒的租戶 ID
     * 
     * @param tenantId 租戶 ID
     */
    public static void setTenantId(Long tenantId);
    
    /**
     * 取得當前執行緒的租戶 ID
     * 
     * @return 租戶 ID
     */
    public static Long getTenantId();
    
    /**
     * 清除當前執行緒的租戶上下文
     */
    public static void clear();
}
```

### Result<T>

統一回應包裝（來自 `rbac-common-core`）。

```java
@Data
public class Result<T> {
    private Boolean success;
    private Integer code;
    private String message;
    private T data;
    
    public static <T> Result<T> success(T data);
    public static <T> Result<T> error(Integer code, String message);
}
```

### 異常類型

來自 `rbac-common-core`：

```java
// 業務異常
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;
}

// 資源不存在異常
public class ResourceNotFoundException extends BusinessException {
    // 固定 code = 404
}

// 參數驗證異常
public class ValidationException extends BusinessException {
    // 固定 code = 400
}
```

---

## 資料交換格式

### 租戶基本資訊

用於模組間傳遞租戶基本資訊。

```java
@Data
public class TenantInfo {
    private Long id;
    private String name;
    private PlanType planType;
    private TenantStatus status;
}
```

---

## 事件通知契約

租戶管理模組將在關鍵操作後發布事件（未來實作）。

### TenantCreatedEvent

```java
@Data
public class TenantCreatedEvent {
    private Long tenantId;
    private String tenantName;
    private PlanType planType;
    private LocalDateTime createdAt;
    private Long createdBy;
}
```

### TenantStatusChangedEvent

```java
@Data
public class TenantStatusChangedEvent {
    private Long tenantId;
    private TenantStatus oldStatus;
    private TenantStatus newStatus;
    private String reason;
    private LocalDateTime changedAt;
    private Long changedBy;
}
```

### TenantDeletedEvent

```java
@Data
public class TenantDeletedEvent {
    private Long tenantId;
    private String tenantName;
    private LocalDateTime deletedAt;
    private Long deletedBy;
}
```

---

## 快取契約

### Redis 鍵格式

```
tenant:{id}              # 租戶詳情快取
tenant:name:{name}       # 租戶名稱唯一性檢查快取
tenant:email:{email}     # 租戶郵箱唯一性檢查快取
tenant:list:{hash}       # 租戶清單查詢快取（雜湊查詢條件）
```

### TTL 策略

- 租戶詳情: 1 小時
- 唯一性檢查: 5 分鐘
- 清單查詢: 10 分鐘

### 快取失效策略

- 建立租戶: 失效 `tenant:name:{name}`, `tenant:email:{email}`, `tenant:list:*`
- 更新租戶: 失效 `tenant:{id}`, `tenant:name:{name}`, `tenant:email:{email}`, `tenant:list:*`
- 刪除租戶: 失效 `tenant:{id}`, `tenant:list:*`

---

## 資料庫契約

### 表結構

租戶管理模組僅操作 `tenants` 表，不直接操作其他表。

### MyBatis 攔截器配置

租戶管理模組**不自行配置** MyBatis 攔截器，完全依賴 Common Database 模組的全域配置：

- `TenantLineInnerInterceptor`: 自動過濾 `tenant_id`
- `AuditMetaObjectHandler`: 自動填充稽核欄位

### 忽略租戶過濾的表

以下系統層級表不進行租戶過濾（在 MyBatis 配置中定義）:

```java
List<String> ignoreTables = Arrays.asList("tenants");
```

**說明**: `tenants` 表本身儲存租戶資訊,使用 `tenant_id` 指向自身,因此在某些管理場景下需要忽略自動過濾。

---

## 權限契約

### 權限碼定義

租戶管理模組定義以下權限碼（供 RBAC 權限模組使用）：

| 權限碼 | 權限名稱 | 說明 |
|--------|---------|------|
| `tenant:create` | 建立租戶 | 允許建立新租戶 |
| `tenant:read` | 查詢租戶 | 允許查詢租戶清單和詳情 |
| `tenant:update` | 更新租戶 | 允許更新租戶資訊和狀態 |
| `tenant:delete` | 刪除租戶 | 允許刪除租戶 |

### 權限檢查

租戶管理模組的 Controller 使用 Spring Security 註解進行權限檢查：

```java
@PreAuthorize("hasAuthority('tenant:create')")
public Result<TenantResponse> createTenant(...) { ... }

@PreAuthorize("hasAuthority('tenant:read')")
public Result<TenantResponse> getTenantById(...) { ... }

@PreAuthorize("hasAuthority('tenant:update')")
public Result<TenantResponse> updateTenant(...) { ... }

@PreAuthorize("hasAuthority('tenant:delete')")
public Result<Void> deleteTenant(...) { ... }
```

---

## 交易契約

### 交易邊界

租戶服務的所有寫入操作（建立、更新、刪除）都在 Service 層使用 `@Transactional` 管理交易：

```java
@Transactional(rollbackFor = Exception.class)
public TenantResponse createTenant(CreateTenantRequest request) { ... }
```

### 唯讀操作

查詢操作使用 `@Transactional(readOnly = true)` 優化效能：

```java
@Transactional(readOnly = true)
public TenantResponse getTenantById(Long id) { ... }
```

---

## 並行控制契約

### 樂觀鎖

租戶實體使用資料庫的 `updated_at` 欄位進行樂觀鎖控制（未來可升級為 MyBatis-Plus 的 `@Version`）：

```sql
UPDATE tenants 
SET name = ?, updated_at = NOW() 
WHERE id = ? AND updated_at = ?
```

若更新失敗（`updated_at` 不匹配），拋出 `ConcurrentModificationException`。

### 分散式鎖

對於需要強一致性的操作（如租戶名稱唯一性檢查 + 建立），使用 Redis 分散式鎖：

```java
String lockKey = "tenant:create:lock";
try {
    redisLockUtil.tryLock(lockKey, 5, TimeUnit.SECONDS);
    // 檢查唯一性 + 建立租戶
} finally {
    redisLockUtil.unlock(lockKey);
}
```

---

## 錯誤碼契約

租戶管理模組定義以下錯誤碼：

| 錯誤碼 | HTTP 狀態 | 說明 |
|-------|----------|------|
| `200` | 200 | 操作成功 |
| `400` | 400 | 請求參數錯誤 |
| `401` | 401 | 未認證 |
| `403` | 403 | 無權限 |
| `404` | 404 | 租戶不存在 |
| `409` | 409 | 租戶名稱或郵箱衝突 |
| `500` | 500 | 伺服器內部錯誤 |

### 自訂業務錯誤碼

| 錯誤碼 | HTTP 狀態 | 說明 |
|-------|----------|------|
| `10001` | 400 | 租戶狀態轉換不合法 |
| `10002` | 400 | 租戶下存在活躍使用者，無法刪除 |
| `10003` | 400 | 僅能刪除 INACTIVE 狀態的租戶 |

---

## 測試契約

### 單元測試

租戶服務的單元測試使用 Mockito 模擬依賴：

```java
@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    
    @Mock
    private TenantMapper tenantMapper;
    
    @InjectMocks
    private TenantServiceImpl tenantService;
    
    // 測試用例...
}
```

### 整合測試

使用 Testcontainers 啟動真實 PostgreSQL 和 Redis：

```java
@SpringBootTest
@Testcontainers
class TenantServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);
    
    // 測試用例...
}
```

---

## 效能契約

### SLA 目標

- **租戶建立**: P95 < 200ms
- **租戶查詢（單筆）**: P95 < 50ms
- **租戶清單查詢**: P95 < 100ms
- **租戶更新**: P95 < 150ms
- **租戶刪除**: P95 < 100ms

### 並行能力

- 支援 1000+ 並行請求
- 無資料競爭問題
- 無記憶體洩漏（ThreadLocal 正確清理）

---

## 總結

內部契約定義了租戶管理模組與系統其他模組的交互規範，包括：

1. ✅ **服務介面**: 清晰的方法簽名和異常定義
2. ✅ **資料交換**: 統一的 DTO 和回應格式
3. ✅ **事件通知**: 關鍵操作的事件發布（未來實作）
4. ✅ **快取策略**: Redis 鍵格式和 TTL 定義
5. ✅ **權限控制**: 權限碼和檢查註解
6. ✅ **交易管理**: 交易邊界和並行控制
7. ✅ **錯誤處理**: 統一的錯誤碼和回應格式

下一步將在 `quickstart.md` 中提供開發環境設定指南。
