# 審查回應報告：001-common-layer 架構改進

**日期**: 2025-11-24  
**狀態**: ✅ 已完成  
**審查者**: 架構審查團隊  
**實作者**: GitHub Copilot

---

## 執行摘要

基於詳細的架構審查報告，我們識別並修正了 5 個關鍵的架構問題，確保 Common Layer 作為系統基礎設施層具備足夠的穩健性、擴展性和解耦能力。所有修正均已完成並更新到相關文檔。

---

## 問題與解決方案總結

| # | 問題類別 | 優先級 | 狀態 | 修正文檔 |
|---|---------|--------|------|---------|
| 1 | ID 生成策略與分散式架構衝突 | 🔴 必修 | ✅ 完成 | data-model.md, plan.md |
| 2 | 審計功能的循環依賴風險 | 🔴 必修 | ✅ 完成 | data-model.md, spec.md, plan.md |
| 3 | MyBatis-Plus Insert 注入機制不清晰 | 🟡 建議 | ✅ 完成 | spec.md, data-model.md |
| 4 | 缺少 MDC Trace ID 支援 | 🟡 建議 | ✅ 完成 | spec.md, data-model.md, plan.md |
| 5 | TimescaleDB 需求不明確 | 🟡 建議 | ✅ 完成 | plan.md, quickstart.md |

---

## 詳細修正內容

### 1. ID 生成策略改進 (🔴 必修)

#### 原問題

```java
// ❌ 原設計 - 使用資料庫自增 ID
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**風險**:

- 分庫分表時 ID 衝突
- 數據遷移困難
- 業務量洩露（ID 連續增長）
- 不支援微服務演進

#### 解決方案

```java
// ✅ 改進後 - 使用 Snowflake 演算法
@TableId(type = IdType.ASSIGN_ID)
private Long id;
```

**優勢**:

- 全域唯一性（64-bit 分散式 ID）
- 支援分庫分表和數據遷移
- 趨勢遞增但不連續
- 無需依賴資料庫生成

**修改位置**:

- `specs/master/data-model.md` - BaseEntity 定義
- `specs/master/plan.md` - 技術背景和複雜度追蹤

---

### 2. 審計依賴解耦 (🔴 必修)

#### 原問題

```java
// ❌ 原設計 - 註解掉的偽代碼，無法運作
@PrePersist
public void prePersistAudit() {
    // Long currentUserId = SecurityContextHolder.getUserId();
    // 問題：Common Layer 不能依賴 Auth/Security 模組（循環依賴）
}
```

#### 解決方案

**步驟 1**: 在 Common Core 定義介面

```java
// ✅ Common Core - 高層策略（抽象）
package com.rbac.common.core.context;

public interface UserContext {
    Long getCurrentUserId();
    String getCurrentUsername();
    default boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}
```

**步驟 2**: Auth 模組實作介面

```java
// ✅ Auth Module - 低層實作（具體）
@Component
public class SecurityUserContext implements UserContext {
    @Override
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 從 JWT 或 SecurityContext 提取使用者 ID
        return extractUserId(auth);
    }
}
```

**步驟 3**: AuditEntity 使用介面

```java
// ✅ Common Database - 使用介面（解耦）
@Component
public static class AuditMetaObjectHandler implements MetaObjectHandler {
    
    @Autowired(required = false) // 可選注入，啟動時不強制要求
    private UserContext userContext;
    
    @Override
    public void insertFill(MetaObject metaObject) {
        if (userContext != null && userContext.isAuthenticated()) {
            Long userId = userContext.getCurrentUserId();
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
        }
    }
}
```

**架構優勢**:

- ✅ 實現依賴倒置原則（DIP）
- ✅ Common Layer 不依賴業務模組
- ✅ Auth 模組可獨立演進
- ✅ 支援多種認證實作（JWT、OAuth2、Session）

**依賴關係圖**:

```text
┌─────────────────────────┐
│  Common Core Module     │
│  - UserContext (介面)   │  ← 高層策略（抽象）
└─────────┬───────────────┘
          │ depends on
          │
┌─────────▼───────────────┐
│  Common Database Module │
│  - AuditMetaObjectHandler│  ← 使用 UserContext 介面
└─────────────────────────┘
          ▲ implements
          │
┌─────────┴───────────────┐
│  Auth/Security Module   │
│  - SecurityUserContext   │  ← 低層實作（具體）
└─────────────────────────┘
```

**修改位置**:

- `specs/master/data-model.md` - 新增 UserContext 介面定義和 AuditEntity 改進
- `specs/master/spec.md` - 新增 FR1.6: UserContext 介面定義
- `specs/master/plan.md` - 更新憲法檢查說明

---

### 3. MyBatis-Plus 租戶注入機制澄清 (🟡 建議)

#### 原問題

- `spec.md` AC2 描述：「TenantInterceptor 能自動在 INSERT 語句中注入 tenant_id」
- **實際行為**：MyBatis-Plus 的 `TenantLineInnerInterceptor` 主要用於 SELECT/UPDATE/DELETE 過濾，INSERT 需要 `MetaObjectHandler`

#### 解決方案

**明確職責分工**:

1. **TenantLineInnerInterceptor** (MyBatis-Plus 租戶插件)
   - 負責：自動在 SQL 中添加 `WHERE tenant_id = ?` 條件
   - 適用：SELECT、UPDATE、DELETE 語句
   - 配置：`TenantLineHandler.getTenantId()`

2. **MetaObjectHandler** (MyBatis-Plus 字段填充)
   - 負責：自動填充 INSERT/UPDATE 時的字段值
   - 適用：`@TableField(fill = FieldFill.INSERT)` 標記的字段
   - 實作：`TenantMetaObjectHandler.insertFill()`

**更新後的需求**:

```markdown
#### FR2: Common Database（數據庫公共模組）

- FR2.2: MyBatis TenantLineInnerInterceptor 自動過濾 SELECT/UPDATE/DELETE 的 tenantId
- FR2.3: MetaObjectHandler 自動填充 INSERT 時的 tenantId、審計字段
```

**修改位置**:

- `specs/master/spec.md` - FR2 需求描述
- `specs/master/plan.md` - 複雜度追蹤說明

---

### 4. MDC Trace ID 支援 (🟡 建議)

#### 原問題

- 架構設計提到 Trace Module 為 Infrastructure 層的一部分
- Common Web 缺少 Trace ID 的傳遞機制
- 日誌無法串聯，分散式追蹤困難

#### 解決方案

**新增 TraceContext 和 TraceIdFilter**:

```java
// ✅ TraceContext - Trace ID 管理
public class TraceContext {
    private static final String TRACE_ID_KEY = "traceId";
    
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }
    
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}

// ✅ TraceIdFilter - 請求級別的 Trace ID 注入
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高優先級
public class TraceIdFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            // 從請求頭提取或生成 Trace ID
            String traceId = httpRequest.getHeader("X-Trace-Id");
            if (traceId == null) {
                traceId = TraceContext.generateTraceId();
            }
            
            TraceContext.setTraceId(traceId);
            httpResponse.setHeader("X-Trace-Id", traceId);
            
            chain.doFilter(request, response);
        } finally {
            TraceContext.clear(); // 防止線程池污染
        }
    }
}
```

**Logback 配置**:

```xml
<encoder>
    <!-- 日誌格式中包含 traceId -->
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] %-5level %logger - %msg%n</pattern>
</encoder>
```

**日誌輸出範例**:

```
2025-11-24 10:30:15.123 [a1b2c3d4e5f6] INFO  UserController - 查詢使用者: 123
2025-11-24 10:30:15.145 [a1b2c3d4e5f6] DEBUG UserService - 從資料庫載入使用者
2025-11-24 10:30:15.167 [a1b2c3d4e5f6] INFO  UserController - 查詢成功
```

**優勢**:

- ✅ 請求全鏈路追蹤
- ✅ 日誌自動串聯（同一個 Trace ID）
- ✅ 問題排查效率提升
- ✅ 支援未來微服務間的 Trace ID 傳遞

**修改位置**:

- `specs/master/spec.md` - 新增 FR4.4: MDC Trace ID 過濾器
- `specs/master/data-model.md` - 新增第 9 節 TraceContext 文檔
- `specs/master/plan.md` - 更新憲法檢查和複雜度追蹤

---

### 5. TimescaleDB 需求澄清 (🟡 建議)

#### 原問題

- `quickstart.md` 指定使用 `timescaledb-ha:pg14-latest`
- `research.md` 未討論 TimescaleDB 的必要性
- Common Layer 不需要時序數據庫特性

#### 解決方案

**簡化為標準 PostgreSQL**:

```yaml
# ❌ 原配置
services:
  postgres:
    image: timescale/timescaledb-ha:pg14-latest

# ✅ 簡化後
services:
  postgres:
    image: postgres:14-alpine  # 或 postgres:14
```

**決策理由**:

1. Common Layer 不涉及時序數據分析
2. TimescaleDB 為可選優化，適用於 Audit Module
3. 降低基礎設施複雜度和學習曲線
4. PostgreSQL 14 已滿足所有 Common Layer 需求

**未來演進路徑**:

- Phase 1 (Common Layer): 使用標準 PostgreSQL
- Phase 2 (Audit Module): 評估是否需要 TimescaleDB 時序優化
- 決策依據：審計日誌量級（如果 > 1000萬筆/月，考慮 TimescaleDB）

**修改位置**:

- `specs/master/plan.md` - 儲存技術說明
- `specs/master/quickstart.md` - 前置需求和測試容器配置

---

## 憲法合規性驗證

### 更新後的憲法檢查

| 原則 | 狀態 | 改進說明 |
|------|------|---------|
| **I. 模組化低耦合架構** | ✅ PASS | **UserContext 介面**實現依賴倒置，Common-Auth 完全解耦 |
| **II. 預設多租戶隔離** | ✅ PASS | `TenantLineInnerInterceptor` + `MetaObjectHandler` 雙層保障 |
| **III. 安全性優先開發** | ✅ PASS | **Snowflake ID** 防止業務量洩露，審計追蹤完整 |
| **IV. 所有關鍵操作的稽核軌跡** | ✅ PASS | **MDC Trace ID** 提供全鏈路追蹤 |
| **V. 關鍵路徑的測試驅動開發** | ✅ PASS | 無變更 |
| **VI. API 優先設計與版本控制** | ✅ PASS | 無變更 |

### 新增合規項目

| 新增要求 | 狀態 | 實作 |
|---------|------|------|
| **分散式系統就緒** | ✅ PASS | Snowflake ID + MDC Trace ID 支援分散式演進 |
| **依賴解耦** | ✅ PASS | UserContext 介面實現依賴倒置原則（DIP） |

---

## 文件更新清單

| 文件 | 狀態 | 主要變更 |
|------|------|---------|
| `data-model.md` | ✅ 完成 | 1. BaseEntity ID 改為 ASSIGN_ID<br>2. 新增 UserContext 介面定義<br>3. AuditEntity 使用 MetaObjectHandler<br>4. 新增 TraceContext 文檔 |
| `spec.md` | ✅ 完成 | 1. FR1.6: 新增 UserContext 介面<br>2. FR2.2-2.4: 澄清租戶注入機制<br>3. FR4.4: 新增 MDC Trace ID 過濾器 |
| `plan.md` | ✅ 完成 | 1. 更新憲法檢查說明<br>2. 新增架構改進文檔<br>3. PostgreSQL 配置簡化 |
| `quickstart.md` | ✅ 完成 | 1. PostgreSQL 前置需求更新<br>2. 測試容器配置簡化 |

---

## 技術決策記錄 (ADR)

### ADR-001: 使用 Snowflake ID 替代自增主鍵

**決策**: 使用 MyBatis-Plus 的 `ASSIGN_ID` (Snowflake 演算法) 生成主鍵  
**日期**: 2025-11-24  
**狀態**: ✅ 已接受

**理由**:

- 支援分庫分表和數據遷移
- 防止業務量洩露
- 全域唯一性無需協調
- 趨勢遞增有利於索引性能

**替代方案**:

- UUID: 無序，索引性能差
- 資料庫序列: 單點依賴

### ADR-002: 透過介面解耦審計與認證

**決策**: 定義 `UserContext` 介面，Auth 模組實作  
**日期**: 2025-11-24  
**狀態**: ✅ 已接受

**理由**:

- 遵循依賴倒置原則（DIP）
- Common Layer 保持獨立性
- 支援多種認證機制

**替代方案**:

- 直接依賴 Spring Security: 循環依賴
- 延遲到 Auth 模組填充: 無法自動化

### ADR-003: 使用 MDC 實現 Trace ID 傳遞

**決策**: 使用 SLF4J MDC 存儲 Trace ID  
**日期**: 2025-11-24  
**狀態**: ✅ 已接受

**理由**:

- SLF4J 標準機制
- 自動傳遞到所有日誌
- 支援跨執行緒（InheritableThreadLocal）

**替代方案**:

- Spring Cloud Sleuth: 依賴過重
- 自定義 ThreadLocal: 重複造輪子

### ADR-004: Common Layer 使用標準 PostgreSQL

**決策**: 不在 Common Layer 引入 TimescaleDB  
**日期**: 2025-11-24  
**狀態**: ✅ 已接受

**理由**:

- Common Layer 無時序數據需求
- 降低基礎設施複雜度
- PostgreSQL 14 已滿足需求

**未來考慮**:

- Audit Module 階段評估 TimescaleDB

---

## 影響評估

### 正面影響

1. **架構健壯性** ⬆️
   - 解決循環依賴問題
   - 支援分散式系統演進

2. **可維護性** ⬆️
   - 職責清晰（MetaObjectHandler vs TenantLineInnerInterceptor）
   - 文檔完整

3. **可追蹤性** ⬆️
   - Trace ID 日誌串聯
   - 問題排查效率提升

4. **安全性** ⬆️
   - Snowflake ID 防止業務量洩露
   - 完整的審計追蹤

### 潛在風險

| 風險 | 機率 | 影響 | 緩解措施 |
|------|------|------|---------|
| UserContext 未實作 | 低 | 中 | `@Autowired(required = false)` 允許啟動 |
| Snowflake ID 時鐘回撥 | 極低 | 高 | MyBatis-Plus 內建時鐘回撥保護 |
| MDC 跨執行緒丟失 | 低 | 低 | 使用 InheritableThreadLocal 或手動傳遞 |

---

## 驗收標準

### 修正完成驗收

- [x] 所有文檔已更新
- [x] 憲法檢查已通過
- [x] 架構改進已記錄
- [x] 技術決策已文檔化

### 實作驗收（Phase 1-2 執行）

未來實作時需驗證：

- [ ] BaseEntity 使用 `@TableId(type = IdType.ASSIGN_ID)` 並生成 64-bit ID
- [ ] UserContext 介面在 common-core 中定義
- [ ] AuditMetaObjectHandler 能自動填充 created_by/updated_by
- [ ] TraceIdFilter 能正確生成/傳遞/清理 Trace ID
- [ ] 日誌輸出包含 `[traceId]`
- [ ] 使用 postgres:14-alpine 容器測試通過

---

## 後續行動

### 立即行動（Phase 1 開始前）

1. ✅ 文檔審查完成
2. ✅ 架構改進記錄
3. ⏳ Code Review 確認改進點

### Phase 1 實作時

1. ⏳ 實作 UserContext 介面
2. ⏳ 實作 TraceContext 和 TraceIdFilter
3. ⏳ 配置 MyBatis-Plus 的 ASSIGN_ID
4. ⏳ 實作 AuditMetaObjectHandler

### Phase 2 驗證時

1. ⏳ 整合測試覆蓋所有改進點
2. ⏳ 性能測試（Snowflake ID 生成效率）
3. ⏳ 日誌追蹤端到端驗證

---

## 結論

所有 5 個架構問題已全部修正並文檔化。改進後的 Common Layer 設計：

✅ **解耦完整**: UserContext 介面實現依賴倒置  
✅ **可追蹤**: MDC Trace ID 提供全鏈路追蹤  
✅ **可擴展**: Snowflake ID 支援分散式演進  
✅ **職責清晰**: 租戶過濾與字段填充機制明確  
✅ **簡潔實用**: 使用標準 PostgreSQL，降低複雜度

**Common Layer 現已準備好作為穩固的基礎設施層支撐所有業務模組開發。**

---

**審查狀態**: ✅ 已完成  
**實作狀態**: ⏳ 待 Phase 1 執行  
**文檔版本**: 1.0  
**最後更新**: 2025-11-24
