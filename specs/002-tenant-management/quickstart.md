# 快速開始 - 租戶管理模組

> **版本**: 1.0.0
> **更新日期**: 2025-01-XX
> **狀態**: Phase 1 設計

---

## 概述

本指南幫助開發者快速搭建租戶管理模組的開發環境,並提供開發、測試、部署的完整流程。

---

## 前置需求

### 必要軟體

| 軟體 | 版本 | 用途 | 下載連結 |
|-----|------|------|---------|
| **JDK** | 17+ | Java 執行環境 | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| **Maven** | 3.9+ | 建構工具 | [Maven](https://maven.apache.org/download.cgi) |
| **PostgreSQL** | 15+ | 主資料庫 | [PostgreSQL](https://www.postgresql.org/download/) |
| **Redis** | 7+ | 快取和分散式鎖 | [Redis](https://redis.io/download) |
| **Git** | 2.40+ | 版本控制 | [Git](https://git-scm.com/downloads) |
| **Docker** | 24+ | 容器化環境（可選） | [Docker](https://www.docker.com/products/docker-desktop) |

### 開發工具（推薦）

- **IDE**: IntelliJ IDEA 2024+ 或 Eclipse 2024+
- **REST 客戶端**: Postman 或 Insomnia
- **資料庫客戶端**: DBeaver 或 pgAdmin
- **Redis 客戶端**: RedisInsight 或 Another Redis Desktop Manager

---

## 環境設定

### 1. Clone 專案

```bash
git clone https://github.com/your-org/rbac-system-v1.git
cd rbac-system-v1
git checkout 001-tenant-management
```

### 2. 啟動資料庫（Docker）

使用 Docker Compose 快速啟動 PostgreSQL 和 Redis：

```bash
cd docker
docker-compose up -d postgres redis
```

驗證服務啟動：

```bash
docker ps
# 應該看到 postgres:15 和 redis:7-alpine 正在執行
```

### 3. 初始化資料庫

執行資料庫遷移腳本：

```bash
# 進入 PostgreSQL 容器
docker exec -it rbac-postgres psql -U rbac -d rbac_db

# 執行初始化 SQL
\i /scripts/init.sql

# 驗證表結構
\dt
# 應該看到 tenants 表
```

或使用 Flyway 自動遷移（推薦）：

```bash
cd backend
mvn flyway:migrate
```

### 4. 配置 application.yml

編輯 `backend/rbac-tenant/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rbac_db
    username: rbac
    password: your_password_here
    driver-class-name: org.postgresql.Driver

  redis:
    host: localhost
    port: 6379
    password: your_redis_password_here
    database: 0

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

logging:
  level:
    com.rbac.tenant: DEBUG
    com.rbac.common: DEBUG
```

### 5. 建構專案

```bash
cd backend
mvn clean install -DskipTests
```

**預期輸出**:
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for rbac-parent 1.0.0:
[INFO]
[INFO] rbac-parent ........................................ SUCCESS [  0.123 s]
[INFO] rbac-common ........................................ SUCCESS [  0.456 s]
[INFO] rbac-common-core ................................... SUCCESS [  1.234 s]
[INFO] rbac-common-database ............................... SUCCESS [  2.345 s]
[INFO] rbac-common-redis .................................. SUCCESS [  1.567 s]
[INFO] rbac-common-web .................................... SUCCESS [  1.890 s]
[INFO] rbac-tenant ........................................ SUCCESS [  3.456 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 執行應用程式

### 開發模式

```bash
cd backend/rbac-tenant
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 生產模式

```bash
java -jar target/rbac-tenant-1.0.0.jar --spring.profiles.active=prod
```

### 驗證啟動

應用程式預設在 `http://localhost:8080` 啟動。

訪問健康檢查端點：

```bash
curl http://localhost:8080/actuator/health
```

**預期回應**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

---

## 測試 API

### 1. 取得 JWT Token

（假設已實作認證模組）

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**回應**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

### 2. 建立租戶

```bash
curl -X POST http://localhost:8080/api/v1/tenants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-Id: 1" \
  -d '{
    "name": "Acme Corporation",
    "contactEmail": "admin@acme.com",
    "planType": "BASIC",
    "description": "重要客戶"
  }'
```

**預期回應**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1234567890123456789,
    "name": "Acme Corporation",
    "contactEmail": "admin@acme.com",
    "planType": "BASIC",
    "planTypeDescription": "基礎版",
    "status": "ACTIVE",
    "statusDescription": "啟用",
    "description": "重要客戶",
    "createdAt": "2025-01-15T10:30:00",
    "createdBy": 1000000000000000001,
    "updatedAt": "2025-01-15T10:30:00",
    "updatedBy": 1000000000000000001
  }
}
```

### 3. 查詢租戶清單

```bash
curl -X GET "http://localhost:8080/api/v1/tenants?pageNum=1&pageSize=20&status=ACTIVE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-Id: 1"
```

### 4. 取得租戶詳情

```bash
curl -X GET http://localhost:8080/api/v1/tenants/1234567890123456789 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-Id: 1"
```

### 5. 更新租戶

```bash
curl -X PUT http://localhost:8080/api/v1/tenants/1234567890123456789 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-Id: 1" \
  -d '{
    "planType": "PRO",
    "description": "已升級至專業版"
  }'
```

### 6. 變更租戶狀態

```bash
curl -X PATCH http://localhost:8080/api/v1/tenants/1234567890123456789/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-Id: 1" \
  -d '{
    "status": "SUSPENDED",
    "reason": "逾期付款"
  }'
```

### 7. 刪除租戶

```bash
curl -X DELETE http://localhost:8080/api/v1/tenants/1234567890123456789 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-Id: 1"
```

---

## 執行測試

### 單元測試

```bash
cd backend/rbac-tenant
mvn test
```

### 整合測試

```bash
mvn verify -P integration-test
```

### 測試覆蓋率

```bash
mvn clean test jacoco:report
```

查看報告：
```bash
open target/site/jacoco/index.html
```

**預期覆蓋率**:
- **行覆蓋率**: > 80%
- **分支覆蓋率**: > 70%

---

## 常見問題

### 1. PostgreSQL 連線失敗

**錯誤**:
```
org.postgresql.util.PSQLException: Connection refused
```

**解決方案**:
- 檢查 PostgreSQL 是否啟動: `docker ps | grep postgres`
- 檢查 `application-dev.yml` 中的連線設定
- 確認防火牆未阻擋 5432 埠

### 2. Redis 連線失敗

**錯誤**:
```
redis.clients.jedis.exceptions.JedisConnectionException
```

**解決方案**:
- 檢查 Redis 是否啟動: `docker ps | grep redis`
- 檢查 `application-dev.yml` 中的 Redis 設定
- 確認防火牆未阻擋 6379 埠

### 3. 租戶上下文未設定

**錯誤**:
```
TenantContextException: 租戶上下文未設定
```

**解決方案**:
- 確認 HTTP Header 包含 `X-Tenant-Id`
- 檢查 `TenantFilter` 是否正確註冊
- 檢查 Filter 執行順序（應在認證之後）

### 4. MyBatis 攔截器未生效

**症狀**: 查詢返回所有租戶的資料（未過濾）

**解決方案**:
- 檢查 `MyBatisPlusConfig` 是否配置 `TenantLineInnerInterceptor`
- 確認 `TenantContextHolder` 中有值
- 檢查表是否在 `ignoreTables` 清單中

### 5. 測試失敗：Testcontainers 無法啟動

**錯誤**:
```
org.testcontainers.containers.ContainerLaunchException
```

**解決方案**:
- 確認 Docker 正在執行: `docker info`
- 檢查 Docker Desktop 設定（記憶體、CPU 限制）
- 確認網路連線正常（拉取映像檔）

---

## 開發工作流程

### 1. 建立新功能分支

```bash
git checkout -b feature/tenant-list-export
```

### 2. 開發並本地測試

```bash
# 編寫程式碼
# 執行單元測試
mvn test

# 執行整合測試
mvn verify -P integration-test
```

### 3. 程式碼檢查

```bash
# Checkstyle 檢查
mvn checkstyle:check

# SpotBugs 檢查
mvn spotbugs:check
```

### 4. 提交變更

```bash
git add .
git commit -m "feat(tenant): 新增租戶清單匯出功能"
git push origin feature/tenant-list-export
```

### 5. 建立 Pull Request

- 在 GitHub 上建立 PR
- 等待 CI/CD 通過
- 請求 Code Review
- 合併至 `001-tenant-management` 分支

---

## 除錯技巧

### 1. 啟用 SQL 日誌

在 `application-dev.yml` 中:

```yaml
logging:
  level:
    com.rbac.tenant.mapper: DEBUG
    org.springframework.jdbc.core: DEBUG

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 2. 檢查租戶上下文

在 Controller 或 Service 中添加日誌：

```java
@GetMapping("/{id}")
public Result<TenantResponse> getTenantById(@PathVariable Long id) {
    Long tenantId = TenantContextHolder.getTenantId();
    log.debug("當前租戶 ID: {}", tenantId);
    // ...
}
```

### 3. 檢查 JWT Token

使用 [jwt.io](https://jwt.io/) 解析 Token 內容：

```json
{
  "sub": "admin",
  "tenant_id": 1234567890123456789,
  "authorities": ["tenant:read", "tenant:create"],
  "iat": 1705302600,
  "exp": 1705306200
}
```

### 4. 檢查 Redis 快取

使用 Redis CLI：

```bash
docker exec -it rbac-redis redis-cli

# 查看所有鍵
KEYS tenant:*

# 查看快取內容
GET tenant:1234567890123456789

# 清除快取
DEL tenant:1234567890123456789
```

### 5. 檢查 PostgreSQL 資料

```bash
docker exec -it rbac-postgres psql -U rbac -d rbac_db

# 查詢租戶資料
SELECT * FROM tenants WHERE deleted = false;

# 檢查索引
\d+ tenants
```

---

## 效能調優

### 1. 連線池設定

在 `application.yml` 中調整 HikariCP:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 2. Redis 連線池設定

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

### 3. 啟用 MyBatis-Plus 快取

```yaml
mybatis-plus:
  configuration:
    cache-enabled: true
    local-cache-scope: statement
```

### 4. JVM 參數調優

```bash
java -Xms512m -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar rbac-tenant-1.0.0.jar
```

---

## 部署

### Docker 部署

建立 Docker 映像檔：

```bash
cd backend/rbac-tenant
docker build -t rbac-tenant:1.0.0 .
```

執行容器：

```bash
docker run -d \
  --name rbac-tenant \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/rbac_db \
  -e SPRING_REDIS_HOST=redis \
  rbac-tenant:1.0.0
```

### Docker Compose 部署

使用 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  tenant-service:
    image: rbac-tenant:1.0.0
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/rbac_db
      SPRING_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis
```

執行：

```bash
docker-compose up -d
```

---

## 監控與告警

### Actuator 端點

訪問以下端點進行監控：

- **健康檢查**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **環境變數**: `GET /actuator/env`
- **日誌層級**: `GET /actuator/loggers`

### Prometheus 整合

在 `application.yml` 中啟用：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

訪問 `http://localhost:8080/actuator/prometheus` 取得 Metrics。

### 日誌收集

使用 Logback 輸出 JSON 格式日誌：

```xml
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

---

## 下一步

- ✅ **Phase 1 完成**: `data-model.md`, `contracts/`, `quickstart.md` 已生成
- 🔄 **Phase 2 待辦**: 執行 `tasks.md` 生成並開始實作
- 📖 **參考文件**: 查看 `docs/` 目錄下的架構設計文件

---

## 支援與聯絡

- **技術文件**: `docs/README.md`
- **API 文件**: `http://localhost:8080/swagger-ui.html`
- **Issue 追蹤**: [GitHub Issues](https://github.com/your-org/rbac-system-v1/issues)
- **團隊聯絡**: rbac-team@your-org.com
