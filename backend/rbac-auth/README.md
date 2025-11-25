# RBAC Authentication Module

RBAC 系統的認證模組，提供基於 JWT 的身份認證和授權功能。

## 功能特點

### 🔐 身份認證
- **JWT Token 認證**: 使用 HMAC-SHA256 簽名的 JWT Token
- **安全登入**: BCrypt 密碼雜湊，防止暴力破解
- **帳號鎖定**: 5 次失敗嘗試後鎖定帳號 15 分鐘
- **Token 黑名單**: 登出後 Token 立即失效

### 🛡️ 安全特性
- **無狀態認證**: 不依賴服務端會話
- **Token 過期**: 24 小時有效期
- **請求攔截**: 自動驗證每個請求的 JWT Token
- **多租戶支援**: 通過 UserContext 實現租戶隔離

### 📊 用戶上下文
- **ThreadLocal 管理**: 請求級別的用戶信息共享
- **自動注入**: 過濾器自動解析並設置用戶上下文
- **記憶體安全**: 請求結束後自動清理，防止洩漏

## API 接口

### 登入
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**回應**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400
  }
}
```

### 獲取用戶信息
```http
GET /api/v1/auth/me
Authorization: Bearer <token>
```

**回應**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "admin",
    "tenantId": "tenant1",
    "roles": ["ADMIN", "USER"]
  }
}
```

### 登出
```http
POST /api/v1/auth/logout
Authorization: Bearer <token>
```

**回應**:
```json
{
  "code": 200,
  "message": "success"
}
```

## 技術架構

### 核心組件

#### 服務層
- **JwtTokenService**: JWT Token 的生成、驗證和解析
- **AuthService**: 登入和登出業務邏輯，包含帳號鎖定機制

#### 安全配置
- **SecurityConfig**: Spring Security 配置，定義認證規則
- **JwtAuthenticationFilter**: 請求攔截器，驗證 JWT Token

#### 數據訪問
- **UserRepository**: 用戶數據訪問接口
- **MockUserRepository**: 開發環境的記憶體實現

#### 異常處理
- **GlobalExceptionHandler**: 統一異常處理和錯誤響應
- **自定義異常**: AuthenticationException, TokenExpiredException, AccountLockedException

### 依賴注入
```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // JWT 認證配置
}
```

## 配置說明

### JWT 配置
```yaml
rbac:
  jwt:
    secret: your-secret-key-here
    expiration: 86400
```

### Redis 配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Mock 用戶 (開發環境)
```yaml
rbac:
  mock:
    users:
      - username: admin
        passwordHash: $2a$10$...
        tenantId: tenant1
        roles: [ADMIN, USER]
```

## 安全考慮

### 密碼安全
- 使用 BCrypt 進行密碼雜湊
- 最小密碼長度驗證
- 防止常見密碼攻擊

### Token 安全
- HMAC-SHA256 簽名算法
- Token 黑名單機制
- 自動過期處理

### 帳號保護
- 失敗嘗試次數限制
- 臨時帳號鎖定
- 詳細的錯誤日誌記錄

## 測試

### 單元測試
```bash
mvn test -Dtest="*Test"
```

### 整合測試
```bash
mvn test -Dtest="*IntegrationTest"
```

### API 文檔
啟動應用後訪問: `http://localhost:8080/swagger-ui.html`

## 開發環境

### 啟動應用
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 測試登入
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

## 監控和日誌

### 日誌級別
- **INFO**: 成功登入/登出操作
- **WARN**: 認證失敗、Token 過期
- **ERROR**: 系統異常

### 關鍵指標
- 登入成功/失敗次數
- Token 驗證通過/失敗次數
- 帳號鎖定事件

## 擴展性

### 自定義認證
實現 `UserRepository` 接口以支持不同的數據源。

### 額外安全措施
- 實現雙因素認證
- 添加 IP 白名單
- 集成 OAuth2 提供者

### 性能優化
- Token 緩存
- Redis 集群支援
- 非同步日誌記錄</content>
<parameter name="filePath">d:\SideProject\rbac-system-v1\backend\rbac-auth\README.md