# 這是一份針對 `001-common-layer` 實作文件的詳細審查報告

整體而言，這套 Speckit 文件（Research, Spec, Plan, Data Model, Quickstart）品質很高，結構清晰，且與 `01-系統架構設計.md` 的核心精神（低耦合、多租戶隔離）高度一致。

然而，為了確保作為「基礎設施」的 Common Layer 足夠穩健且符合架構長遠發展，我發現了幾個**關鍵的潛在問題、不一致處以及改進建議**。

以下是詳細的審查分析：

-----

## 1\. 架構一致性審查 (Architecture Compliance)

| 檢核項目 | 架構文件要求 | 現狀 (Speckit) | 評估 |
| :--- | :--- | :--- | :--- |
| **模組劃分** | Common Core, Database, Redis, Web | 結構完全對應 (`rbac-common-*`) | ✅ 符合 |
| **多租戶隔離** | 租戶隔離策略、TenantContext | 透過 `TenantLineInnerInterceptor` 與 `TenantContextHolder` 實作 | ✅ 符合 |
| **數據庫支援** | 多數據源支持 (Common Database) | `spec.md` FR2.6 將其列為「為未來擴展預留」 | ⚠️ **偏差** (見下方詳述) |
| **ID 生成策略** | 分布式部署/微服務擴展性 | `data-model.md` 使用 `IDENTITY` (自增主鍵) | ⚠️ **風險** (見下方詳述) |
| **依賴倒置** | 業務層依賴抽象介面 | Common Layer 尚未定義足夠的抽象介面供 Audit/Auth 使用 | ⚠️ **疏漏** (見下方詳述) |

-----

## 2\. 發現的問題與不一致 (Issues & Inconsistencies)

### A. ID 生成策略與分佈式架構的衝突

* **文件位置**: `data-model.md` (BaseEntity)
* **問題**: `BaseEntity` 定義了 `@GeneratedValue(strategy = GenerationType.IDENTITY)`。
* **分析**: 架構文件提到系統需支持「微服務部署（擴展方案）」。使用資料庫自增 ID (Identity) 在分庫分表或數據遷移時會極其痛苦，且容易洩露業務量。
* **建議**: 在 Common Layer 階段就應確立使用 **Snowflake (雪花演算法)** 或 **MyBatis-Plus 的 `ASSIGN_ID`**。這能保證全域唯一性，且不依賴資料庫生成，更符合分佈式系統設計。

### B. 審計 (Audit) 功能的循環依賴風險

* **文件位置**: `data-model.md` (AuditEntity)
* **問題**: `AuditEntity` 中的 `prePersistAudit` 方法中註解掉了獲取當前用戶的代碼：`// Long currentUserId = SecurityContextHolder.getUserId();`。
* **分析**: 這是典型的架構問題。Common Layer 不能依賴 Auth/Security Module（否則會循環依賴）。如果不解決這個問題，`AuditEntity` 的自動填充功能在 Phase 1 根本無法運作，或者開發者會被迫在 Common 層引入 Spring Security 依賴，破壞分層原則。
* **建議**: 在 `common-core` 中定義一個 `UserContext` 或 `AuditorAware` **介面 (Interface)**。
    1. Common 層只依賴此介面。
    2. Auth 層稍後實作此介面並注入 Bean。
    3. `AuditEntity` 透過 `ApplicationContext` 獲取此介面的實作來填充 ID。

### C. MyBatis-Plus 與 Spec 描述的微小脫節

* **文件位置**: `spec.md` (AC2) vs `research.md`
* **問題**: `spec.md` 的 Acceptance Criteria 寫道：「TenantInterceptor 能自動在 INSERT 語句中注入 tenant\_id」。
* **分析**: 雖然 MyBatis-Plus 的 `TenantLineInnerInterceptor` 確實能處理 Select/Update/Delete 的過濾，但對於 **Insert** 的自動注入，通常需要實作 `MetaObjectHandler` 或者在 `TenantLineHandler` 中特別配置。原生的 MP 租戶插件對 Insert 的 `tenant_id` 注入行為有時不如預期（通常用於過濾而非填充）。
* **建議**: 在 `quickstart.md` 或 `data-model.md` 中明確指出使用 MP 的 `MetaObjectHandler` 來處理 Insert 時的 `tenant_id` 填充，或者確認 MP 版本對 Insert 注入的具體行為，以免開發者誤解。

### D. 多數據源支持被延後

* **文件位置**: `spec.md` (FR2.6)
* **問題**: 架構設計明確要求 Common Database 支援多數據源，但 Spec 將其標記為「預留」。
* **分析**: 如果初期只有一個 DB，這可以接受。但如果系統設計包含讀寫分離（架構圖 7.1 提到），則 Common Database 現在就應該包含 `DynamicDataSource` 的基礎配置（例如依賴 `dynamic-datasource-spring-boot-starter`），否則後期重構成本很高。

-----

## 3\. 文件具體修訂建議

### 修改 `data-model.md`

1. **BaseEntity**:

    ```java
    // 建議修改為 Snowflake ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // 或使用 MP 的 @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    ```

2. **AuditEntity**: 移除註解掉的偽代碼，改為說明需依賴的介面機制。

### 修改 `spec.md`

1. **FR1 (Common Core)**: 新增 FR1.6 `UserContext` 介面定義（用於解耦 Audit 與 Auth）。
2. **AC2 (Common Database)**: 修正關於 Insert 注入的描述，建議明確提及 `MetaObjectHandler` 或 MP 的具體機制。
3. **Trace Context**: 雖然架構提到 Trace Module 是 Infrastructure，但 Common Web 應該現在就包含 **Trace ID 的傳遞機制** (MDC)，否則日誌將無法串聯。建議在 FR4 中加入 `MDC TraceId Filter`。

### 修改 `quickstart.md`

1. **TimescaleDB**: 你在 `quickstart.md` 中指定了 `timescaledb-ha:pg14-latest`。這是一個具體的技術選型，但在 `research.md` 中未見討論。如果只是為了 PostgreSQL，建議改回標準 Postgres 映像檔，除非系統有明確的時序數據需求（Audit Log?），這需要與架構確認。

-----

## 4\. 總結

**結論**: `001-common-layer` 的設計**符合需求，但在「解耦」與「擴展性」的細節實作上需要微調**。

**行動清單**:

1. **必修**: 將 ID 生成策略改為 Snowflake (ASSIGN\_ID)。
2. **必修**: 在 Common Core 定義 `Security/User Context` 介面以解決 Audit 依賴問題。
3. **建議**: 在 Common Web 加入 MDC Trace ID 支援。
4. **建議**: 確認是否真的需要 TimescaleDB，或僅需標準 Postgres。

如果能修正上述幾點，這將是一個非常堅固的基礎層。
