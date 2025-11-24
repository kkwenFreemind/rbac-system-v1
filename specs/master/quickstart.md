# 快速入門指南：001-common-layer

**日期**：2025-11-24 | **階段**：Phase 1 - 設計與合約

## 概述

本指南提供逐步說明，用於在多租戶 RBAC 系統中設定和使用 Common Layer。Common Layer 必須優先實作，因為它為所有業務模組提供基礎設施。

## 前置需求

- **Java**：JDK 17 或更高版本
- **Maven**：3.8+ (或 Gradle 7.5+)
- **PostgreSQL**：14+ (建議使用 postgres:14-alpine 或 postgres:14)
  - 注意：TimescaleDB 為可選優化，僅在 Audit 模組需要時序數據優化時考慮
- **Redis**：6.x+ (單機或叢集)
- **IDE**：IntelliJ IDEA、Eclipse 或 VS Code with Java extensions

## 專案設定

### 步驟 1：建立 Maven 多模組專案

```bash
cd rbac-system-v1
```

為父專案建立 `pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.rbac</groupId>
    <artifactId>rbac-system</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>RBAC System Parent</name>
    <description>Multi-tenant RBAC System</description>

    <modules>
        <module>rbac-common/rbac-common-core</module>
        <module>rbac-common/rbac-common-database</module>
        <module>rbac-common/rbac-common-redis</module>
        <module>rbac-common/rbac-common-web</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <!-- Spring Boot -->
        <spring-boot.version>3.5.0</spring-boot.version>
        
        <!-- MyBatis-Plus -->
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        
        <!-- Database -->
        <postgresql.version>42.7.1</postgresql.version>
        
        <!-- Redis -->
        <jedis.version>4.4.3</jedis.version>
        
        <!-- Utilities -->
        <lombok.version>1.18.30</lombok.version>
        <jackson.version>2.15.3</jackson.version>
        
        <!-- Testing -->
        <junit.version>5.10.1</junit.version>
        <mockito.version>5.7.0</mockito.version>
        <testcontainers.version>1.19.3</testcontainers.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <!-- MyBatis-Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            
            <!-- PostgreSQL Driver -->
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <version>${postgresql.version}</version>
            </dependency>
            
            <!-- Lombok -->
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### 步驟 2：建立 Common Core 模組

建立 `rbac-common/rbac-common-core/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-system</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>rbac-common-core</artifactId>
    <name>RBAC Common Core</name>

    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**建立關鍵類別**（完整實作請參閱 data-model.md）：

- `com.rbac.common.core.exception.*` (RbacException、BusinessException 等)
- `com.rbac.common.core.result.*` (Result、ResultCode)
- `com.rbac.common.core.model.*` (PageRequest、PageResponse)
- `com.rbac.common.core.util.*` (StringUtil、DateUtil、JsonUtil 等)

### 步驟 3：建立 Common Database 模組

建立 `rbac-common/rbac-common-database/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-system</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>rbac-common-database</artifactId>
    <name>RBAC Common Database</name>

    <dependencies>
        <!-- Common Core -->
        <dependency>
            <groupId>com.rbac</groupId>
            <artifactId>rbac-common-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        
        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        
        <!-- HikariCP (connection pool) -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Testcontainers for integration tests -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**建立關鍵類別**：

- `com.rbac.common.database.entity.*` (BaseEntity、TenantEntity、AuditEntity)
- `com.rbac.common.database.context.TenantContextHolder`
- `com.rbac.common.database.interceptor.TenantInterceptor`
- `com.rbac.common.database.config.MyBatisPlusConfig`

### 步驟 4：建立 Common Redis 模組

建立 `rbac-common/rbac-common-redis/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-system</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>rbac-common-redis</artifactId>
    <name>RBAC Common Redis</name>

    <dependencies>
        <!-- Common Core -->
        <dependency>
            <groupId>com.rbac</groupId>
            <artifactId>rbac-common-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- Spring Data Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <!-- Lettuce (Redis client) -->
        <dependency>
            <groupId>io.lettuce</groupId>
            <artifactId>lettuce-core</artifactId>
        </dependency>
        
        <!-- Apache Commons Pool (for connection pooling) -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Testcontainers Redis -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**建立關鍵類別**：

- `com.rbac.common.redis.config.*` (RedisConfig、RedisProperties)
- `com.rbac.common.redis.util.*` (CacheService 介面、RedisCacheService、CacheKeyUtil)
- `com.rbac.common.redis.lock.*` (DistributedLock 介面、RedisDistributedLock)

### 步驟 5：建立 Common Web 模組

建立 `rbac-common/rbac-common-web/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.rbac</groupId>
        <artifactId>rbac-system</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>rbac-common-web</artifactId>
    <name>RBAC Common Web</name>

    <dependencies>
        <!-- Common Core -->
        <dependency>
            <groupId>com.rbac</groupId>
            <artifactId>rbac-common-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- Common Database (for TenantContextHolder) -->
        <dependency>
            <groupId>com.rbac</groupId>
            <artifactId>rbac-common-database</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- Spring Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- AOP -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**建立關鍵類別**：

- `com.rbac.common.web.filter.*` (TenantFilter、RequestLogFilter)
- `com.rbac.common.web.handler.GlobalExceptionHandler`
- `com.rbac.common.web.config.*` (WebMvcConfig、CorsConfig)

---

## 設定

### application.yml

建立 `src/main/resources/application.yml`：

```yaml
spring:
  application:
    name: rbac-system
  
  profiles:
    active: dev
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:rbac_system}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  # Redis Configuration
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0

# RBAC Configuration
rbac:
  tenant:
    header-name: X-Tenant-Id
    enabled: true
    excluded-tables:
      - sys_tenant
      - sys_config
  
  cache:
    enabled: true
    ttl: 1800
    prefix: rbac
  
  lock:
    timeout: 30
    retry-count: 3
    retry-delay: 100

# MyBatis-Plus Configuration
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:mapper/**/*.xml

# Logging
logging:
  level:
    com.rbac: DEBUG
    org.springframework: INFO
    com.baomidou.mybatisplus: DEBUG
```

---

## 使用範例

### 範例 1：使用 Result<T> 進行 API 回應

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        if (user == null) {
            return Result.error(ErrorCode.RESOURCE_NOT_FOUND, "User not found");
        }
        return Result.success(user);
    }
    
    @PostMapping
    public Result<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return Result.success(user);
    }
    
    @GetMapping
    public Result<PageResponse<UserDTO>> listUsers(
        @Valid @ModelAttribute PageRequest pageRequest,
        @RequestParam(required = false) String keyword
    ) {
        PageResponse<UserDTO> users = userService.listUsers(pageRequest, keyword);
        return Result.success(users);
    }
}
```

### 範例 2：使用 TenantEntity

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends AuditEntity {
    
    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer status;
    
    // tenantId、createdBy、createdAt、updatedBy、updatedAt 為繼承欄位
}

// Service 層 - tenant_id 自動注入
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encryptPassword(request.getPassword()));
        
        // 無需手動設定 tenantId！
        // TenantInterceptor 會自動注入
        userMapper.insert(user);
        
        return user;
    }
}
```

### 範例 3：使用 CacheService

```java
@Service
public class UserService {
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private UserMapper userMapper;
    
    public User getUserById(Long id) {
        // 1. 先嘗試從快取讀取
        String cacheKey = CacheKeyUtil.userKey(id);
        User user = cacheService.get(cacheKey, User.class);
        
        if (user != null) {
            log.debug("Cache hit for user: {}", id);
            return user;
        }
        
        // 2. 從資料庫載入
        user = userMapper.selectById(id);
        
        // 3. 儲存到快取，TTL 為 30 分鐘
        if (user != null) {
            cacheService.set(cacheKey, user, 1800);
        }
        
        return user;
    }
    
    public void updateUser(Long id, UpdateUserRequest request) {
        // 更新資料庫
        User user = userMapper.selectById(id);
        user.setEmail(request.getEmail());
        userMapper.updateById(user);
        
        // 清除快取
        String cacheKey = CacheKeyUtil.userKey(id);
        cacheService.delete(cacheKey);
    }
}
```

### 範例 4：使用 DistributedLock

```java
@Service
public class RoleService {
    
    @Autowired
    private DistributedLock distributedLock;
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    public void assignPermission(Long roleId, Long permissionId) {
        String lockKey = "lock:role:assign:" + roleId;
        
        // 使用鎖定執行
        distributedLock.executeWithLock(lockKey, 30, TimeUnit.SECONDS, () -> {
            // 檢查是否已分配
            LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
            query.eq(RolePermission::getRoleId, roleId);
            query.eq(RolePermission::getPermissionId, permissionId);
            
            if (rolePermissionMapper.selectCount(query) == 0) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        });
    }
}
```

### 範例 5：使用 TenantFilter

`TenantFilter` 會自動套用到所有 HTTP 請求。加入到您的主應用程式：

```java
@Configuration
public class WebFilterConfig {
    
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration() {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("tenantFilter");
        return registration;
    }
}
```

**客戶端請求範例**：

```bash
curl -X GET "http://localhost:8080/api/v1/users/123" \
  -H "X-Tenant-Id: tenant_abc" \
  -H "Authorization: Bearer <jwt-token>"
```

過濾器會提取 `tenant_abc` 並設定到 `TenantContextHolder`，然後由 `TenantInterceptor` 用於過濾資料庫查詢。

---

## 測試

### 單元測試範例

```java
@SpringBootTest
public class CacheServiceTest {
    
    @Autowired
    private CacheService cacheService;
    
    @Test
    public void testSetAndGet() {
        String key = "test:user:123";
        User user = new User();
        user.setId(123L);
        user.setUsername("test");
        
        cacheService.set(key, user, 60);
        
        User cached = cacheService.get(key, User.class);
        
        assertNotNull(cached);
        assertEquals(123L, cached.getId());
        assertEquals("test", cached.getUsername());
    }
    
    @Test
    public void testDelete() {
        String key = "test:delete:456";
        cacheService.set(key, "value", 60);
        
        assertTrue(cacheService.exists(key));
        
        cacheService.delete(key);
        
        assertFalse(cacheService.exists(key));
    }
}
```

### 整合測試範例（使用 Testcontainers）

```java
@SpringBootTest
@Testcontainers
public class TenantInterceptorIntegrationTest {
    
    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
        .withDatabaseName("rbac_test")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    public void testTenantIsolation() {
        // 設定租戶上下文
        TenantContextHolder.setTenantId("123");
        
        // 在租戶 123 中建立使用者
        User user1 = new User();
        user1.setUsername("user1");
        userMapper.insert(user1);
        
        // 查詢應只返回租戶 123 的使用者
        List<User> users = userMapper.selectList(null);
        assertEquals(1, users.size());
        assertEquals(123L, users.get(0).getTenantId());
        
        // 切換到租戶 456
        TenantContextHolder.clear();
        TenantContextHolder.setTenantId("456");
        
        // 應該看不到租戶 456 的使用者
        users = userMapper.selectList(null);
        assertEquals(0, users.size());
        
        // 清理
        TenantContextHolder.clear();
    }
}
```

---

## 建置與執行

### 建置所有模組

```bash
mvn clean install
```

### 執行測試

```bash
# 執行所有測試
mvn test

# 執行特定測試類別
mvn test -Dtest=CacheServiceTest

# 執行並產生覆蓋率報告
mvn test jacoco:report
```

### 打包

```bash
mvn clean package
```

---

## 疑難排解

### 問題：租戶上下文未設定

**錯誤**：`TenantException: Tenant context not set during entity creation`

**解決方案**：確保 `TenantFilter` 已註冊並在資料庫操作前執行。使用 `@Order(Ordered.HIGHEST_PRECEDENCE)` 檢查過濾器順序。

### 問題：ThreadLocal 記憶體洩漏

**錯誤**：前一個請求的租戶上下文在下一個請求中仍可見

**解決方案**：驗證在 `TenantFilter.doFilterInternal()` 的 `finally` 區塊中有呼叫 `TenantContextHolder.clear()`。

### 問題：Redis 連線被拒絕

**錯誤**：`Could not connect to Redis at localhost:6379`

**解決方案**：

1. 檢查 Redis 是否正在執行：`redis-cli ping`（應返回 PONG）
2. 驗證 `application.yml` 中的 Redis host/port
3. 檢查防火牆設定

### 問題：MyBatis 租戶攔截器未運作

**錯誤**：查詢返回所有租戶的資料

**解決方案**：

1. 驗證 `MyBatisPlusConfig` 已載入（加入 `@Configuration`）
2. 檢查 `TenantLineHandler.getTenantId()` 返回正確值
3. 確保實體繼承 `TenantEntity`
4. 驗證資料表有 `tenant_id` 欄位並建立索引

---

## 後續步驟

設定 Common Layer 後：

1. **驗證安裝**：執行所有測試以確保設定正確
2. **建立測試應用程式**：建置簡單的 REST API 來測試 Common Layer 元件
3. **進行業務模組開發**：開始實作 Auth Module、User Module 等
4. **檢閱文件**：閱讀 `data-model.md` 和 `internal-contracts.md` 以了解 API 詳情

---

## 額外資源

- [MyBatis-Plus 文件](https://baomidou.com/)
- [Spring Boot Redis 整合](https://spring.io/guides/gs/messaging-redis/)
- [專案憲章](../../.specify/memory/constitution.md)
- [低耦合設計指南](../../docs/04-低耦合設計指南.md)
- [多租戶隔離策略](../../docs/02-多租戶隔離策略.md)

---

## 支援

如有問題或疑問：

- 檢閱 `/docs` 資料夾中的專案文件
- 查看 `research.md` 以了解技術決策
- 參考 `internal-contracts.md` 以了解 API 規格
