# RBAC Common Layer

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36.svg)](https://maven.apache.org/)

RBAC Common Layer 是一個多租戶企業級 Java 基礎設施層，提供統一的異常處理、資料庫租戶隔離、Redis 快取與分散式鎖定，以及 Web 請求處理等核心功能。

## 🏗️ 架構概覽

```mermaid
rbac-common/
├── rbac-common-core/          # 核心模組 - 異常處理、工具類別、統一響應格式
├── rbac-common-database/      # 資料庫模組 - 多租戶隔離、MyBatis-Plus 配置
├── rbac-common-redis/         # Redis 模組 - 快取服務、分散式鎖定
└── rbac-common-web/           # Web 模組 - HTTP 過濾器、全域異常處理、請求追蹤
```

## 📦 模組說明

### 🔧 rbac-common-core

**核心功能模組**，提供所有業務模組依賴的基礎設施：

- **統一異常處理**: `RbacException`、`BusinessException`、`SystemException` 等
- **響應格式**: `Result<T>` 統一 API 響應格式
- **工具類別**: 字串處理、日期處理、JSON 處理、加密、驗證等
- **常數定義**: 錯誤代碼、租戶常數等

### 🗄️ rbac-common-database

**資料庫存取模組**，實現多租戶資料隔離：

- **實體基類**: `BaseEntity` (Snowflake ID)、`TenantEntity`、`AuditEntity`
- **租戶隔離**: 自動注入 `tenant_id`，MyBatis-Plus 攔截器過濾
- **稽核追蹤**: 自動填充 `createTime`、`updateTime`、`createBy`、`updateBy`
- **連線池**: HikariCP 配置優化

### ⚡ rbac-common-redis

**快取與鎖定模組**，提供高效能資料存取：

- **快取服務**: `CacheService` 介面，支援 Redis 快取操作
- **分散式鎖定**: `DistributedLock` 介面，基於 Redis 的分散式鎖
- **鍵值管理**: 統一的快取鍵生成策略
- **序列化**: Jackson 序列化配置

### 🌐 rbac-common-web

**Web 處理模組**，統一 HTTP 請求處理：

- **租戶過濾器**: 從 `X-Tenant-Id` 標頭提取租戶資訊
- **追蹤過濾器**: MDC Trace ID 生成與傳播
- **全域異常處理**: 統一錯誤響應格式
- **請求記錄**: AOP 切面記錄請求詳情

## 🚀 快速開始

### 1. 加入依賴

在您的 `pom.xml` 中加入需要的模組：

```xml
<dependencies>
    <!-- 核心模組（必需） -->
    <dependency>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-common-core</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- 資料庫模組 -->
    <dependency>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-common-database</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Redis 模組 -->
    <dependency>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-common-redis</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Web 模組 -->
    <dependency>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-common-web</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 2. 配置應用

#### 資料庫配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rbac_db
    username: rbac_user
    password: rbac_password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: assign_id  # Snowflake ID
```

#### Redis 配置 (application.yml)

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

#### Web 配置 (application.yml)

```yaml
rbac:
  cors:
    allowed-origins: "http://localhost:3000,https://yourdomain.com"
    allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
    allowed-headers: "*"
    allow-credentials: true
```

## 💡 使用範例

### 統一 API 響應

```java
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @PostMapping("/users")
    public Result<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.create(request);
        return Result.success(user);
    }

    @PutMapping("/users/{id}")
    public Result<User> updateUser(@PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        User user = userService.update(id, request);
        return Result.success(user);
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
```

### 自訂異常處理

```java
@Service
public class UserService {

    public User getById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用戶不存在");
        }
        return user;
    }

    public User create(CreateUserRequest request) {
        // 驗證輸入
        ValidationUtil.validateNotNull(request.getUsername(), "username", "用戶名不能為空");
        ValidationUtil.validateEmail(request.getEmail(), "email", "郵箱格式不正確");

        // 檢查用戶名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "用戶名已存在");
        }

        return userRepository.save(request.toEntity());
    }
}
```

### 實體類別定義

```java
// 繼承 TenantEntity 自動獲得租戶隔離和稽核欄位
@Entity
@Table(name = "users")
public class User extends TenantEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String fullName;

    @Column
    private Boolean enabled = true;

    // getters and setters...
}
```

### 快取使用

```java
@Service
public class UserService {

    @Autowired
    private CacheService cacheService;

    public User getById(Long id) {
        String cacheKey = CacheKeyUtil.buildKey("user", id);

        // 嘗試從快取獲取
        User user = cacheService.get(cacheKey, User.class);
        if (user != null) {
            return user;
        }

        // 從資料庫查詢
        user = userRepository.findById(id);
        if (user != null) {
            // 存入快取，設定 30 分鐘過期
            cacheService.set(cacheKey, user, 30, TimeUnit.MINUTES);
        }

        return user;
    }
}
```

### 分散式鎖定

```java
@Service
public class OrderService {

    @Autowired
    private DistributedLock distributedLock;

    public Order createOrder(CreateOrderRequest request) {
        String lockKey = LockKeyGenerator.generateKey("order:create", request.getUserId());

        try {
            // 獲取分散式鎖，防止重複下單
            if (!distributedLock.tryLock(lockKey, 30, TimeUnit.SECONDS)) {
                throw new BusinessException("ORDER_CREATING", "訂單處理中，請稍後重試");
            }

            // 檢查庫存
            if (!inventoryService.checkStock(request.getProductId(),
                    request.getQuantity())) {
                throw new BusinessException("INSUFFICIENT_STOCK", "庫存不足");
            }

            // 扣減庫存
            inventoryService.deductStock(request.getProductId(), request.getQuantity());

            // 建立訂單
            Order order = orderRepository.save(request.toEntity());

            return order;

        } finally {
            // 釋放鎖
            distributedLock.unlock(lockKey);
        }
    }
}
```

### 日誌追蹤

```java
@RestController
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private TraceContext traceContext;

    @GetMapping("/products/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        // 獲取當前請求的 Trace ID
        String traceId = traceContext.getTraceId();
        logger.info("查詢商品資訊，商品ID: {}, Trace ID: {}", id, traceId);

        Product product = productService.getById(id);
        return Result.success(product);
    }
}
```

## 🔒 多租戶支援

Common Layer 內建多租戶支援：

- **自動租戶注入**: 所有資料庫操作自動注入 `tenant_id`
- **請求級別隔離**: HTTP 請求自動提取 `X-Tenant-Id` 標頭
- **ThreadLocal 管理**: 安全的線程隔離，自動清理防止記憶體洩漏
- **稽核追蹤**: 自動記錄操作用戶和時間戳

## 📊 監控與追蹤

### MDC 日誌追蹤

所有 HTTP 請求自動生成 Trace ID，並注入到 MDC：

```log
2025-11-24 15:30:15 [http-nio-8080-exec-1] INFO  c.r.c.w.a.RequestLogAspect - \
[TRACE:550e8400-e29b-41d4-a716-446655440000] GET /api/users/123 - 200 OK - 45ms
```

### 效能指標

- **資料庫連線池**: HikariCP 提供連線池監控
- **Redis 連線**: Lettuce 客戶端提供連線狀態監控
- **快取命中率**: 快取服務提供命中率統計

## 🧪 測試支援

### 單元測試

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_ShouldSuccess() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");

        User user = userService.create(request);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).equals("testuser");
    }
}
```

### 整合測試

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser_ShouldAutoInjectTenantId() {
        // 設定租戶上下文
        TenantContextHolder.setTenantId("tenant-123");

        User user = new User();
        user.setUsername("testuser");

        User saved = userRepository.save(user);

        assertThat(saved.getTenantId()).isEqualTo("tenant-123");
        assertThat(saved.getCreateTime()).isNotNull();
    }
}
```

## 📋 版本資訊

- **版本**: 1.0.0
- **Java 版本**: 17+
- **Spring Boot**: 3.5.x
- **資料庫**: PostgreSQL 15+
- **快取**: Redis 7+

## 🤝 貢獻指南

1. Fork 此專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

## 📄 授權

此專案採用 MIT 授權 - 詳見 [LICENSE](LICENSE) 檔案

## 📞 聯絡資訊

- **專案維護者**: RBAC Team
- **問題回報**: [GitHub Issues](https://github.com/kkwenFreemind/rbac-system-v1/issues)
- **文件**: [Wiki](https://github.com/kkwenFreemind/rbac-system-v1/wiki)
