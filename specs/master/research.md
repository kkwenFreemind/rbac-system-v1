# 研究報告：001-common-layer

**日期**：2025-11-24 | **階段**：Phase 0 - 大綱與研究

## 概述

本文件整合了實作多租戶 RBAC 系統 Common Layer 的研究發現。所有來自實作計畫的技術未知項目都已透過分析專案文件、Spring Boot 最佳實踐和多租戶架構模式得到解決。

## 研究領域

### 1. MyBatis vs MyBatis-Plus 租戶隔離方案

#### 決策：**MyBatis-Plus（推薦）**

#### 理由

- **內建租戶攔截器**：MyBatis-Plus 提供開箱即用的 `TenantLineInnerInterceptor`，減少自訂程式碼
- **自動 CRUD**：BaseMapper 減少樣板程式碼，加速 repository 開發
- **活躍社群**：維護良好，更新頻繁，擁有完整的中文文件
- **分頁支援**：內建分頁外掛簡化清單查詢
- **程式碼生成**：支援程式碼產生器，快速建立 entity/mapper

#### 考慮的替代方案

| Option | Pros | Cons | Verdict |
|--------|------|------|---------|
| **原生 MyBatis** | 更多控制權，更輕量，明確的 SQL | 需要自訂租戶攔截器，更多樣板程式碼 | ❌ 已拒絕 - 需要太多自訂工作 |
| **Spring Data JPA** | JPA 標準，優秀的 Spring 整合 | 效能開銷，多租戶複雜，學習曲線 | ❌ 已拒絕 - 對 RBAC 系統過於複雜 |
| **MyBatis-Plus** | 兼具兩者優點，租戶支援，活躍社群 | 比原生 MyBatis 稍重 | ✅ **已選用** |

#### 實作指引

```java
// MyBatis-Plus 租戶攔截器配置
@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 租戶隔離攔截器
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = TenantContextHolder.getTenantId();
                if (tenantId == null) {
                    throw new TenantException("Tenant context not set");
                }
                return new LongValue(Long.parseLong(tenantId));
            }
            
            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }
            
            @Override
            public boolean ignoreTable(String tableName) {
                // 沒有 tenant_id 的系統表
                return Arrays.asList("sys_tenant", "sys_config").contains(tableName);
            }
        });
        
        // 分頁攔截器
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        
        interceptor.addInnerInterceptor(tenantInterceptor);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}
```

**參考資料**：

- [MyBatis-Plus 官方文件 - 租戶外掛](https://baomidou.com/pages/aef2f2/)
- 專案文件：[02-多租戶隔離策略.md](../../docs/02-多租戶隔離策略.md#32-策略一行級數據隔離推薦) - 第 3.2 節

---

### 2. Redis 客戶端：Jedis vs Lettuce

#### 決策：**Lettuce（推薦）**

#### 理由

- **Spring Boot 3.x 預設**：Spring Boot Starter Data Redis 預先配置
- **非同步/響應式支援**：基於 Netty 建構，支援非阻塞操作
- **連線池**：使用 Apache Commons Pool 2 提供更好的連線管理
- **執行緒安全**：所有連線預設為執行緒安全
- **活躍開發**：官方 Redis 客戶端，定期更新

#### 考慮的替代方案

| Option | Pros | Cons | Verdict |
|--------|------|------|---------|
| **Jedis** | 簡單 API，成熟，廣泛使用 | 僅同步，連線非執行緒安全 | ❌ 已拒絕 - 擴展性較差 |
| **Lettuce** | 非同步，執行緒安全，Spring Boot 預設 | 配置稍微複雜 | ✅ **已選用** |
| **Redisson** | 豐富功能（分散式鎖、集合） | 較重，基本快取可能過於複雜 | 🟡 若需進階功能可考慮 |

#### 實作指引

```yaml
# application.yml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 8    # 最大連線數
        max-wait: -1ms   # 最大等待時間（-1 = 無限制）
        max-idle: 8      # 最大閒置連線數
        min-idle: 0      # 最小閒置連線數
      shutdown-timeout: 100ms
```

```java
@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 值使用 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                                 ObjectMapper.DefaultTyping.NON_FINAL, 
                                 JsonTypeInfo.As.PROPERTY);
        serializer.setObjectMapper(om);
        
        // 鍵使用字串序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

**參考資料**：

- [Spring Data Redis 文件](https://spring.io/projects/spring-data-redis)
- [Lettuce 參考指南](https://lettuce.io/core/release/reference/)

---

### 3. 分散式鎖實作策略

#### 決策：**基於 Redis 的 Redlock 演算法（簡化單實例版本）**

#### 理由

- **經過驗證的模式**：基於 Redis 官方 Redlock 演算法
- **易於實作**：單一 Redis 實例足以應付大多數情況
- **效能**：低延遲（鎖定取得約 1-5ms）
- **自動過期**：內建 TTL 防止死鎖
- **成本效益**：無需額外基礎設施

#### 實作模式

```java
public interface DistributedLock {
    /**
     * 嘗試取得鎖定並設定逾時
     * @param key 鎖定鍵
     * @param timeout 逾時時長
     * @param unit 時間單位
     * @return 若取得鎖定則返回 true
     */
    boolean tryLock(String key, long timeout, TimeUnit unit);
    
    /**
     * 釋放鎖定
     * @param key 鎖定鍵
     */
    void unlock(String key);
    
    /**
     * 使用鎖定執行動作
     * @param key 鎖定鍵
     * @param timeout 鎖定逾時
     * @param unit 時間單位
     * @param action 要執行的動作
     * @return 動作結果
     */
    <T> T executeWithLock(String key, long timeout, TimeUnit unit, Supplier<T> action);
}

@Service
public class RedisDistributedLock implements DistributedLock {
    private final StringRedisTemplate redisTemplate;
    private final ThreadLocal<String> lockValue = new ThreadLocal<>();
    
    @Override
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        String value = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, value, timeout, unit);
        
        if (Boolean.TRUE.equals(success)) {
            lockValue.set(value);
            return true;
        }
        return false;
    }
    
    @Override
    public void unlock(String key) {
        String value = lockValue.get();
        if (value == null) return;
        
        // Lua 腳本用於原子性檢查與刪除
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(key),
            value
        );
        
        lockValue.remove();
    }
    
    @Override
    public <T> T executeWithLock(String key, long timeout, TimeUnit unit, Supplier<T> action) {
        if (!tryLock(key, timeout, unit)) {
            throw new BusinessException("Failed to acquire lock: " + key);
        }
        
        try {
            return action.get();
        } finally {
            unlock(key);
        }
    }
}
```

#### 鎖定鍵命名規範

```
lock:{模組}:{資源}:{id}

範例：
lock:user:create:tenant_123
lock:role:update:role_456
lock:permission:assign:user_789
```

#### 考慮的替代方案

| Option | Pros | Cons | Verdict |
|--------|------|------|---------|
| **資料庫鎖** | 簡單，無需 Redis | 效能瓶頸，擴展性有限 | ❌ 已拒絕 |
| **Redisson** | 功能完整，經過實戰考驗 | 相依性較重，設定複雜 | 🟡 若需多實例 Redis 叢集可考慮 |
| **Zookeeper** | 強一致性保證 | 需要額外基礎設施，較高延遲 | ❌ 已拒絕 - 過於複雜 |
| **Redis Redlock（單實例）** | 輕量，快速，滿足大多數情況 | 非分散式系統安全（可接受的折衷） | ✅ **已選用** |

**多實例 Redlock 說明**：對於需要高可用性的生產系統，建議使用 3 個以上 Redis 實例實作完整 Redlock 演算法。對於初期部署，配合適當監控的單實例 Redis 已足夠。

**參考資料**：

- [Redis Redlock Algorithm](https://redis.io/docs/manual/patterns/distributed-locks/)
- [Redisson Documentation](https://github.com/redisson/redisson/wiki/8.-distributed-locks-and-synchronizers)

---

### 4. ThreadLocal 清理最佳實踐

#### 關鍵問題：執行緒池污染

**問題**：應用伺服器（Tomcat、Jetty）使用執行緒池處理請求。若 `ThreadLocal` 未正確清理，同一執行緒上的下一個請求會繼承前一個租戶上下文，造成**災難性的跨租戶資料洩漏**。

#### 決策：**多層清理策略**

##### 第 1 層：保證清理的過濾器（主要）

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain chain) {
        try {
            // 1. 從請求中提取租戶 ID
            String tenantId = extractTenantId(request);
            
            // 2. 驗證租戶
            validateTenant(tenantId);
            
            // 3. 設定上下文
            TenantContextHolder.setTenantId(tenantId);
            
            // 4. 處理請求
            chain.doFilter(request, response);
            
        } catch (TenantException e) {
            handleTenantException(response, e);
        } catch (Exception e) {
            log.error("Unexpected error in TenantFilter", e);
            handleGenericException(response, e);
        } finally {
            // ⚠️ 關鍵：即使發生例外也要清理
            TenantContextHolder.clear();
        }
    }
    
    private String extractTenantId(HttpServletRequest request) {
        // 選項 1：從標頭取得
        String tenantId = request.getHeader("X-Tenant-Id");
        
        // 選項 2：從 JWT token 取得（生產環境首選）
        if (tenantId == null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                tenantId = extractTenantFromToken(token.substring(7));
            }
        }
        
        // 選項 3：從子網域取得
        if (tenantId == null) {
            String host = request.getServerName();
            tenantId = extractTenantFromSubdomain(host);
        }
        
        if (tenantId == null) {
            throw new TenantException("Tenant ID not found in request");
        }
        
        return tenantId;
    }
}
```

##### 第 2 層：請求攔截器（備援）

```java
@Component
public class TenantCleanupInterceptor implements HandlerInterceptor {
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        // 防禦性清理，以防過濾器被繞過
        TenantContextHolder.clear();
    }
}
```

##### 第 3 層：AOP 切面（額外安全）

```java
@Aspect
@Component
public class TenantContextCleanupAspect {
    
    @Around("@annotation(org.springframework.web.bind.annotation.RestController)")
    public Object cleanupTenantContext(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.clear();
        }
    }
}
```

##### 第 4 層：單元測試驗證

```java
@Test
public void testThreadLocalCleanup() throws Exception {
    // Simulate multiple requests on same thread
    ExecutorService executor = Executors.newSingleThreadExecutor();
    
    // Request 1: Tenant A
    executor.submit(() -> {
        TenantContextHolder.setTenantId("tenant_a");
        // Simulate request processing
        // ... 
        TenantContextHolder.clear();
    }).get();
    
    // Request 2: Tenant B (should NOT see tenant_a)
    String tenantId = executor.submit(() -> {
        return TenantContextHolder.getTenantId();
    }).get();
    
    assertNull("ThreadLocal not cleaned, data leakage detected!", tenantId);
}

@Test
public void testTenantFilterCleanup() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Tenant-Id", "tenant_123");
    
    MockHttpServletResponse response = new MockHttpServletResponse();
    
    FilterChain chain = (req, res) -> {
        assertEquals("tenant_123", TenantContextHolder.getTenantId());
    };
    
    TenantFilter filter = new TenantFilter();
    filter.doFilter(request, response, chain);
    
    // 過濾器執行後必須清除上下文
    assertNull("TenantContext not cleared after filter", TenantContextHolder.getTenantId());
}
```

#### TenantContextHolder 實作

```java
public class TenantContextHolder {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    
    // 用於除錯的稽核軌跡
    private static final ThreadLocal<Long> SET_TIMESTAMP = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        
        TENANT_ID.set(tenantId);
        SET_TIMESTAMP.set(System.currentTimeMillis());
        
        if (log.isDebugEnabled()) {
            log.debug("TenantContext set: {} on thread {}", tenantId, Thread.currentThread().getName());
        }
    }
    
    public static String getTenantId() {
        String tenantId = TENANT_ID.get();
        
        if (tenantId == null) {
            log.warn("Accessing TenantContext without setting tenant ID. Stack trace: ", 
                     new Exception("TenantContext not set"));
        }
        
        return tenantId;
    }
    
    public static void clear() {
        String tenantId = TENANT_ID.get();
        
        TENANT_ID.remove();
        SET_TIMESTAMP.remove();
        
        if (log.isDebugEnabled() && tenantId != null) {
            log.debug("TenantContext cleared: {} on thread {}", tenantId, Thread.currentThread().getName());
        }
    }
    
    /**
     * 僅用於測試/除錯 - 檢查上下文是否已設定
     */
    public static boolean isSet() {
        return TENANT_ID.get() != null;
    }
}
```

#### 監控與警示

```java
@Component
public class TenantContextMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // 每 1 分鐘
    public void checkLeakedContexts() {
        // 此方法在排程器執行緒上執行
        // 若租戶上下文在此設定，表示發生洩漏
        if (TenantContextHolder.isSet()) {
            log.error("⚠️ 關鍵：檢測到租戶上下文洩漏！上下文未清除。");
            
            // 警示監控系統
            meterRegistry.counter("rbac.tenant.context.leak").increment();
            
            // 強制清理
            TenantContextHolder.clear();
        }
    }
}
```

**參考資料**：

- 專案文件：[02-多租戶隔離策略.md](../../docs/02-多租戶隔離策略.md#22-租戶上下文管理) - 第 2.2 節（ThreadLocal 警告）
- [Spring Framework - ThreadLocal 最佳實踐](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-async)

---

### 5. 例外處理策略

#### 決策：**分層例外階層與全域處理器**

#### 例外階層設計

```
Throwable
└── Exception
    └── RuntimeException
        └── RbacException（基底）
            ├── BusinessException（4xx 錯誤）
            │   ├── ValidationException
            │   ├── ResourceNotFoundException
            │   └── DuplicateResourceException
            ├── SystemException（5xx 錯誤）
            │   ├── DatabaseException
            │   ├── CacheException
            │   └── ExternalServiceException
            ├── TenantException（租戶特定）
            │   ├── TenantNotFoundException
            │   ├── TenantExpiredException
            │   └── TenantIsolationViolationException
            └── SecurityException（安全相關）
                ├── AuthenticationException
                ├── PermissionDeniedException
                └── TokenExpiredException
```

#### 實作

```java
// 基底例外
public class RbacException extends RuntimeException {
    private String code;
    private HttpStatus httpStatus;
    private Map<String, Object> data;
    
    public RbacException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
    
    // Getters...
}

// 業務例外（客戶端錯誤）
public class BusinessException extends RbacException {
    public BusinessException(String message) {
        super("BUSINESS_ERROR", message, HttpStatus.BAD_REQUEST);
    }
    
    public BusinessException(String code, String message) {
        super(code, message, HttpStatus.BAD_REQUEST);
    }
}

// 全域例外處理器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @ExceptionHandler(RbacException.class)
    public ResponseEntity<Result<Void>> handleRbacException(RbacException ex) {
        log.warn("Business exception: code={}, message={}", ex.getCode(), ex.getMessage());
        
        Result<Void> result = Result.error(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(result);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        log.warn("Validation failed: {}", message);
        
        Result<Void> result = Result.error(ErrorCode.VALIDATION_ERROR, message);
        return ResponseEntity.badRequest().body(result);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        
        String message = "Internal server error";
        String code = ErrorCode.SYSTEM_ERROR;
        
        // 開發模式下包含堆疊追蹤
        if ("dev".equals(activeProfile)) {
            message = ex.getMessage();
        }
        
        Result<Void> result = Result.error(code, message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
    
    @ExceptionHandler(TenantException.class)
    public ResponseEntity<Result<Void>> handleTenantException(TenantException ex) {
        log.error("⚠️ CRITICAL: Tenant exception - {}", ex.getMessage(), ex);
        
        Result<Void> result = Result.error(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
}
```

#### 錯誤代碼規範（來自 spec.md）

```java
public class ErrorCode {
    // 通用錯誤（00-xxx）
    public static final String VALIDATION_ERROR = "00-1-001";
    public static final String RESOURCE_NOT_FOUND = "00-2-001";
    public static final String DUPLICATE_RESOURCE = "00-2-002";
    public static final String SYSTEM_ERROR = "00-3-001";
    public static final String DATABASE_ERROR = "00-3-002";
    
    // 租戶錯誤（03-xxx）
    public static final String TENANT_NOT_FOUND = "03-2-001";
    public static final String TENANT_EXPIRED = "03-2-002";
    public static final String TENANT_ISOLATION_VIOLATION = "03-4-001";
    
    // 認證錯誤（01-xxx）
    public static final String AUTHENTICATION_FAILED = "01-4-001";
    public static final String TOKEN_EXPIRED = "01-4-002";
    public static final String PERMISSION_DENIED = "01-4-003";
}
```

**參考資料**：

- 專案文件：`04-低耦合設計指南.md` - 第 9 節（異常處理解耦）
- spec.md - 附錄（錯誤代碼規範）

---

### 6. 配置外部化模式

#### 決策：**Spring Boot Configuration Properties + 環境變數**

#### 配置階層

```
1. 環境變數（最高優先權）
   ↓（覆寫）
2. application-{profile}.yml
   ↓（覆寫）
3. application.yml（預設值）
```

#### 實作

```yaml
# application.yml（預設值）
rbac:
  tenant:
    header-name: X-Tenant-Id
    enabled: true
  cache:
    enabled: true
    ttl: 1800
    prefix: "rbac"
  lock:
    timeout: 30
    retry-count: 3
    retry-delay: 100
  audit:
    enabled: true
    async: true

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rbac_system
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0

---
# application-prod.yml（生產環境覆寫）
rbac:
  tenant:
    enabled: true
  cache:
    ttl: 3600
  lock:
    timeout: 60

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    hikari:
      maximum-pool-size: 50
```

#### 配置屬性類別

```java
@Configuration
@ConfigurationProperties(prefix = "rbac")
@Validated
public class RbacProperties {
    
    @NotNull
    private TenantProperties tenant = new TenantProperties();
    
    @NotNull
    private CacheProperties cache = new CacheProperties();
    
    @NotNull
    private LockProperties lock = new LockProperties();
    
    @NotNull
    private AuditProperties audit = new AuditProperties();
    
    // 內部類別
    public static class TenantProperties {
        private String headerName = "X-Tenant-Id";
        private boolean enabled = true;
        private List<String> excludedTables = new ArrayList<>();
        
        // Getters/setters
    }
    
    public static class CacheProperties {
        private boolean enabled = true;
        private int ttl = 1800; // seconds
        private String prefix = "rbac";
        
        // Getters/setters
    }
    
    public static class LockProperties {
        private int timeout = 30; // seconds
        private int retryCount = 3;
        private int retryDelay = 100; // milliseconds
        
        // Getters/setters
    }
    
    public static class AuditProperties {
        private boolean enabled = true;
        private boolean async = true;
        
        // Getters/setters
    }
}
```

#### 程式碼使用方式

```java
@Service
public class TenantService {
    
    private final RbacProperties rbacProperties;
    
    public TenantService(RbacProperties rbacProperties) {
        this.rbacProperties = rbacProperties;
    }
    
    public void process(HttpServletRequest request) {
        if (!rbacProperties.getTenant().isEnabled()) {
            return;
        }
        
        String headerName = rbacProperties.getTenant().getHeaderName();
        String tenantId = request.getHeader(headerName);
        // ...
    }
}
```

#### 環境特定配置檔案

```bash
# 開發環境
export DB_USERNAME=dev_user
export DB_PASSWORD=dev_pass
export REDIS_HOST=localhost

# 生產環境（在部署環境中設定）
export DB_USERNAME=prod_user
export DB_PASSWORD=<secured-value>
export REDIS_HOST=redis-cluster.internal
```

**參考資料**：

- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- 專案文件：`04-低耦合設計指南.md` - 第 6 節（配置外部化）

---

## 決策摘要

| 領域 | 決策 | 主要理由 |
|------|----------|----------------|
| **ORM 框架** | MyBatis-Plus | 內建租戶攔截器，較少樣板程式碼 |
| **Redis 客戶端** | Lettuce | Spring Boot 預設，非同步支援，執行緒安全 |
| **分散式鎖** | Redis Redlock（單實例） | 簡單，快速，足以應付初期部署 |
| **ThreadLocal 清理** | 多層（Filter + Interceptor + AOP） | 防止災難性的租戶資料洩漏 |
| **例外處理** | 分層階層 + 全域處理器 | 清晰的錯誤分類，生產環境安全 |
| **配置** | Spring Boot Properties + 環境變數 | 標準，安全，環境靈活 |

## 後續步驟（Phase 1）

所有技術決策已解決，Phase 1 可以進行：

1. **data-model.md**：定義實體模型（BaseEntity、TenantEntity 等）
2. **contracts/**：定義內部 API（若需要跨模組合約）
3. **quickstart.md**：設定說明和使用範例

技術上下文中的所有需要澄清項目均已解決。可以放心開始實作。
