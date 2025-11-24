# Feature Specification: 001-common-layer

**Branch**: `001-common-layer` | **Date**: 2025-11-24 | **Status**: Planning

## Overview

實現多租戶RBAC系統的公共層（Common Layer），作為所有業務模組的基礎設施。公共層提供通用工具、數據訪問、緩存管理和Web層基礎設施。

## Problem Statement

在開始實現業務邏輯（Auth、User、Tenant、Permission等模組）之前，需要建立一個穩固的公共基礎設施層。這個基礎層必須：

1. 提供統一的異常處理和響應格式
2. 提供多租戶數據隔離的基礎設施
3. 提供緩存和分布式鎖支持
4. 提供通用的數據庫操作和實體基類
5. 提供Web層的通用組件（過濾器、攔截器、全局異常處理）

沒有這個基礎層，業務模組將無法開始開發。

## Requirements

### Functional Requirements

#### FR1: Common Core(核心公共模組)

- FR1.1: 統一異常定義(BusinessException、SystemException等)
- FR1.2: 統一響應格式(Result<T>包含code、message、data、timestamp)
- FR1.3: 通用工具類(字符串、日期、JSON、加密等)
- FR1.4: 常量定義(錯誤碼、系統參數等)
- FR1.5: 基礎數據模型(BaseEntity使用Snowflake ID、PageRequest、PageResponse)
- FR1.6: UserContext介面定義(解耦Common與Auth模組,用於審計追蹤)

#### FR2: Common Database(數據庫公共模組)

- FR2.1: 實體基類(BaseEntity使用Snowflake ID、TenantEntity包含tenantId)
- FR2.2: MyBatis TenantLineInnerInterceptor自動過濾SELECT/UPDATE/DELETE的tenantId
- FR2.3: MetaObjectHandler自動填充INSERT時的tenantId、審計字段(createdBy/updatedBy)
- FR2.4: 審計字段透過UserContext介面獲取當前使用者(解耦Auth模組)
- FR2.5: 邏輯刪除支持(deleted字段使用@TableLogic)
- FR2.6: 數據庫配置和連接池配置
- FR2.7: 多數據源支持(為讀寫分離預留DynamicDataSource配置)

#### FR3: Common Redis（Redis公共模組）

- FR3.1: Redis連接配置（單機/集群）
- FR3.2: 緩存工具類（get、set、delete、exists、expire）
- FR3.3: 分布式鎖實現（基於Redis）
- FR3.4: 序列化配置（JSON序列化）
- FR3.5: Key命名規範工具（tenant:module:key格式）

#### FR4: Common Web（Web公共模組）

- FR4.1: 全局異常處理器（捕獲所有異常並返回統一格式）
- FR4.2: 租戶上下文過濾器（從請求頭提取tenantId並設置到TenantContextHolder）
- FR4.3: 請求日誌攔截器（記錄請求路徑、參數、響應時間）
- FR4.4: MDC Trace ID過濾器（生成並傳遞Trace ID用於日誌串聯）
- FR4.5: 跨域配置（CORS）
- FR4.6: 參數驗證增強（統一處理@Valid驗證失敗）
- FR4.7: API版本控制支持（/api/v1/格式）

### Non-Functional Requirements

#### NFR1: 性能

- NFR1.1: Redis緩存操作響應時間 < 10ms（p95）
- NFR1.2: 分布式鎖獲取時間 < 50ms（正常情況）
- NFR1.3: MyBatis攔截器不增加超過5ms的查詢開銷

#### NFR2: 可靠性

- NFR2.1: 租戶隔離必須100%生效，無任何跨租戶數據洩漏
- NFR2.2: 全局異常處理必須捕獲所有未處理的異常
- NFR2.3: 分布式鎖必須防止死鎖（設置超時時間）

#### NFR3: 可維護性

- NFR3.1: 所有公共組件必須有清晰的Javadoc文檔
- NFR3.2: 配置項必須有默認值和說明
- NFR3.3: 日誌級別必須合理（減少生產環境日誌量）

#### NFR4: 安全性

- NFR4.1: 敏感配置（數據庫密碼、Redis密碼）不得硬編碼
- NFR4.2: 異常信息不得洩漏敏感數據（堆棧信息僅在開發環境顯示）
- NFR4.3: SQL注入防護（使用參數化查詢）

## Architecture

### Module Structure

```
rbac-system/
├── rbac-common/
│   ├── rbac-common-core/          # 核心公共模組
│   │   ├── exception/              # 異常定義
│   │   ├── model/                  # 基礎數據模型
│   │   ├── result/                 # 統一響應格式
│   │   ├── constant/               # 常量定義
│   │   └── util/                   # 工具類
│   ├── rbac-common-database/      # 數據庫公共模組
│   │   ├── entity/                 # 實體基類
│   │   ├── interceptor/            # MyBatis攔截器
│   │   ├── config/                 # 數據庫配置
│   │   └── util/                   # 數據庫工具
│   ├── rbac-common-redis/         # Redis公共模組
│   │   ├── config/                 # Redis配置
│   │   ├── lock/                   # 分布式鎖
│   │   └── util/                   # 緩存工具
│   └── rbac-common-web/           # Web公共模組
│       ├── filter/                 # 過濾器
│       ├── interceptor/            # 攔截器
│       ├── handler/                # 異常處理器
│       └── config/                 # Web配置
```

### Key Components

#### 1. TenantContextHolder（租戶上下文持有者）

```java
public class TenantContextHolder {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) { ... }
    public static String getTenantId() { ... }
    public static void clear() { ... }
}
```

#### 2. TenantInterceptor（租戶SQL攔截器）

```java
@Intercepts({
    @Signature(type = Executor.class, method = "query", ...),
    @Signature(type = Executor.class, method = "update", ...)
})
public class TenantInterceptor implements Interceptor {
    // 自動在SQL中添加 tenant_id = ? 條件
}
```

#### 3. DistributedLock（分布式鎖）

```java
public interface DistributedLock {
    boolean tryLock(String key, long timeout, TimeUnit unit);
    void unlock(String key);
}
```

#### 4. Result<T>（統一響應格式）

```java
public class Result<T> {
    private String code;      // 響應碼
    private String message;   // 響應消息
    private T data;           // 響應數據
    private Long timestamp;   // 時間戳
}
```

### Dependencies

- Spring Boot 3.5.x
- MyBatis 3.5.x / MyBatis-Plus 3.5.x
- Spring Data Redis
- Jedis / Lettuce
- Jackson（JSON序列化）
- Lombok（減少樣板代碼）
- SLF4J + Logback（日誌）

## Acceptance Criteria

### AC1: Common Core

- [ ] 定義至少5種常見業務異常類型
- [ ] Result<T>能夠處理成功和失敗兩種情況
- [ ] 提供至少10個常用工具方法（字符串、日期、JSON等）
- [ ] 錯誤碼定義清晰且不重複

### AC2: Common Database

- [ ] BaseEntity包含id、tenantId、createTime、updateTime、deleted
- [ ] TenantInterceptor能自動在SELECT語句中添加tenant_id過濾
- [ ] TenantInterceptor能自動在INSERT語句中注入tenant_id
- [ ] 審計字段在insert/update時自動填充
- [ ] 支持邏輯刪除（軟刪除）

### AC3: Common Redis

- [ ] 能成功連接Redis並執行基本操作
- [ ] 分布式鎖能防止並發問題（測試場景：100個線程競爭1個鎖）
- [ ] 分布式鎖能自動釋放（超時或正常釋放）
- [ ] 緩存Key遵循統一命名規範

### AC4: Common Web

- [ ] 全局異常處理器能捕獲所有未處理異常
- [ ] 租戶過濾器能從請求頭提取tenantId並設置到TenantContextHolder
- [ ] 請求完成後TenantContextHolder被清理（防止線程池污染）
- [ ] @Valid驗證失敗時返回統一格式的錯誤信息
- [ ] CORS配置允許前端跨域請求

## Testing Strategy

### Unit Tests

- 測試所有工具類方法
- 測試Result<T>的success()和error()方法
- 測試分布式鎖的lock和unlock邏輯
- 測試TenantContextHolder的set/get/clear

### Integration Tests

- 測試TenantInterceptor在真實MyBatis環境中的工作
- 測試Redis連接和緩存操作
- 測試全局異常處理器捕獲不同類型的異常
- 測試租戶過濾器在真實HTTP請求中的工作

### Security Tests

- 測試租戶隔離：租戶A不能訪問租戶B的數據
- 測試SQL注入防護
- 測試異常信息不洩漏敏感數據

## Migration Plan

N/A（這是第一個實現的模組，無需遷移）

## Rollout Plan

### Phase 1: 基礎設施（Week 1）

- Day 1-2: 實現Common Core（異常、Result、工具類）
- Day 3-4: 實現Common Database（BaseEntity、TenantInterceptor）
- Day 5: 編寫單元測試

### Phase 2: 緩存和Web層（Week 2）

- Day 1-2: 實現Common Redis（配置、工具類、分布式鎖）
- Day 3-4: 實現Common Web（過濾器、攔截器、異常處理器）
- Day 5: 編寫集成測試

### Phase 3: 驗證和文檔（Week 2）

- 運行所有測試並達到70%覆蓋率
- 編寫README和使用示例
- Code Review

## Success Metrics

- 單元測試覆蓋率 > 70%
- 所有Acceptance Criteria通過
- 租戶隔離測試100%通過
- 文檔完整且清晰

## Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| MyBatis攔截器性能問題 | High | Low | 在攔截器中優化SQL解析邏輯，避免重複解析 |
| 租戶隔離漏洞 | Critical | Medium | 全面的集成測試，Code Review重點檢查 |
| 分布式鎖死鎖 | High | Low | 設置合理的超時時間，使用try-finally確保釋放 |
| Redis連接失敗 | Medium | Low | 實現降級策略，Redis不可用時不影響核心功能 |

## References

- [系統架構設計](../../docs/01-系統架構設計.md)
- [多租戶隔離策略](../../docs/02-多租戶隔離策略.md)
- [低耦合設計指南](../../docs/04-低耦合設計指南.md)
- [數據庫設計文檔](../../docs/05-數據庫設計文檔.md)

## Appendix

### Configuration Example

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rbac_system
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}

rbac:
  tenant:
    header-name: X-Tenant-Id  # 租戶ID請求頭名稱
  redis:
    key-prefix: rbac          # Redis Key前綴
  lock:
    timeout: 30               # 分布式鎖超時時間（秒）
```

### Error Code Convention

```
[模組碼][錯誤類型][序號]

模組碼:
- 00: Common
- 01: Auth
- 02: User
- 03: Tenant
- 04: Permission
- 05: Security
- 06: Audit

錯誤類型:
- 1: 參數錯誤
- 2: 業務錯誤
- 3: 系統錯誤
- 4: 權限錯誤

示例:
- 00-1-001: Common模組參數錯誤001
- 01-4-001: Auth模組權限錯誤001
```
