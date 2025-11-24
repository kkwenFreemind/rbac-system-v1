# 實作計劃：001-common-layer

**分支**: `001-common-layer` | **日期**: 2025-11-24 | **規格**: [spec.md](./spec.md)
**輸入**: 來自 `/specs/master/spec.md` 的功能規格

**注意**: 此計劃實作了多租戶 RBAC 系統的基礎 Common Layer，為所有業務模組提供基礎架構。

## 總結

實作 Common Layer (公共層) 作為多租戶 RBAC 系統的基礎架構。此層提供：

- 統一的異常處理和響應格式化
- 多租戶數據隔離架構 (TenantContextHolder, MyBatis 攔截器)
- Redis 快取和分散式鎖定支援
- 資料庫存取工具和基礎實體類別
- Web 層通用元件 (過濾器、攔截器、全域異常處理器)

沒有此基礎，就無法開發任何業務模組 (Auth, User, Tenant, Permission)。Common Layer 確保一致的行為、程式碼可重用性，並強制執行憲法的租戶隔離和低耦合要求。

## 技術背景

**語言/版本**: Java 17 (LTS), Spring Boot 3.5.x  
**主要依賴**:

- Spring Boot Starter Web 3.5.x
- Spring Boot Starter Data Redis 3.5.x
- MyBatis-Plus 3.5.x (或 MyBatis 3.5.x + Spring 整合)
- Jedis 4.x / Lettuce 6.x (Redis 客戶端)
- Jackson 2.17.x (JSON 序列化)
- Lombok 1.18.x (程式碼生成)
- SLF4J + Logback (日誌記錄)

**儲存**:

- PostgreSQL 14+ (主要資料庫，建議使用官方 postgres:14-alpine 映像檔)
  - 注意: TimescaleDB 為可選優化，僅在未來 Audit 模組需要時序數據優化時考慮
- Redis 6.x+ (快取和分散式鎖定)

**測試**:

- JUnit 5 (Jupiter)
- Spring Boot Test
- Mockito 4.x
- Testcontainers (用於真實 Redis/PostgreSQL 的整合測試)

**目標平台**: JVM 伺服器環境 (Linux/Windows)，可部署為 JAR 或容器  
**專案類型**: 多模組 Maven 專案 (單體式，具微服務演進潛力)  
**效能目標**:

- Redis 快取操作 < 10ms (p95)
- 分散式鎖定獲取 < 50ms (正常情況)
- MyBatis 攔截器開銷 < 5ms 每查詢

**約束**:

- 100% 租戶隔離強制執行 (零跨租戶數據洩漏)
- 具保證清理的執行緒安全 TenantContextHolder
- 所有異常必須由全域處理器捕獲
- 配置必須支援外部化 (環境變數、配置文件)

**規模/範圍**:

- 初始支援 1000+ 租戶
- 處理 10,000+ 並發請求
- 模組化設計支援 10+ 業務模組
- 整個系統 500K+ 行程式碼的基礎

## 憲法檢查

*門檻: 必須在 Phase 0 研究前通過。Phase 1 設計後重新檢查。*

### 核心原則合規性

| 原則 | 狀態 | 合規性說明 |
|-----------|--------|------------------|
| **I. 模組化低耦合架構** | ✅ PASS | Common Layer 提供基於介面的抽象（`CacheService`、`UserContext`）。**UserContext 介面**解決 Common-Auth 循環依賴，實現依賴倒置原則（DIP）。模組邊界清晰。 |
| **II. 預設多租戶隔離** | ✅ PASS | TenantContextHolder + MyBatis `TenantLineInnerInterceptor` 自動過濾 SELECT/UPDATE/DELETE。`MetaObjectHandler` 自動填充 INSERT 時的 `tenant_id`。ThreadLocal 清理在 finally 區塊中得到保證。 |
| **III. 安全性優先開發** | ✅ PASS | 全域異常處理器防止敏感數據洩漏。**Snowflake ID** 替代自增主鍵防止業務量洩露。審計追蹤架構透過 `UserContext` 介面實現。 |
| **IV. 所有關鍵操作的稽核軌跡** | ✅ PASS | `AuditEntity` + `MetaObjectHandler` 自動記錄 created_by/updated_by。**MDC Trace ID** 提供請求追蹤和日誌串聯。審計實作解耦到 Auth 模組。 |
| **V. 關鍵路徑的測試驅動開發** | ✅ PASS | 租戶隔離邏輯和 TenantContextHolder 的 TDD 方法。目標: 70%+ 覆蓋率。MyBatis 攔截器的整合測試。 |
| **VI. API 優先設計與版本控制** | ✅ PASS | Common Web 提供版本化 API 支援 (`/api/v1/`)。標準化的 Result<T> 響應格式包含 code、message、data、timestamp。 |

### 安全與合規性

| 要求 | 狀態 | 實作 |
|-------------|--------|----------------|
| **敏感配置外部化** | ✅ PASS | 所有配置通過 application.yml 並支援環境變數。無硬編碼憑證。 |
| **異常信息脫敏** | ✅ PASS | 全域異常處理器在生產模式下過濾堆疊追蹤。敏感數據永遠不會在錯誤響應中暴露。 |
| **SQL注入防護** | ✅ PASS | MyBatis parameterized queries enforced. No dynamic SQL string concatenation in common layer. |
| **分散式系統就緒** | ✅ PASS | Snowflake ID（ASSIGN_ID）取代 IDENTITY，支援分庫分表和微服務演進。MDC Trace ID 支援分散式追蹤。 |
| **依賴解耦** | ✅ PASS | UserContext 介面實現依賴倒置，Common Layer 不依賴業務模組（Auth/Security）。 |

### 品質標準

| 標準 | 狀態 | 目標 |
|----------|--------|--------|
| **程式碼覆蓋率** | 🟡 待處理 | 70%+ 單元測試覆蓋率 |
| **API響應時間** | 🟡 待處理 | p95 < 200ms (在 Phase 1 中驗證) |
| **索引設計** | ✅ PASS | BaseEntity 包含 tenant_id 並在文檔中提供索引指導 |

**門檻狀態**: ✅ **通過** - 所有憲法要求均已滿足。繼續 Phase 0。

## 專案結構

### 文檔 (此功能)

```text
specs/master/
├── plan.md              # 此檔案 (/speckit.plan 命令輸出)
├── spec.md              # 功能規格
├── research.md          # Phase 0 輸出 (技術決策與最佳實務)
├── data-model.md        # Phase 1 輸出 (實體模型)
├── quickstart.md        # Phase 1 輸出 (設定與使用指南)
└── contracts/           # Phase 1 輸出 (API 契約，如果適用)
```

### 原始碼 (儲存庫根目錄)

```text
backend/
├── rbac-common/                      # Common Layer Root Module
│   ├── rbac-common-core/             # Core utilities & base models
│   │   ├── src/main/java/com/rbac/common/core/
│   │   │   ├── exception/            # Exception classes
│   │   │   │   ├── RbacException.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── SystemException.java
│   │   │   │   ├── TenantException.java
│   │   │   │   └── PermissionDeniedException.java
│   │   │   ├── result/               # Unified response format
│   │   │   │   ├── Result.java
│   │   │   │   ├── ResultCode.java
│   │   │   │   └── PageResult.java
│   │   │   ├── model/                # Base data models
│   │   │   │   ├── PageRequest.java
│   │   │   │   ├── PageResponse.java
│   │   │   │   └── AuditInfo.java
│   │   │   ├── constant/             # Constants
│   │   │   │   ├── ErrorCode.java
│   │   │   │   ├── CommonConstant.java
│   │   │   │   └── TenantConstant.java
│   │   │   └── util/                 # Utility classes
│   │   │       ├── StringUtil.java
│   │   │       ├── DateUtil.java
│   │   │       ├── JsonUtil.java
│   │   │       ├── EncryptUtil.java
│   │   │       └── ValidationUtil.java
│   │   └── src/test/java/
│   │
│   ├── rbac-common-database/         # Database common module
│   │   ├── src/main/java/com/rbac/common/database/
│   │   │   ├── entity/               # Base entity classes
│   │   │   │   ├── BaseEntity.java
│   │   │   │   ├── TenantEntity.java
│   │   │   │   └── AuditEntity.java
│   │   │   ├── interceptor/          # MyBatis 攔截器
│   │   │   │   ├── TenantInterceptor.java
│   │   │   │   └── AuditInterceptor.java
│   │   │   ├── config/               # Database configuration
│   │   │   │   ├── MyBatisConfig.java
│   │   │   │   ├── DataSourceConfig.java
│   │   │   │   └── DynamicDataSourceRouter.java
│   │   │   ├── context/              # Context holders
│   │   │   │   └── TenantContextHolder.java
│   │   │   └── util/                 # Database utilities
│   │   │       └── SqlUtil.java
│   │   └── src/test/java/
│   │
│   ├── rbac-common-redis/            # Redis common module
│   │   ├── src/main/java/com/rbac/common/redis/
│   │   │   ├── config/               # Redis configuration
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── RedisProperties.java
│   │   │   ├── lock/                 # Distributed lock
│   │   │   │   ├── DistributedLock.java (interface)
│   │   │   │   ├── RedisDistributedLock.java
│   │   │   │   └── LockKeyGenerator.java
│   │   │   └── util/                 # Cache utilities
│   │   │       ├── CacheService.java (interface)
│   │   │       ├── RedisCacheService.java
│   │   │       └── CacheKeyUtil.java
│   │   └── src/test/java/
│   │
│   └── rbac-common-web/              # Web common module
│       ├── src/main/java/com/rbac/common/web/
│       │   ├── filter/               # 過濾器
│       │   │   ├── TenantFilter.java
│       │   │   └── RequestLogFilter.java
│       │   ├── interceptor/          # 攔截器
│       │   │   └── PermissionInterceptor.java
│       │   ├── handler/              # 異常處理器
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── config/               # Web configuration
│       │   │   ├── WebMvcConfig.java
│       │   │   ├── CorsConfig.java
│       │   │   └── ApiVersionConfig.java
│       │   └── aspect/               # AOP 切面
│       │       └── RequestLogAspect.java
│       └── src/test/java/
│
├── docs/                             # Documentation (existing)
└── specs/                            # Specifications
```

**結構決策**: 為 Common Layer 選擇多模組 Maven 專案結構。這符合憲法的模組化原則，並實現：

- 明確的關注點分離 (core, database, redis, web)
- 模組的獨立版本控制和部署
- 易於演進到微服務（如有需要）
- 業務模組將僅依賴所需通用模組 (例如，User 模組依賴 common-core + common-database，但如果不需要快取則不依賴 common-redis)

## 複雜度追蹤

> **無違規** - 所有憲法要求均在無妥協的情況下滿足。Common Layer 設計遵循既定模式 (Repository, DTO, ThreadLocal context)，這些是多租戶系統的業界標準，並在憲法中明確推薦。

### 架構改進 (基於審查報告)

以下改進確保 Common Layer 符合長期架構演進要求：

1. **ID 生成策略**
   - ❌ 原設計：`GenerationType.IDENTITY` (資料庫自增)
   - ✅ 改進後：`IdType.ASSIGN_ID` (Snowflake 雪花演算法)
   - 理由：支援分庫分表、數據遷移、微服務演進，防止業務量洩露

2. **審計依賴解耦**
   - ❌ 原問題：AuditEntity 註解掉 SecurityContext 呼叫，無法自動填充使用者
   - ✅ 解決方案：定義 `UserContext` 介面，Auth 模組實作，Common 層僅依賴抽象
   - 理由：實現依賴倒置原則（DIP），避免循環依賴

3. **租戶 ID 注入機制**
   - ❌ 原描述：TenantInterceptor 自動注入 INSERT
   - ✅ 澄清機制：使用 `MetaObjectHandler` 填充 INSERT，`TenantLineInnerInterceptor` 過濾 SELECT/UPDATE/DELETE
   - 理由：MyBatis-Plus 原生租戶插件主要用於過濾，填充需要 MetaObjectHandler

4. **分散式追蹤**
   - ❌ 原疏漏：缺少 Trace ID 傳遞機制
   - ✅ 新增功能：MDC Trace ID 過濾器，自動生成/傳遞/清理 Trace ID
   - 理由：日誌串聯、問題排查、未來微服務追蹤的基礎

5. **資料庫選型**
   - ❌ 原配置：timescaledb-ha:pg14-latest
   - ✅ 簡化為：postgres:14-alpine（TimescaleDB 延後到 Audit 模組需要時）
   - 理由：Common Layer 不需要時序數據庫特性，降低基礎設施複雜度

### 技術債務

- 無已知技術債務。所有設計決策均符合最佳實務和長期架構目標。
