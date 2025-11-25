# 功能規格：認證授權模組

**功能分支**: `001-authentication`  
**建立日期**: 2025-11-25  
**狀態**: 草稿  
**輸入**: 使用者描述：「Auth Module（認證授權）：JWT Token 生成/驗證（方案1：極簡JWT + Redis）、登入/登出、Redis Session 管理、基礎權限校驗框架」

---

## 使用者場景與測試 *(必填)*

### 使用者故事 1 - 使用者透過帳號密碼登入系統 (優先順序：P1)

使用者輸入使用者名稱和密碼以獲取系統存取權限。系統驗證憑證，生成 JWT Token（包含 tenant_id 和使用者權限資訊），並返回給客戶端以供後續請求使用。

**為何此優先順序**：這是整個 RBAC 系統的入口點。沒有認證能力，所有其他模組（租戶管理、使用者管理、權限控制）都無法運作。這是最基礎的 P1 功能。

**獨立測試**：可以透過發送 POST /api/v1/auth/login 請求，包含有效的使用者名稱和密碼，驗證返回 200 狀態碼和包含 JWT Token 的回應。解碼 Token 應包含 user_id、tenant_id 和 username。

**驗收場景**：

1. **假設** 系統中存在使用者 "john" 密碼為 "SecurePass123!"，**當** 使用者提交正確的帳號密碼，**則** 系統返回 JWT Token，Token 包含 user_id、tenant_id、username、roles 和過期時間 (24小時)
2. **假設** 使用者提交正確的帳號但錯誤的密碼，**當** 登入請求發送，**則** 系統返回 401 Unauthorized 並顯示「帳號或密碼錯誤」
3. **假設** 使用者提交不存在的帳號，**當** 登入請求發送，**則** 系統返回 401 Unauthorized 並顯示「帳號或密碼錯誤」（避免洩漏帳號是否存在）
4. **假設** 使用者連續 5 次輸入錯誤密碼，**當** 第 5 次失敗後，**則** 系統鎖定該帳號 15 分鐘，並返回「帳號已鎖定，請 15 分鐘後再試」

---

### 使用者故事 2 - 系統驗證 JWT Token 並提取使用者上下文 (優先順序：P1)

當使用者攜帶 JWT Token 發送請求時，系統自動驗證 Token 的有效性（簽章、過期時間、黑名單），並將使用者資訊（user_id、tenant_id、username、roles）注入到 UserContext 中，供後續業務邏輯使用。

**為何此優先順序**：這是認證系統的核心。所有受保護的 API 都依賴此功能來識別請求者身份。租戶管理模組需要從 UserContext 獲取 tenant_id 和 user_id 來實現資料隔離和稽核。

**獨立測試**：可以透過攜帶有效 JWT Token 發送任何受保護的 API 請求（例如 GET /api/v1/users/me），驗證系統能正確解析 Token 並返回當前使用者資訊，而不返回 401 錯誤。

**驗收場景**：

1. **假設** 使用者持有有效的 JWT Token，**當** 使用者發送任何受保護的 API 請求時，**則** 系統成功驗證 Token 並允許請求通過，UserContext.getCurrentUser() 返回正確的使用者資訊
2. **假設** 使用者持有已過期的 JWT Token，**當** 使用者發送 API 請求時，**則** 系統返回 401 Unauthorized 並顯示「Token 已過期，請重新登入」
3. **假設** 使用者持有被篡改的 JWT Token（簽章不匹配），**當** 使用者發送 API 請求時，**則** 系統返回 401 Unauthorized 並顯示「Token 無效」
4. **假設** 使用者的 Token 在 Redis 黑名單中（已登出），**當** 使用者發送 API 請求時，**則** 系統返回 401 Unauthorized 並顯示「Token 已失效，請重新登入」
5. **假設** 請求未攜帶 Authorization Header，**當** 存取受保護的 API 時，**則** 系統返回 401 Unauthorized 並顯示「未提供認證資訊」

---

### 使用者故事 3 - 使用者登出並撤銷 Token (優先順序：P1)

使用者點擊「登出」後，系統將當前 JWT Token 加入 Redis 黑名單，確保該 Token 無法再用於後續請求。即使 Token 尚未過期，也無法繼續使用。

**為何此優先順序**：這是安全性的基本要求。沒有登出功能，使用者無法主動撤銷權限（例如換裝置後需要登出舊裝置），也無法應對 Token 洩漏的風險。

**獨立測試**：可以透過先登入獲取 Token，然後發送 POST /api/v1/auth/logout 請求，之後使用相同 Token 存取受保護 API，驗證系統返回 401 Unauthorized。

**驗收場景**：

1. **假設** 使用者已登入並持有有效 JWT Token，**當** 使用者發送登出請求時，**則** 系統將 Token 加入 Redis 黑名單（TTL 設為 Token 剩餘有效期），並返回 200 OK 「登出成功」
2. **假設** 使用者已登出（Token 在黑名單中），**當** 使用者嘗試使用該 Token 存取任何 API 時，**則** 系統返回 401 Unauthorized 「Token 已失效，請重新登入」
3. **假設** 使用者發送登出請求但未攜帶 Token，**當** 請求發送時，**則** 系統返回 401 Unauthorized 「未提供認證資訊」

---

### 使用者故事 4 - 系統提供 UserContext 介面供其他模組使用 (優先順序：P1)

其他業務模組（例如租戶管理、使用者管理）可以透過 UserContext.getCurrentUser() 獲取當前已認證使用者的資訊，包括 user_id、tenant_id、username、roles，無需手動解析 JWT Token。

**為何此優先順序**：這是認證模組對外的核心 API。租戶管理模組的稽核欄位（created_by、updated_by）依賴此功能。沒有此介面，其他模組無法知道「誰在做什麼」。

**獨立測試**：可以透過在任何 Controller 或 Service 中呼叫 UserContext.getCurrentUser()，驗證返回的物件包含正確的 user_id、tenant_id 和 username，與 JWT Token 中的資訊一致。

**驗收場景**：

1. **假設** 使用者已認證並持有有效 Token，**當** 業務邏輯呼叫 UserContext.getCurrentUser() 時，**則** 返回包含 user_id、tenant_id、username、roles 的物件
2. **假設** 使用者未認證（未攜帶 Token 或 Token 無效），**當** 業務邏輯呼叫 UserContext.getCurrentUser() 時，**則** 拋出 UnauthorizedException 「使用者未認證」
3. **假設** 使用者在請求處理過程中，**當** 呼叫 UserContext.getTenantId() 時，**則** 返回該使用者所屬的 tenant_id，確保租戶隔離正確運作

---

### 使用者故事 5 - 系統支援基於註解的權限校驗 (優先順序：P2)

開發者可以在 Controller 或 Service 方法上使用 `@PreAuthorize("hasPermission('tenant:create')")` 註解，系統自動檢查當前使用者是否具備該權限。如果沒有權限，系統返回 403 Forbidden。

**為何此優先順序**：這是權限控制的基礎框架。雖然初期可以透過手動檢查權限實現基本功能，但標準化的註解方式能大幅簡化後續開發。P2 是因為可以先實現核心認證，後續再完善權限框架。

**獨立測試**：可以透過在測試 Controller 的方法上加上 `@PreAuthorize("hasPermission('test:read')")`，然後使用具有和不具有該權限的使用者 Token 存取該 API，驗證權限控制生效。

**驗收場景**：

1. **假設** Controller 方法標註 `@PreAuthorize("hasPermission('tenant:create')")`，**當** 具有 tenant:create 權限的使用者存取該 API 時，**則** 請求成功通過
2. **假設** Controller 方法標註 `@PreAuthorize("hasPermission('tenant:create')")`，**當** 不具有該權限的使用者存取該 API 時，**則** 系統返回 403 Forbidden 「權限不足」
3. **假設** Controller 方法未標註任何權限註解，**當** 任何已認證使用者存取該 API 時，**則** 請求成功通過（只要求已認證，不檢查權限）

---

### Edge Cases

- **Token 在請求處理過程中過期**：如果 Token 在請求進入時有效，但在處理過程中過期，是否中斷請求？（合理預設：不中斷，請求完成後才要求重新認證）
- **並發登出**：使用者在不同裝置同時登出，Redis 黑名單操作是否有競爭條件？（合理預設：Redis 操作是原子性的，使用 SET NX + TTL）
- **密碼鎖定期間重置密碼**：使用者帳號因多次錯誤密碼被鎖定後，如果透過「忘記密碼」流程重置密碼，是否解除鎖定？（合理預設：重置密碼後自動解除鎖定）
- **JWT Secret 輪換**：如果系統更換 JWT 簽章密鑰，舊 Token 是否全部失效？（合理預設：支援多密鑰驗證，舊密鑰在過渡期內仍可驗證，但僅用新密鑰簽發）
- **跨租戶存取**：如果管理員需要以 super-admin 身份存取多個租戶的資料，如何處理 tenant_id？（合理預設：初版不支援，所有使用者綁定單一租戶）

## Requirements *(必填)*

### Functional Requirements

- **FR-001**: 系統必須提供 POST /api/v1/auth/login API，接受 username 和 password，返回 JWT Token
- **FR-002**: 系統必須在 JWT Token 的 Payload 中包含 user_id、tenant_id、username、roles 和過期時間
- **FR-003**: 系統必須使用 HMAC-SHA256 演算法簽發和驗證 JWT Token
- **FR-004**: 系統必須在每次 API 請求時驗證 JWT Token 的簽章、過期時間和黑名單狀態
- **FR-005**: 系統必須提供 POST /api/v1/auth/logout API，將當前 Token 加入 Redis 黑名單
- **FR-006**: 系統必須在 Redis 中儲存 Token 黑名單，Key 為 `auth:blacklist:{token}`，TTL 為 Token 剩餘有效期
- **FR-007**: 系統必須提供 UserContext 介面，包含 getCurrentUser()、getUserId()、getTenantId()、getUsername() 方法
- **FR-008**: 系統必須在請求處理前將解析後的使用者資訊儲存在 ThreadLocal 中，請求結束後清理
- **FR-009**: 系統必須在密碼錯誤 5 次後鎖定帳號 15 分鐘，並在 Redis 中記錄鎖定狀態（Key: `auth:lock:{username}`，TTL: 15分鐘）
- **FR-010**: 系統必須支援 @PreAuthorize 註解，格式為 `@PreAuthorize("hasPermission('resource:action')")`
- **FR-011**: 系統必須在權限驗證失敗時返回 403 Forbidden，並記錄日誌（包含 user_id、tenant_id、請求的權限碼）
- **FR-012**: 系統必須在 Token 無效或缺失時返回 401 Unauthorized，並包含清楚的錯誤訊息
- **FR-013**: 系統必須在登入成功時記錄日誌，包含 user_id、tenant_id、IP 位址和登入時間
- **FR-014**: 系統必須提供 GET /api/v1/auth/me API，返回當前已認證使用者的基本資訊（user_id、username、tenant_id、roles）
- **FR-015**: 系統必須在使用者輸入錯誤密碼時返回通用錯誤訊息「帳號或密碼錯誤」，避免洩漏帳號是否存在

### Key Entities

- **User (使用者)**：代表可以登入系統的帳號，包含 user_id、username (唯一)、password_hash、tenant_id、status (ACTIVE/LOCKED)、roles (關聯到角色)
- **JWT Token**：包含使用者身份資訊的安全令牌，包含 Payload (user_id, tenant_id, username, roles, exp), Signature (HMAC-SHA256)，有效期 24 小時
- **Token Blacklist (Token 黑名單)**：儲存在 Redis 中的已登出 Token 清單，Key 格式 `auth:blacklist:{token}`，TTL 為 Token 剩餘有效期
- **UserContext (使用者上下文)**：ThreadLocal 儲存的當前請求使用者資訊，包含 user_id、tenant_id、username、roles
- **Login Lock (登入鎖定)**：儲存在 Redis 中的帳號鎖定狀態，Key 格式 `auth:lock:{username}`，TTL 為 15 分鐘，Value 為鎖定原因和時間

## Success Criteria *(必填)*

### Measurable Outcomes

- **SC-001**: 使用者可以在 2 秒內完成登入流程並獲得 JWT Token
- **SC-002**: 系統可以在 100 毫秒內完成 JWT Token 驗證（包含 Redis 黑名單查詢）
- **SC-003**: 系統可以處理 1000 個並發登入請求而不出現錯誤或顯著延遲（P99 < 3 秒）
- **SC-004**: 登出後的 Token 在 1 秒內無法再用於 API 請求（Redis 黑名單即時生效）
- **SC-005**: 使用者在帳號被鎖定後 15 分鐘可以恢復登入能力
- **SC-006**: 系統可以正確阻止 100% 的無效 Token 請求（過期、篡改、黑名單）
- **SC-007**: 權限驗證失敗率低於 0.1%（排除正常的權限不足情況）
- **SC-008**: 所有認證相關操作（登入、登出、Token 驗證）的日誌記錄完整率達 100%
- **SC-009**: UserContext.getCurrentUser() 在已認證請求中的成功率達 100%
- **SC-010**: 系統在高負載下（5000 QPS）Token 驗證的 P95 延遲低於 150 毫秒

## Assumptions *(選填)*

- **Auth Module 初版使用 Mock 資料（重要）**：
  - 由於 User/Tenant/Role Module 尚未開發，初版使用硬編碼測試資料
  - 測試使用者：`admin`/`admin123`、`john`/`SecurePass123!`
  - JWT Token 中的 `tenant_id` 固定為 `1`（模擬預設租戶）
  - JWT Token 中的 `roles` 固定為 `["ROLE_USER"]`（模擬預設角色）
  - 使用記憶體 Map 或配置檔儲存測試使用者資料（username, password_hash, user_id）
  - **當 User/Tenant/Role Module 完成後，需重構為查詢資料庫**
- 使用者密碼已預先儲存為 BCrypt Hash（密碼加密和使用者註冊功能屬於 User Module，不在此模組範圍）
- JWT Secret 儲存在環境變數或配置檔中（初版使用單一密鑰，不支援密鑰輪換）
- 所有使用者綁定單一租戶，不支援跨租戶存取（super-admin 跨租戶功能留待後續實現）
- Redis 可用性由基礎設施保證（使用 Redis Sentinel 或 Cluster 確保高可用）
- 權限碼格式為 `resource:action`（例如 `tenant:create`、`user:read`），權限與角色的對應關係由 Permission Module 管理
- Token 有效期固定為 24 小時（不支援 Refresh Token，使用者需每 24 小時重新登入）
- 系統時鐘同步由 NTP 保證（JWT exp 校驗依賴伺服器時間準確性）

## Dependencies *(選填)*

- **Common Layer**：需要 rbac-common-redis 提供 Redis 操作工具類，rbac-common-core 提供 Result、BaseException

- **設計原則：依賴倒置（低耦合架構）**：
  - Auth Module 僅依賴 `UserRepository` 介面（抽象），不依賴具體實作
  - 初版使用 `MockUserRepository` 實作（記憶體 Map 或配置檔）
  - 後期使用 `JpaUserRepository` 實作（資料庫查詢）
  - **核心優勢**：
    - AuthService 的 JWT 邏輯完全不受資料來源影響（符合開放封閉原則）
    - 透過 Spring `@Profile` 或 `@ConditionalOnProperty` 自動切換實作
    - 重構時不需修改核心認證邏輯，只需新增資料庫實作類別
  - **實作範例**：

    ```java
    // 介面定義（穩定的抽象）
    public interface UserRepository {
        Optional<User> findByUsername(String username);
        boolean validatePassword(String raw, String hash);
    }
    
    // 初版實作（開發環境）
    @Repository
    @Profile("dev")
    public class MockUserRepository implements UserRepository { ... }
    
    // 後期實作（生產環境）
    @Repository
    @Profile("prod")
    public class JpaUserRepository implements UserRepository { ... }
    
    // AuthService 只依賴介面（低耦合）
    @Service
    public class AuthService {
        @Autowired
        private UserRepository userRepository;  // 自動注入對應實作
    }
    ```

- **初版不依賴 User/Tenant/Role Module（使用 Mock 資料）**：
  - 初版使用記憶體 Map 或配置檔儲存測試使用者（避免依賴尚未開發的模組）
  - JWT Token 中的 tenant_id 和 roles 使用硬編碼預設值
  - Mock 使用者資料包含預先計算的 BCrypt Hash（推薦使用 application.yml 或 Java 常數）
  - **重構時機**：當 User Module 完成後，替換 Mock 資料來源為資料庫查詢（核心 JWT 邏輯不變）

- **後續模組依賴本模組**：Tenant Module、User Module、Role Module、Permission Module 都依賴此模組提供的 UserContext 和權限框架

## Out of Scope *(選填)*

- 使用者註冊功能（屬於 User Module）
- 密碼重置/忘記密碼功能（屬於 User Module）
- 多因素認證 (MFA)（留待未來版本）
- OAuth2 / SSO 整合（留待未來版本）
- Refresh Token 機制（初版使用固定 24 小時 Token）
- 細粒度權限規則引擎（例如支援 AND/OR 邏輯組合，初版僅支援單一權限碼校驗）
- 角色和權限的 CRUD 管理（屬於 Role Module 和 Permission Module）
