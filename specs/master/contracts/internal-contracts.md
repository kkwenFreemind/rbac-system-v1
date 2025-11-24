# 公共層內部契約

**日期**: 2025-11-24 | **階段**: Phase 1 - 設計與契約

## 概述

公共層提供業務模組所依賴的內部服務契約（Java 介面）。這些不是 REST API，而是用於快取、資料庫和工具操作的程式化介面。

## 契約類別

1. **快取服務契約** - Redis 快取操作
2. **鎖服務契約** - 分散式鎖定
3. **Repository 契約** - 資料庫存取模式
4. **上下文契約** - 租戶與安全上下文管理

---

## 1. 快取服務契約

### 介面: `CacheService`

**套件**: `com.rbac.common.redis.util`

**目的**: 統一的快取介面，抽象化 Redis 操作。

**方法**:

```java
public interface CacheService {
    
    /**
     * 從快取取得值
     * @param key 快取鍵
     * @param type 值的類別型別
     * @return 快取的值，若未找到則回傳 null
     */
    <T> T get(String key, Class<T> type);
    
    /**
     * 設定快取值並指定 TTL
     * @param key 快取鍵
     * @param value 要快取的值
     * @param ttl 存活時間（秒）
     */
    void set(String key, Object value, long ttl);
    
    /**
     * 使用預設 TTL 設定快取值
     * @param key 快取鍵
     * @param value 要快取的值
     */
    void set(String key, Object value);
    
    /**
     * 刪除單一鍵
     * @param key 快取鍵
     * @return 若已刪除回傳 true，若鍵不存在回傳 false
     */
    boolean delete(String key);
    
    /**
     * 刪除符合模式的鍵
     * @param pattern 鍵模式（例如："user:*"）
     * @return 已刪除的鍵數量
     */
    long deletePattern(String pattern);
    
    /**
     * 檢查鍵是否存在
     * @param key 快取鍵
     * @return 若存在回傳 true
     */
    boolean exists(String key);
    
    /**
     * 為鍵設定過期時間
     * @param key 快取鍵
     * @param ttl 存活時間（秒）
     * @return 若成功設定過期時間回傳 true
     */
    boolean expire(String key, long ttl);
    
    /**
     * 遞增數值
     * @param key 快取鍵
     * @param delta 遞增量
     * @return 遞增後的新值
     */
    Long increment(String key, long delta);
    
    /**
     * 遞減數值
     * @param key 快取鍵
     * @param delta 遞減量
     * @return 遞減後的新值
     */
    Long decrement(String key, long delta);
}
```

**使用契約範例**:

```java
@Service
public class UserService {
    private final CacheService cacheService;
    
    public User getUserById(Long id) {
        // 1. 優先嘗試快取
        String cacheKey = CacheKeyUtil.userKey(id);
        User user = cacheService.get(cacheKey, User.class);
        
        // 2. 若快取中沒有，從資料庫載入
        if (user == null) {
            user = userRepository.findById(id);
            if (user != null) {
                // 3. 以 30 分鐘 TTL 存入快取
                cacheService.set(cacheKey, user, 1800);
            }
        }
        
        return user;
    }
}
```

**鍵命名契約**:

```
格式: {module}:{tenant}:{type}:{id}

範例:
user:123:info:456        → 租戶 123 中使用者 456 的資訊
role:123:permissions:789 → 租戶 123 中角色 789 的權限
auth:123:token:abc       → 租戶 123 的驗證令牌
```

---

## 2. 鎖服務契約

### 介面: `DistributedLock`

**套件**: `com.rbac.common.redis.lock`

**目的**: 用於並行操作的分散式鎖定。

**方法**:

```java
public interface DistributedLock {
    
    /**
     * 嘗試取得鎖並指定逾時時間
     * @param key 鎖鍵
     * @param timeout 鎖逾時持續時間
     * @param unit 時間單位
     * @return 若取得鎖回傳 true，否則回傳 false
     */
    boolean tryLock(String key, long timeout, TimeUnit unit);
    
    /**
     * 使用預設逾時時間（30 秒）嘗試取得鎖
     * @param key 鎖鍵
     * @return 若取得鎖回傳 true
     */
    default boolean tryLock(String key) {
        return tryLock(key, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 釋放鎖
     * @param key 鎖鍵
     */
    void unlock(String key);
    
    /**
     * 在鎖定狀態下執行操作（自動取得與釋放）
     * @param key 鎖鍵
     * @param timeout 鎖逾時時間
     * @param unit 時間單位
     * @param action 持有鎖時要執行的操作
     * @return 操作結果
     * @throws BusinessException 若無法取得鎖
     */
    <T> T executeWithLock(String key, long timeout, TimeUnit unit, Supplier<T> action);
    
    /**
     * 使用預設逾時時間在鎖定狀態下執行操作
     * @param key 鎖鍵
     * @param action 要執行的操作
     * @return 操作結果
     */
    default <T> T executeWithLock(String key, Supplier<T> action) {
        return executeWithLock(key, 30, TimeUnit.SECONDS, action);
    }
    
    /**
     * 在鎖定狀態下執行 runnable 操作（無回傳值）
     * @param key 鎖鍵
     * @param timeout 鎖逾時時間
     * @param unit 時間單位
     * @param action 要執行的操作
     */
    default void executeWithLock(String key, long timeout, TimeUnit unit, Runnable action) {
        executeWithLock(key, timeout, unit, () -> {
            action.run();
            return null;
        });
    }
}
```

**使用契約範例**:

```java
@Service
public class RoleService {
    private final DistributedLock distributedLock;
    
    public void assignPermission(Long roleId, Long permissionId) {
        // 鎖鍵格式: lock:module:operation:id
        String lockKey = "lock:role:assign:" + roleId;
        
        // 在鎖定狀態下執行
        distributedLock.executeWithLock(lockKey, 30, TimeUnit.SECONDS, () -> {
            // 關鍵區段：僅有一個執行緒可以執行此操作
            Role role = roleRepository.findById(roleId);
            Permission permission = permissionRepository.findById(permissionId);
            
            // 檢查是否已分配（鎖可防止競爭條件）
            if (!role.hasPermission(permissionId)) {
                rolePermissionRepository.save(new RolePermission(roleId, permissionId));
            }
        });
    }
}
```

**鎖鍵命名契約**:

```
格式: lock:{module}:{operation}:{resource}

範例:
lock:user:create:tenant_123     → 在租戶 123 中建立使用者的鎖
lock:role:update:role_456       → 更新角色 456 的鎖
lock:permission:assign:user_789 → 為使用者 789 分配權限的鎖
```

---

## 3. Repository 契約

### 介面: `BaseRepository<T, ID>`

**套件**: `com.rbac.common.database.repository`

**目的**: 用於 CRUD 操作的通用 repository 契約。

**方法**:

```java
public interface BaseRepository<T extends BaseEntity, ID> {
    
    /**
     * 以 ID 查詢實體
     * @param id 實體 ID
     * @return 實體，若未找到則回傳 null
     */
    T findById(ID id);
    
    /**
     * 查詢所有實體（請謹慎使用，優先使用分頁）
     * @return 所有實體的清單
     */
    List<T> findAll();
    
    /**
     * 以分頁方式查詢實體
     * @param pageRequest 分頁參數
     * @return 分頁結果
     */
    PageResponse<T> findAll(PageRequest pageRequest);
    
    /**
     * 儲存實體（新增或更新）
     * @param entity 要儲存的實體
     * @return 已儲存的實體（包含生成的 ID）
     */
    T save(T entity);
    
    /**
     * 批次儲存多個實體
     * @param entities 要儲存的實體
     * @return 已儲存的實體數量
     */
    int saveBatch(List<T> entities);
    
    /**
     * 更新實體
     * @param entity 包含更新值的實體
     * @return 已更新的實體
     */
    T update(T entity);
    
    /**
     * 以 ID 刪除實體（若為 BaseEntity 則為軟刪除）
     * @param id 實體 ID
     * @return 若已刪除回傳 true
     */
    boolean deleteById(ID id);
    
    /**
     * 檢查實體是否存在
     * @param id 實體 ID
     * @return 若存在回傳 true
     */
    boolean existsById(ID id);
    
    /**
     * 計算總實體數
     * @return 總數
     */
    long count();
}
```

**租戶感知 Repository 契約**:

```java
public interface TenantRepository<T extends TenantEntity, ID> extends BaseRepository<T, ID> {
    
    /**
     * 在當前租戶上下文中以 ID 查詢實體
     * 自動從 TenantContextHolder 依 tenant_id 過濾
     * @param id 實體 ID
     * @return 實體，若未找到或屬於不同租戶則回傳 null
     */
    @Override
    T findById(ID id);
    
    /**
     * 查詢當前租戶內的所有實體
     * @return 當前租戶的實體清單
     */
    @Override
    List<T> findAll();
    
    /**
     * 以租戶 ID 查詢實體（僅管理員使用）
     * @param tenantId 租戶 ID
     * @return 指定租戶的實體清單
     */
    List<T> findByTenantId(Long tenantId);
    
    /**
     * 計算當前租戶內的實體數
     * @return 當前租戶的數量
     */
    @Override
    long count();
}
```

**實作說明**: 所有 repository 實作必須使用搭配 `TenantInterceptor` 的 MyBatis-Plus 來自動注入 `tenant_id` 過濾器。業務程式碼不應手動新增 `WHERE tenant_id = ?` 條件。

---

## 4. 上下文契約

### 介面: `TenantContextHolder`（靜態工具）

**套件**: `com.rbac.common.database.context`

**目的**: 用於當前租戶上下文的執行緒區域儲存。

**方法**:

```java
public class TenantContextHolder {
    
    /**
     * 為當前執行緒設定租戶 ID
     * @param tenantId 租戶 ID
     * @throws IllegalArgumentException 若 tenantId 為 null
     */
    public static void setTenantId(String tenantId);
    
    /**
     * 取得當前執行緒的租戶 ID
     * @return 租戶 ID，若未設定則回傳 null
     */
    public static String getTenantId();
    
    /**
     * 清除當前執行緒的租戶 ID
     * 關鍵：必須在 finally 區塊中呼叫以防止執行緒池污染
     */
    public static void clear();
    
    /**
     * 檢查租戶上下文是否已設定（用於測試/除錯）
     * @return 若已設定租戶 ID 則回傳 true
     */
    public static boolean isSet();
}
```

**使用契約**:

```java
// 在 TenantFilter 中
@Override
protected void doFilterInternal(HttpServletRequest request, 
                               HttpServletResponse response, 
                               FilterChain chain) {
    try {
        String tenantId = extractTenantId(request);
        TenantContextHolder.setTenantId(tenantId);
        
        chain.doFilter(request, response);
    } finally {
        // ⚠️ 關鍵：務必清理
        TenantContextHolder.clear();
    }
}
```

**契約保證**:
1. 在任何租戶範圍的資料庫操作之前必須呼叫 `setTenantId()`
2. 必須在 `finally` 區塊中呼叫 `clear()` 以防止洩漏
3. 若在未設定租戶 ID 的情況下呼叫 `getTenantId()` 會記錄警告
4. 所有租戶範圍的 repository 都依賴此上下文被設定

---

## 5. 工具契約

### 介面: `CacheKeyUtil`（靜態工具）

**套件**: `com.rbac.common.redis.util`

**目的**: 生成標準化的快取鍵。

**方法**:

```java
public class CacheKeyUtil {
    
    /**
     * 生成帶有租戶上下文的快取鍵
     * @param module 模組名稱（例如："user", "role"）
     * @param type 資料類型（例如："info", "permissions"）
     * @param id 資源 ID
     * @return 格式化的快取鍵
     */
    public static String generateKey(String module, String type, String id);
    
    /**
     * 生成使用者快取鍵
     * @param userId 使用者 ID
     * @return 鍵格式："user:{tenantId}:info:{userId}"
     */
    public static String userKey(Long userId);
    
    /**
     * 生成角色快取鍵
     * @param roleId 角色 ID
     * @return 鍵格式："role:{tenantId}:info:{roleId}"
     */
    public static String roleKey(Long roleId);
    
    /**
     * 生成使用者權限快取鍵
     * @param userId 使用者 ID
     * @return 鍵格式："user:{tenantId}:permissions:{userId}"
     */
    public static String userPermissionsKey(Long userId);
    
    /**
     * 生成用於刪除某類型所有鍵的模式
     * @param module 模組名稱
     * @param type 資料類型
     * @return 模式格式："{module}:{tenantId}:{type}:*"
     */
    public static String pattern(String module, String type);
}
```

---

## 契約測試需求

### 單元測試契約

所有契約必須有單元測試驗證：

```java
// CacheService 契約測試
@Test
public void cacheService_shouldStoreAndRetrieve() {
    CacheService cache = new RedisCacheService(redisTemplate);
    
    cache.set("test:key", "test-value", 60);
    String value = cache.get("test:key", String.class);
    
    assertEquals("test-value", value);
}

// DistributedLock 契約測試
@Test
public void distributedLock_shouldPreventConcurrentAccess() throws Exception {
    DistributedLock lock = new RedisDistributedLock(redisTemplate);
    
    CountDownLatch latch = new CountDownLatch(2);
    AtomicInteger counter = new AtomicInteger(0);
    
    // 兩個執行緒嘗試遞增計數器
    Runnable task = () -> {
        lock.executeWithLock("test:lock", () -> {
            int current = counter.get();
            Thread.sleep(10); // 模擬工作
            counter.set(current + 1);
        });
        latch.countDown();
    };
    
    new Thread(task).start();
    new Thread(task).start();
    
    latch.await(5, TimeUnit.SECONDS);
    
    // 沒有鎖的話，結果可能是 1（競爭條件）
    // 有鎖的話，結果保證是 2
    assertEquals(2, counter.get());
}

// TenantContextHolder 契約測試
@Test
public void tenantContextHolder_shouldIsolateThreads() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    
    // 執行緒 1：租戶 A
    Future<String> tenant1 = executor.submit(() -> {
        TenantContextHolder.setTenantId("tenant_a");
        Thread.sleep(100);
        String id = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        return id;
    });
    
    // 執行緒 2：租戶 B
    Future<String> tenant2 = executor.submit(() -> {
        TenantContextHolder.setTenantId("tenant_b");
        Thread.sleep(100);
        String id = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        return id;
    });
    
    // 每個執行緒應該看到自己的租戶 ID
    assertEquals("tenant_a", tenant1.get());
    assertEquals("tenant_b", tenant2.get());
}
```

---

## 契約版本控制

由於這些是內部契約，版本控制遵循語意化版本控制：

- **MAJOR**：破壞性變更（方法簽名變更、刪除方法）
- **MINOR**：新增方法（向下相容）
- **PATCH**：錯誤修復、文件更新

**範例**：
- `CacheService v1.0.0`：初始發布
- `CacheService v1.1.0`：新增 `increment()` 和 `decrement()` 方法（向下相容）
- `CacheService v2.0.0`：變更 `get()` 回傳類型或參數（破壞性變更）

---

## 總結

公共層提供 **5 個關鍵契約類別**：

1. **CacheService**：Redis 快取抽象
2. **DistributedLock**：並行存取控制
3. **BaseRepository / TenantRepository**：資料庫存取模式
4. **TenantContextHolder**：執行緒區域租戶上下文管理
5. **CacheKeyUtil**：標準化快取鍵生成

所有契約強制執行 **憲法遵從**：
- 租戶阤2離（透過 `TenantContextHolder` 和 `TenantRepository`）
- 基於介面的設計（低耦合）
- 安全優先（租戶驗證、上下文清理）
- 可測試性（所有契約都有單元測試需求）

**下一步**：Phase 1 繼續以 `quickstart.md` 提供設定與使用指南。
