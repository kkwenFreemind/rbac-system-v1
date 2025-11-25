# Implementation Plan: 認證授權模組 (Authentication Module)

**Branch**: `001-authentication` | **Date**: 2025-11-25 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-authentication/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

實作基於 JWT + Redis 的極簡認證授權模組，提供登入/登出、Token 生成/驗證、UserContext 介面和 @PreAuthorize 權限校驗框架。採用依賴倒置設計，初版使用 MockUserRepository（記憶體 Map）實作，確保模組獨立性，待 User/Tenant/Role Module 完成後無縫切換至資料庫實作。

**技術方案**：

- JWT Token (HMAC-SHA256, 24 小時有效期) 包含 user_id、tenant_id、username、roles
- Redis 黑名單機制實現 Token 撤銷（登出）
- BCrypt 密碼驗證（Rounds: 10）
- 帳號鎖定策略（5 次錯誤 → 鎖定 15 分鐘）
- Spring Security 整合 @PreAuthorize 註解
- ThreadLocal UserContext 提供跨模組使用者上下文

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.5.0

**Primary Dependencies**:

- Spring Security 6.3+ (認證授權框架)
- JJWT 0.12+ (JWT Token 生成/驗證)
- Spring Data Redis (Token 黑名單、帳號鎖定)
- BCrypt (密碼加密驗證)
- Spring AOP (權限校驗攔截)
- rbac-common-redis (Redis 操作工具 - 來自 Common Layer)
- rbac-common-core (Result、BaseException - 來自 Common Layer)
- rbac-common-web (統一異常處理、CORS 配置 - 來自 Common Layer)

**Storage**:

- Redis 7+ (Token 黑名單: `auth:blacklist:{token}`, 帳號鎖定: `auth:lock:{username}`)
- 初版使用 MockUserRepository (記憶體 Map / application.yml 配置)
- 後期整合 PostgreSQL 15+ (透過 JpaUserRepository 實作)

**Testing**:

- JUnit 5 + Mockito (單元測試, 目標覆蓋率 >80%)
- Spring Boot Test + Testcontainers Redis (整合測試)
- RestAssured (API 端點測試)
- 測試重點：JWT 生成/驗證、Token 黑名單、帳號鎖定、UserContext、權限校驗

**Target Platform**: Linux server / Docker container (Spring Boot embedded Tomcat)

**Project Type**: Backend Web API (RESTful)

**Performance Goals**:

- 登入響應時間 <2 秒 (SC-001)
- Token 驗證 <100 毫秒 (含 Redis 查詢) (SC-002)
- 支援 1000 並發登入請求 (P99 <3 秒) (SC-003)
- 高負載 Token 驗證 (5000 QPS, P95 <150ms) (SC-010)

**Constraints**:

- API 響應時間 P95 <200ms (憲章要求)
- Token 有效期固定 24 小時 (不支援 Refresh Token)
- 初版不支援跨租戶存取 (tenant_id 固定為 1)
- 初版不支援密鑰輪換 (單一 JWT Secret)
- 模組必須獨立可測試 (使用 Mock 資料，不依賴 User/Tenant/Role Module)

**Scale/Scope**:

- 支援 10,000+ 並發使用者 (憲章要求)
- 3 個 REST API 端點 (login, logout, me)
- 5 個核心類別 (AuthService, JwtTokenService, UserContext, MockUserRepository, AuthController)
- 4 個 P1 使用者故事 + 1 個 P2 使用者故事
- 預估開發時間 3-5 天

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. 模組化低耦合架構

- **Status**: PASS
- **Verification**:
  - Auth Module 只依賴 `UserRepository` 介面 (抽象)，不依賴具體實作
  - 使用 Spring Dependency Injection (@Autowired 建構子注入)
  - 跨模組通信透過 DTO 和介面，不暴露領域實體
  - 無循環依賴 (Auth 是獨立模組，提供 UserContext 給其他模組使用)
  - 模組可獨立測試 (Mock 實作)

### ✅ II. 預設多租戶隔離

- **Status**: PASS (with annotation)
- **Verification**:
  - JWT Token 包含 `tenant_id` (初版固定為 1)
  - UserContext 提供 `getTenantId()` 方法供其他模組使用
  - **註記**: 初版使用 Mock 資料，tenant_id 固定為 1。真正的租戶隔離將在 Tenant Module 完成後透過 TenantFilter 實現。Auth Module 提供基礎框架，確保所有模組都能從 UserContext 獲取 tenant_id

### ✅ III. 安全性優先開發

- **Status**: PASS
- **Verification**:
  - JWT Token (HMAC-SHA256 簽章，24 小時有效期)
  - BCrypt 密碼驗證 (Rounds 10)
  - Redis 黑名單機制 (Token 撤銷)
  - 帳號鎖定策略 (5 次錯誤 → 鎖定 15 分鐘)
  - Spring Security 整合 @PreAuthorize 註解
  - 所有 API 端點需要認證 (除 /login 外)
  - 錯誤訊息通用化，防止帳號枚舉

### ✅ IV. 所有關鍵操作的稽核軌跡

- **Status**: PASS (準備就緒)
- **Verification**:
  - 登入成功記錄日誌 (user_id, tenant_id, IP, 時間戳)
  - 登出記錄 Token 撤銷事件
  - 權限驗證失敗記錄日誌 (user_id, tenant_id, 請求的權限碼)
  - **註記**: Auth Module 提供日誌記錄點，實際稽核持久化由 Audit Module 負責 (後續實作)

### ✅ V. 關鍵路徑的測試驅動開發

- **Status**: PASS (承諾)
- **Verification**:
  - 承諾對認證流程 (login/logout) 撰寫測試
  - 承諾對 JWT 生成/驗證撰寫測試
  - 承諾對 Token 黑名單機制撰寫測試
  - 承諾對 UserContext 撰寫測試
  - 承諾對權限校驗 (@PreAuthorize) 撰寫測試
  - 目標單元測試覆蓋率 >80%
  - 整合測試使用 Testcontainers Redis

### ✅ VI. API 優先設計與版本控制

- **Status**: PASS
- **Verification**:
  - API 遵循 RESTful 慣例
  - URL 版本控制 `/api/v1/auth/`
  - OpenAPI/Swagger 文件生成 (SpringDoc)
  - 統一回應格式 {code, message, data, timestamp, traceId}
  - 標準 HTTP 狀態碼 (200, 401, 403)

### 🟡 Gate Summary

| Gate | Status | Justification |
|------|--------|---------------|
| 模組化低耦合架構 | ✅ PASS | 依賴倒置設計，介面隔離，Mock 實作確保獨立性 |
| 預設多租戶隔離 | ✅ PASS | UserContext 提供 tenant_id，為其他模組提供基礎 |
| 安全性優先開發 | ✅ PASS | 多層安全控制，JWT + BCrypt + Redis 黑名單 |
| 稽核軌跡 | ✅ PASS | 提供日誌記錄點，待 Audit Module 持久化 |
| 測試驅動開發 | ✅ PASS | 承諾 TDD 流程，關鍵路徑優先測試 |
| API 優先設計 | ✅ PASS | RESTful + OpenAPI + 統一回應格式 |

**Result**: ✅ **ALL GATES PASS** - 可進入 Phase 0 Research

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
backend/rbac-auth/
├── pom.xml                              # Maven 配置，依賴 rbac-common-*
├── src/main/java/com/rbac/auth/
│   ├── controller/                      # Controller 層
│   │   └── AuthController.java          # 認證 API (/login, /logout, /me)
│   ├── service/                         # Service 層
│   │   ├── AuthService.java             # 核心認證邏輯 (依賴 UserRepository)
│   │   └── JwtTokenService.java         # JWT Token 生成/驗證
│   ├── repository/                      # Repository 層 (介面導向)
│   │   ├── UserRepository.java          # 使用者倉儲介面 (抽象)
│   │   ├── MockUserRepository.java      # Mock 實作 (初版)
│   │   └── JpaUserRepository.java       # JPA 實作 (後續，與 User Module 整合)
│   ├── model/                           # Model 層
│   │   ├── entity/                      # 領域實體
│   │   │   └── User.java                # User 實體 (Mock 資料使用)
│   │   ├── dto/                         # 資料傳輸物件
│   │   │   ├── LoginRequest.java        # 登入請求
│   │   │   ├── LoginResponse.java       # 登入回應 (包含 JWT)
│   │   │   └── UserInfoResponse.java    # 使用者資訊回應
│   │   └── vo/                          # 值物件 (如果需要)
│   ├── config/                          # Configuration 配置
│   │   ├── SecurityConfig.java          # Spring Security 設定
│   │   ├── JwtConfig.java               # JWT 設定 (secret, expiry)
│   │   └── MockDataConfig.java          # Mock 資料配置 (讀取 application.yml)
│   ├── filter/                          # Filter 過濾器
│   │   └── JwtAuthenticationFilter.java # JWT 驗證過濾器
│   ├── context/                         # Context 上下文
│   │   ├── UserContext.java             # UserContext 介面
│   │   └── UserContextHolder.java       # ThreadLocal 管理
│   ├── aspect/                          # AOP 切面
│   │   └── PermissionCheckAspect.java   # @PreAuthorize 實作 (P2)
│   ├── exception/                       # 異常定義
│   │   ├── AuthenticationException.java # 認證失敗異常
│   │   ├── TokenExpiredException.java   # Token 過期異常
│   │   └── AccountLockedException.java  # 帳號鎖定異常
│   └── util/                            # 工具類
│       ├── JwtUtil.java                 # JWT 工具 (JJWT 封裝)
│       └── BCryptUtil.java              # BCrypt 工具 (加密/驗證)
├── src/main/resources/
│   ├── application.yml                  # 主配置
│   ├── application-dev.yml              # 開發環境配置 (Mock 資料)
│   └── application-prod.yml             # 生產環境配置 (JPA 實作)
└── src/test/java/com/rbac/auth/
    ├── controller/                      # Controller 層測試
    │   └── AuthControllerTest.java      # API 整合測試 (@SpringBootTest)
    ├── service/                         # Service 層測試
    │   ├── AuthServiceTest.java         # 單元測試 (Mock UserRepository)
    │   └── JwtTokenServiceTest.java     # JWT 邏輯測試
    ├── repository/                      # Repository 層測試
    │   └── MockUserRepositoryTest.java  # Mock 實作測試
    ├── filter/                          # Filter 測試
    │   └── JwtAuthenticationFilterTest.java
    └── integration/                     # 整合測試
        └── AuthIntegrationTest.java     # Redis + JWT 完整流程測試 (Testcontainers)
```

**Structure Decision**: Backend Web API 架構，採用標準 Spring Boot 多模組結構。Auth Module 位於 `backend/rbac-auth/`，獨立於 Common Layer (`backend/rbac-common/`) 和其他業務模組 (`backend/rbac-tenant/`, `backend/rbac-user/` 等)。

**Key Design Principles**:

- **分層架構**: Controller → Service → Repository → Entity (嚴格單向依賴，符合憲章要求)
- **介面隔離**: `UserRepository` 介面確保 AuthService 不依賴具體實作
- **依賴注入**: 全部使用 Spring @Autowired 建構子注入
- **Profile 切換**: `@Profile("dev")` MockUserRepository, `@Profile("prod")` JpaUserRepository
- **無狀態設計**: JWT Token 攜帶所有認證資訊，無 Session 狀態
- **獨立測試**: 每層都可獨立測試，Service 層使用 Mock Repository

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**Status**: ✅ **NO VIOLATIONS** - All constitution gates passed without exception
