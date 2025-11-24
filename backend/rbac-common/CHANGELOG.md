# 更新日誌

此專案的所有重要變更都將記錄在此檔案中。

此格式基於 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)，
並遵循 [語意化版本](https://semver.org/spec/v2.0.0.html)。

## [1.0.0] - 2025-11-24

### 新增功能

#### 核心模組 (`rbac-common-core`)

- **異常處理**: 新增完整的異常層次結構
  - `RbacException`: 基礎異常類別
  - `BusinessException`: 業務邏輯錯誤
  - `SystemException`: 系統層級錯誤
  - `TenantException`: 租戶相關錯誤
  - `PermissionDeniedException`: 授權失敗

- **統一響應格式**: 實作 `Result<T>` 類別以提供一致的 API 響應
  - 支援成功和錯誤響應
  - 泛型支援資料包裝
  - 標準化錯誤代碼處理

- **工具類別**: 新增必要的工具函數
  - `StringUtil`: 字串處理工具
  - `DateUtil`: 日期時間處理工具
  - `JsonUtil`: JSON 序列化/反序列化
  - `EncryptUtil`: 加密和雜湊工具
  - `ValidationUtil`: 輸入驗證工具

- **核心模型**: 新增基礎資料模型
  - `PageRequest`: 分頁請求參數
  - `PageResponse`: 分頁響應包裝器
  - `AuditInfo`: 稽核追蹤資訊

- **常數和配置**:
  - `CommonConstant`: 通用應用常數
  - `TenantConstant`: 租戶相關常數
  - `ErrorCode`: 標準化錯誤代碼
  - `ResultCode`: 響應狀態代碼
  - `RbacProperties`: 配置屬性

#### 資料庫模組 (`rbac-common-database`)

- **多租戶隔離**: 實作租戶資料隔離
  - `TenantEntity`: 支援租戶的基礎實體
  - `AuditEntity`: 支援稽核追蹤的實體
  - `TenantContextHolder`: Thread-local 租戶上下文管理
  - `TenantLineInnerInterceptor`: MyBatis 攔截器用於自動租戶過濾

- **實體基礎類別**:
  - `BaseEntity`: 支援 Snowflake ID 產生的基礎實體
  - 自動稽核欄位填充 (createTime, updateTime, createBy, updateBy)

- **資料庫配置**:
  - HikariCP 連線池優化
  - 動態資料來源路由 (為讀寫分離做準備)
  - MyBatis-Plus 整合租戶隔離

- **資料庫工具**:
  - `SqlUtil`: SQL 相關工具函數

#### Redis 模組 (`rbac-common-redis`)

- **快取服務**: 基於 Redis 的快取實作
  - `CacheService`: 統一快取介面
  - `RedisCacheService`: Redis 實作搭配 Jackson 序列化
  - `CacheKeyUtil`: 標準化快取鍵產生

- **分散式鎖定**: 基於 Redis 的分散式鎖實作
  - `DistributedLock`: 分散式鎖定介面
  - `RedisDistributedLock`: Redis 實作搭配 Lua 腳本
  - `LockKeyGenerator`: 標準化鎖鍵產生

- **Redis 配置**:
  - Lettuce 客戶端配置
  - 連線池和超時設定
  - 自訂複雜物件序列化

#### Web 模組 (`rbac-common-web`)

- **全域異常處理**: 集中式錯誤處理
  - `GlobalExceptionHandler`: 統一錯誤響應格式化
  - 自動將異常轉換為 `Result<T>` 格式

- **請求處理過濾器**:
  - `TenantFilter`: 自動從 HTTP 標頭提取租戶 ID
  - `TraceIdFilter`: 請求追蹤搭配 MDC 整合
  - `RequestLogFilter`: 請求記錄和監控

- **HTTP 攔截器**:
  - `TenantCleanupInterceptor`: 租戶上下文的 Thread-local 清理

- **Web 配置**:
  - `WebMvcConfig`: 過濾器和攔截器註冊
  - `CorsConfig`: 跨來源資源共享配置
  - `ApiVersionConfig`: API 版本支援

- **請求記錄和追蹤**:
  - `RequestLogAspect`: 基於 AOP 的請求記錄
  - `TraceContext`: MDC 包裝器用於追蹤 ID 管理

### 技術細節

- **Java 版本**: 17+
- **Spring Boot**: 3.5.x
- **資料庫**: PostgreSQL 15+
- **快取**: Redis 7+
- **建置工具**: Maven 3.6+
- **ID 產生**: 分散式 ID 產生的 Snowflake 演算法
- **序列化**: JSON 處理的 Jackson
- **連線池**: 資料庫連線的 HikariCP
- **ORM**: 資料庫操作的 MyBatis-Plus

### 安全性功能

- 資料庫層級的多租戶資料隔離
- 防止資料洩漏的自動租戶上下文清理
- 並發操作安全的分散式鎖定
- 安全輸入驗證工具
- 所有資料修改的稽核追蹤

### 測試

- 完整的單元測試覆蓋
- 搭配 Testcontainers 的整合測試
- 多租戶隔離驗證
- Thread-local 清理驗證
- 分散式鎖並發測試

### 文件

- 包含使用範例的完整 README.md
- API 文件和程式碼範例
- 所有模組的配置指南
- 業務模組整合範例

---

## 變更類型

- `Added` 用於新功能
- `Changed` 用於現有功能的變更
- `Deprecated` 用於即將移除的功能
- `Removed` 用於已移除的功能
- `Fixed` 用於任何錯誤修復
- `Security` 用於漏洞修復

---

## 貢獻指南

請詳讀 [CONTRIBUTING.md](CONTRIBUTING.md) 以了解我們的行為準則，以及
提交 Pull Request 的流程。

---

## 版本歷史

- **1.0.0**: 完整 Common Layer 實作的初始版本
  - 多租戶架構支援
  - 統一異常處理和響應格式
  - Redis 快取和分散式鎖定
  - 全面的 Web 請求處理
  - 完整的測試覆蓋和文件
