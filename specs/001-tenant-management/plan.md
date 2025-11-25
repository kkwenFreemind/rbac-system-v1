# Implementation Plan: 租戶管理模組

**Branch**: `001-tenant-management` | **Date**: 2025-11-24 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-tenant-management/spec.md`

## Summary

實作多租戶 RBAC 系統的租戶管理模組，提供租戶 CRUD 操作、自動租戶上下文注入（TenantFilter）、以及透過 MyBatis 攔截器實現的行級資料隔離。此模組是整個多租戶系統的基礎，確保每個客戶組織的資料完全隔離，並支援平台管理員進行租戶生命週期管理。

**核心技術策略**：

- 使用 MyBatis-Plus TenantLineInnerInterceptor 自動注入 tenant_id 過濾條件
- 透過 ThreadLocal (TenantContextHolder) 儲存租戶上下文
- 使用 Spring Filter 在 HTTP 請求層面自動設定和清理租戶上下文
- 基於 Common Layer 現有基礎（TenantEntity、AuditMetaObjectHandler）擴展租戶管理功能

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 3.5.0, MyBatis-Plus 3.5.7, PostgreSQL 42.7.4, Spring Data Redis  
**Storage**: PostgreSQL 15+ (主要資料儲存), Redis 7+ (Token 黑名單、快取)  
**Testing**: JUnit 5.11.0, Mockito 5.12.0, Testcontainers 1.20.0  
**Target Platform**: Linux Server (容器化部署)  
**Project Type**: Web Application (Backend API) - 多模組 Maven 專案  
**Performance Goals**:

- API 響應時間 P95 < 200ms
- 支援 1000+ 並行請求來自 50+ 不同租戶
- 租戶清單查詢（1000 筆記錄）< 500ms
**Constraints**:
- 零跨租戶資料洩漏（100% 隔離）
- ThreadLocal 記憶體洩漏為零（24 小時壓力測試）
- 管理操作在 2 秒內完成
**Scale/Scope**:
- 預期租戶數：100-1000
- 每租戶使用者數：10-10000
- 總使用者數：10萬+

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### 核心原則檢查

✅ **I. 模組化低耦合架構**

- 租戶管理模組將作為獨立業務模組開發
- 僅透過 Common Layer 定義的介面與其他模組通信
- 使用 DTO 進行跨模組資料傳輸，不直接暴露 Tenant 實體
- 依賴注入使用 Spring @Autowired（建構子注入）
- **無違反**：模組邊界清晰，符合低耦合原則

✅ **II. 預設多租戶隔離**

- Tenant 實體繼承自 TenantEntity（已包含 tenant_id）
- 利用 Common Database 模組的 MyBatis TenantLineInnerInterceptor
- 租戶上下文透過 TenantContextHolder（已存在於 Common Layer）管理
- 所有查詢自動包含 tenant_id 過濾
- **無違反**：完全符合多租戶隔離原則

✅ **III. 安全性優先開發**

- 所有租戶管理 API 需要管理員權限（透過 Spring Security）
- 使用 JWT 認證（依賴 Authentication 模組）
- 敏感操作記錄稽核日誌
- **無違反**：符合安全性要求

✅ **IV. 所有關鍵操作的稽核軌跡**

- 租戶 CRUD 操作自動觸發稽核記錄
- 利用 Common Database 的 AuditMetaObjectHandler
- 記錄操作者、時間戳、變更內容
- **無違反**：完全符合稽核要求

✅ **V. 關鍵路徑的測試驅動開發**

- 租戶隔離邏輯屬於關鍵路徑，將採用 TDD
- 單元測試覆蓋率目標：>70%
- 整合測試驗證租戶隔離和 CRUD 操作
- **無違反**：符合測試要求

✅ **VI. API 優先設計與版本控制**

- RESTful API 設計（/api/v1/tenants）
- 使用 SpringDoc OpenAPI 生成文件
- 統一回應格式（使用 Common Core 的 Result）
- **無違反**：符合 API 設計原則

### 安全性與合規要求檢查

✅ **資料保護**

- 敏感欄位（contact_email）將使用 AES-256 加密
- 資料庫憑證儲存在環境變數中
- TLS 傳輸加密

✅ **存取控制**

- 租戶管理操作僅限平台管理員
- 實施最小權限原則

✅ **合規性**

- 支援租戶軟刪除（符合 GDPR）
- 完整稽核追蹤
- 資料匯出功能（未來擴展）

### 品質標準檢查

✅ **程式碼品質**

- 遵循 SOLID 原則
- 所有類別包含 Javadoc（@author CHANG SHOU-WEN）
- 方法長度 < 50 行
- 無程式碼重複

✅ **效能**

- 租戶清單查詢使用索引（tenant_id, name）
- 實施分頁（預設 20，最大 100）
- 快取租戶資訊（TTL 30 分鐘）

✅ **文件**

- OpenAPI/Swagger 自動生成
- 複雜邏輯包含內嵌註解
- 資料庫遷移腳本追蹤

### 結論

**✅ 所有憲章檢查通過** - 租戶管理模組設計符合專案憲章的所有核心原則、安全要求和品質標準。無需複雜度豁免。

## Project Structure

### Documentation (this feature)

```text
specs/001-tenant-management/
├── plan.md              # This file (implementation plan)
├── research.md          # Phase 0 output (technology research)
├── data-model.md        # Phase 1 output (data model design)
├── quickstart.md        # Phase 1 output (development guide)
├── contracts/           # Phase 1 output (API contracts)
│   └── tenant-api.yaml  # OpenAPI specification for tenant endpoints
└── tasks.md             # Phase 2 output (implementation tasks)
```

### Source Code (repository root)

```text
backend/
├── rbac-common/                           # 現有 Common Layer
│   ├── rbac-common-core/                  # 提供 Result, 異常處理
│   ├── rbac-common-database/              # 提供 TenantEntity, TenantContextHolder
│   ├── rbac-common-redis/                 # 提供快取服務
│   └── rbac-common-web/                   # 提供 TenantFilter, GlobalExceptionHandler
│
├── rbac-tenant/                           # 新增：租戶管理模組 ⭐
│   ├── pom.xml                            # Maven 配置
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rbac/tenant/
│   │   │   │   ├── entity/
│   │   │   │   │   └── Tenant.java        # 租戶實體（extends TenantEntity）
│   │   │   │   ├── dto/
│   │   │   │   │   ├── TenantCreateRequest.java
│   │   │   │   │   ├── TenantUpdateRequest.java
│   │   │   │   │   ├── TenantQueryRequest.java
│   │   │   │   │   └── TenantResponse.java
│   │   │   │   ├── mapper/
│   │   │   │   │   └── TenantMapper.java  # MyBatis Mapper 介面
│   │   │   │   ├── service/
│   │   │   │   │   ├── ITenantService.java       # Service 介面
│   │   │   │   │   └── impl/
│   │   │   │   │       └── TenantServiceImpl.java # Service 實作
│   │   │   │   ├── controller/
│   │   │   │   │   └── TenantController.java     # REST API Controller
│   │   │   │   └── config/
│   │   │   │       └── TenantModuleConfig.java   # 模組配置
│   │   │   └── resources/
│   │   │       ├── mapper/
│   │   │       │   └── TenantMapper.xml   # MyBatis XML 映射
│   │   │       └── application-tenant.yml # 模組配置
│   │   └── test/
│   │       ├── java/com/rbac/tenant/
│   │       │   ├── service/
│   │       │   │   └── TenantServiceTest.java    # 單元測試
│   │       │   ├── controller/
│   │       │   │   └── TenantControllerTest.java # API 測試
│   │       │   └── integration/
│   │       │       └── TenantIsolationIntegrationTest.java  # 整合測試
│   │       └── resources/
│   │           └── application-test.yml   # 測試配置
│
└── pom.xml                                # 父 POM（需更新以包含 rbac-tenant）
```

**Structure Decision**:

採用多模組 Maven Web Application 結構。租戶管理模組 (`rbac-tenant`) 作為獨立業務模組，與現有 Common Layer 並列。

**設計理由**：

1. **模組隔離**：租戶管理作為獨立模組，符合低耦合原則
2. **依賴清晰**：僅依賴 Common Layer，不依賴其他業務模組
3. **可測試性**：獨立模組便於單元測試和整合測試
4. **可擴展性**：未來可以輕鬆添加其他業務模組（User, Permission 等）
5. **Maven 標準**：遵循 Spring Boot + Maven 多模組專案最佳實踐

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**無複雜度違規** - 租戶管理模組設計符合所有憲章原則，無需豁免。

---

## Phase 0: Outline & Research

### Research Tasks

租戶管理模組的技術基礎已在 Common Layer 實作中確立，無需額外研究。以下為確認事項：

#### RT-001: 確認 MyBatis-Plus TenantLineInnerInterceptor 配置

**狀態**: ✅ 已完成（Common Database 模組）

**發現**：

- `MyBatisPlusConfig.java` 已配置 `TenantLineInnerInterceptor`
- 自動在 SELECT 查詢中注入 `tenant_id` 過濾條件
- 支援動態忽略表（例如系統層級表）

#### RT-002: 確認 TenantContextHolder 實作

**狀態**: ✅ 已完成（Common Database 模組）

**發現**：

- 使用 `ThreadLocal` 儲存租戶上下文
- 提供 `setTenantId()`, `getTenantId()`, `clear()` 方法
- 執行緒安全，支援並行請求

#### RT-003: 確認 TenantFilter 實作

**狀態**: ✅ 已完成（Common Web 模組）

**發現**：

- 從 HTTP Header 提取 `X-Tenant-Id`
- 自動設定到 `TenantContextHolder`
- 請求完成後自動清理（防止記憶體洩漏）
- 備援清理機制（`TenantCleanupInterceptor`）

#### RT-004: 確認 AuditMetaObjectHandler 實作

**狀態**: ✅ 已完成（Common Database 模組）

**發現**：

- 自動填充 `created_by`, `created_at`, `updated_by`, `updated_at`
- 自動填充 `tenant_id`（在 INSERT 時）
- 與 MyBatis-Plus 整合

#### RT-005: 確認軟刪除機制

**狀態**: ✅ 已完成（Common Database 模組）

**發現**：

- `BaseEntity` 支援 `@TableLogic` 註解
- 軟刪除記錄不會出現在正常查詢中
- 保留資料以供稽核

### Research Findings Summary

**結論**: 租戶管理模組所需的所有基礎設施都已在 Common Layer 中實作。無需額外技術研究或原型開發。

**技術決策**：

1. **實體設計**: `Tenant` 實體繼承 `TenantEntity`（包含 tenant_id, 稽核欄位）
2. **隔離策略**: 利用 MyBatis 攔截器自動過濾
3. **上下文管理**: 複用 `TenantContextHolder`
4. **API 設計**: RESTful + SpringDoc OpenAPI
5. **測試策略**: 單元測試 + Testcontainers 整合測試

---

## Phase 1: Design & Contracts

### Data Model Design

將在 `data-model.md` 中詳細定義：

- **Tenant 實體**: 欄位、關係、驗證規則
- **DTO 設計**: Request/Response 物件
- **資料庫 Schema**: 表結構、索引、約束
- **狀態機**: 租戶狀態轉換規則

### API Contracts

將在 `contracts/tenant-api.yaml` 中定義 OpenAPI 規格：

- **POST /api/v1/tenants**: 建立租戶
- **GET /api/v1/tenants/{id}**: 取得租戶詳情
- **GET /api/v1/tenants**: 查詢租戶清單（分頁、過濾）
- **PUT /api/v1/tenants/{id}**: 更新租戶
- **DELETE /api/v1/tenants/{id}**: 軟刪除租戶
- **PATCH /api/v1/tenants/{id}/status**: 變更租戶狀態

### Quickstart Guide

將在 `quickstart.md` 中提供：

- 開發環境設定
- 模組依賴配置
- 本地測試指南
- API 測試範例

---

## Constitution Check (Post-Design Re-Evaluation)

設計完成後,重新檢視憲章符合性:

| 原則 | 符合性 | 說明 |
|-----|-------|------|
| **模組化低耦合** | ✅ PASS | 租戶模組獨立於 rbac-tenant,僅依賴 Common Layer 的穩定介面 |
| **預設多租戶隔離** | ✅ PASS | data-model.md 確認所有查詢透過 MyBatis 攔截器自動過濾 tenant_id |
| **安全優先** | ✅ PASS | API 契約定義 JWT 認證 + 權限檢查,資料模型包含輸入驗證規則 |
| **關鍵操作稽核** | ✅ PASS | 繼承 TenantEntity 自動記錄 created_by/updated_by,刪除操作為軟刪除 |
| **關鍵路徑 TDD** | ✅ PASS | quickstart.md 定義單元測試 + 整合測試策略,租戶隔離為關鍵路徑 |
| **API 優先設計** | ✅ PASS | tenant-api.yaml 完整定義 OpenAPI 規格,先於實作 |

**結論**: 設計階段無新增違規,符合所有憲章原則。✅

---

## Phase 2: Implementation Tasks

任務分解將在 `tasks.md` 中詳細定義，遵循以下結構：

### Phase 2.1: Setup (3-5 tasks)

- 建立 `rbac-tenant` Maven 模組
- 配置 pom.xml 依賴
- 建立目錄結構
- 配置 application-tenant.yml

### Phase 2.2: Entity & Mapper (5-8 tasks)

- 建立 `Tenant` 實體類別
- 建立 DTO 類別（Request/Response）
- 建立 `TenantMapper` 介面
- 撰寫 MyBatis XML 映射
- 建立資料庫遷移腳本

### Phase 2.3: Service Layer (8-12 tasks)

- 建立 `ITenantService` 介面
- 實作 `TenantServiceImpl`
- 實作 CRUD 邏輯
- 實作查詢過濾邏輯
- 實作狀態轉換邏輯
- 撰寫單元測試

### Phase 2.4: Controller Layer (5-8 tasks)

- 建立 `TenantController`
- 實作 REST API 端點
- 添加 SpringDoc 註解
- 實作驗證邏輯
- 撰寫 API 測試

### Phase 2.5: Integration & Testing (8-12 tasks)

- 撰寫整合測試
- 測試租戶隔離
- 測試並行請求
- 效能測試
- 安全性測試

### Phase 2.6: Documentation & Polish (3-5 tasks)

- 完成 Javadoc
- 生成 OpenAPI 文件
- 更新 README
- 程式碼審查
- 最終驗證

**預估總任務數**: 32-50 個任務
**預估開發時間**: 2-3 週（單人）

---

## Implementation Plan Summary

### Phase Completion Status

- ✅ **Phase 0: Research** - 完成
  - 確認 Common Layer 基礎設施可用（MyBatis 攔截器、TenantContextHolder、TenantFilter）
  - 無需額外技術研究
  
- ✅ **Phase 1: Design** - 完成
  - ✅ `data-model.md`: 實體設計、DTO、資料庫 Schema、狀態機
  - ✅ `contracts/tenant-api.yaml`: OpenAPI 規格（6 個 REST 端點）
  - ✅ `contracts/internal-contracts.md`: 服務介面、事件、快取、權限契約
  - ✅ `quickstart.md`: 開發環境設定、API 測試、除錯技巧
  - ✅ `.github/agents/copilot-instructions.md`: 更新 Copilot 上下文
  
- ⏳ **Phase 2: Tasks** - 待執行
  - 使用 `/speckit.tasks` 生成 `tasks.md`
  - 分解為 32-50 個可追蹤任務
  - 按 User Story 優先級組織（P1 → P2 → P3）

### Key Design Decisions

1. **實體設計**: `Tenant` 繼承 `TenantEntity`,複用稽核欄位和軟刪除
2. **隔離策略**: 完全依賴 MyBatis 攔截器,無需手動過濾 tenant_id
3. **狀態管理**: 狀態機控制轉換規則,刪除前驗證前提條件
4. **API 設計**: RESTful + OpenAPI 規格,支援分頁、過濾、排序
5. **測試策略**: 單元測試（Mockito）+ 整合測試（Testcontainers）
6. **快取策略**: Redis 快取租戶詳情（TTL 1hr）,寫入時主動失效

### Architecture Highlights

```
┌──────────────────────────────────────────────────────────┐
│                   租戶管理模組架構                         │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Controller (REST API)                                   │
│      │                                                   │
│      ├─ TenantController                                 │
│      │   ├─ POST   /tenants         (建立)              │
│      │   ├─ GET    /tenants         (清單查詢)          │
│      │   ├─ GET    /tenants/{id}    (詳情查詢)          │
│      │   ├─ PUT    /tenants/{id}    (更新)              │
│      │   ├─ DELETE /tenants/{id}    (軟刪除)            │
│      │   └─ PATCH  /tenants/{id}/status (狀態變更)      │
│      │                                                   │
│      ▼                                                   │
│  Service (業務邏輯)                                       │
│      │                                                   │
│      ├─ ITenantService (介面)                            │
│      └─ TenantServiceImpl (實作)                         │
│           ├─ CRUD 邏輯                                   │
│           ├─ 唯一性檢查                                  │
│           ├─ 狀態轉換驗證                                │
│           └─ 快取管理                                    │
│      │                                                   │
│      ▼                                                   │
│  Mapper (資料存取)                                        │
│      │                                                   │
│      ├─ TenantMapper (MyBatis-Plus)                      │
│      │   └─ 自動租戶過濾 (TenantLineInnerInterceptor)    │
│      │                                                   │
│      ▼                                                   │
│  Entity (資料模型)                                        │
│      │                                                   │
│      └─ Tenant extends TenantEntity                      │
│          ├─ id (Snowflake ID)                            │
│          ├─ name (唯一)                                  │
│          ├─ contact_email (唯一)                         │
│          ├─ plan_type (枚舉)                             │
│          ├─ status (枚舉)                                │
│          └─ 稽核欄位 (繼承)                              │
│                                                          │
├──────────────────────────────────────────────────────────┤
│  橫切關注點 (Common Layer)                                │
│                                                          │
│  ├─ TenantFilter (Web)                                   │
│  │   └─ 從 HTTP Header 提取 X-Tenant-Id → ThreadLocal    │
│  │                                                       │
│  ├─ TenantLineInnerInterceptor (Database)                │
│  │   └─ 自動注入 tenant_id 過濾條件                      │
│  │                                                       │
│  ├─ AuditMetaObjectHandler (Database)                    │
│  │   └─ 自動填充稽核欄位 (created_by, updated_by)        │
│  │                                                       │
│  └─ GlobalExceptionHandler (Web)                         │
│      └─ 統一異常處理和錯誤回應                           │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### Technology Stack Confirmation

| 層級 | 技術 | 用途 |
|-----|------|------|
| **語言** | Java 17 | 核心語言 |
| **框架** | Spring Boot 3.5.0 | Web 框架 |
| **ORM** | MyBatis-Plus 3.5.7 | 資料存取 + 租戶攔截 |
| **資料庫** | PostgreSQL 15+ | 主資料儲存 |
| **快取** | Redis 7+ | 租戶詳情快取 + 分散式鎖 |
| **測試** | JUnit 5 + Mockito + Testcontainers | 單元 + 整合測試 |
| **建構** | Maven 3.9+ | 多模組建構 |
| **API 文件** | SpringDoc OpenAPI 3 | 自動生成 API 文件 |

### Performance Targets

| 指標 | 目標 | 驗證方式 |
|-----|------|---------|
| **租戶建立** | P95 < 200ms | JMeter 壓測 |
| **租戶查詢（單筆）** | P95 < 50ms | JMeter 壓測 |
| **租戶清單查詢** | P95 < 100ms | JMeter 壓測 |
| **並行能力** | 1000+ 並行請求 | JMeter 並行測試 |
| **資料隔離** | 零跨租戶洩漏 | 整合測試驗證 |
| **記憶體洩漏** | 零 ThreadLocal 洩漏 | Profiler 驗證 |

### Security Measures

1. ✅ **JWT 認證**: 所有 API 需要有效 Token
2. ✅ **權限控制**: 4 種權限碼（create/read/update/delete）
3. ✅ **租戶隔離**: MyBatis 攔截器自動過濾
4. ✅ **輸入驗證**: Jakarta Validation (@NotNull, @Size, @Email)
5. ✅ **SQL 注入防護**: MyBatis 參數綁定
6. ✅ **稽核日誌**: 自動記錄操作人和時間

---

## Next Steps

1. ✅ **Phase 0 完成**: 研究確認基礎設施可用
2. ✅ **Phase 1 完成**: 設計文件生成完畢
3. ⏳ **Phase 2 待辦**: 執行 `/speckit.tasks` 生成 `tasks.md`
4. ⏳ **實作**: 按 TDD 流程實作（測試先行）
5. ⏳ **整合**: 整合測試 + 效能測試
6. ⏳ **部署**: Docker 容器化 + CI/CD

---

## Generated Artifacts

### 📄 Documentation
- ✅ `plan.md` (本文件)
- ✅ `data-model.md` - 完整資料模型設計
- ✅ `quickstart.md` - 開發環境設定指南
- ⏳ `tasks.md` (待生成)

### 📋 Contracts
- ✅ `contracts/tenant-api.yaml` - OpenAPI 3.0 規格
- ✅ `contracts/internal-contracts.md` - 內部服務契約

### 🔧 Configuration
- ✅ `.github/agents/copilot-instructions.md` - Copilot 上下文

### 📊 Metrics
- **文件總數**: 6 個
- **API 端點數**: 6 個
- **實體數**: 1 個 (Tenant)
- **DTO 數**: 5 個 (Request/Response)
- **Service 方法數**: 10 個

---

## Conclusion

租戶管理模組的實作計劃已完成 Phase 0 和 Phase 1,所有設計文件已生成並通過憲章檢查。
下一步將使用 `/speckit.tasks` 命令生成詳細的任務分解,然後按 TDD 流程開始實作。

**估計總工時**: 80-120 小時（單人,包含測試和文件）
**建議迭代**: 2-3 週,每週一次 Code Review

---

**Branch**: `001-tenant-management`
**Plan Path**: `D:\SideProject\rbac-system-v1\specs\001-tenant-management\plan.md`
**Status**: ✅ Phase 1 Complete - Ready for Task Generation
