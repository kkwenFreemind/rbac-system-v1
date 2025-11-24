# API設計文檔

## 1. 概述

本文檔描述多租戶RBAC系統的RESTful API設計規範、接口定義和使用說明。

## 2. API設計原則

### 2.1 RESTful規範

- 使用HTTP方法表示操作：GET（查詢）、POST（創建）、PUT（更新）、DELETE（刪除）
- 使用名詞複數形式作為資源路徑
- 使用HTTP狀態碼表示請求結果
- 支持分頁、排序、過濾
- 版本控制：通過URL路徑 `/api/v1/`

### 2.2 URL設計規範

```
基礎格式：
https://api.example.com/api/v1/{resource}

示例：
GET    /api/v1/users          # 獲取用戶列表
GET    /api/v1/users/{id}     # 獲取單個用戶
POST   /api/v1/users          # 創建用戶
PUT    /api/v1/users/{id}     # 更新用戶
DELETE /api/v1/users/{id}     # 刪除用戶
```

### 2.3 HTTP狀態碼

| 狀態碼 | 說明 | 使用場景 |
|-------|------|---------|
| 200 | OK | 請求成功 |
| 201 | Created | 資源創建成功 |
| 204 | No Content | 請求成功但無返回內容 |
| 400 | Bad Request | 請求參數錯誤 |
| 401 | Unauthorized | 未認證 |
| 403 | Forbidden | 無權限 |
| 404 | Not Found | 資源不存在 |
| 409 | Conflict | 資源衝突 |
| 500 | Internal Server Error | 服務器內部錯誤 |

### 2.4 統一響應格式

#### 成功響應

```json
{
  "code": 0,
  "message": "success",
  "data": {
    // 業務數據
  },
  "timestamp": 1700000000000
}
```

#### 錯誤響應

```json
{
  "code": 40001,
  "message": "用戶名已存在",
  "data": null,
  "timestamp": 1700000000000,
  "traceId": "abc123xyz"
}
```

#### 分頁響應

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      // 數據列表
    ],
    "pagination": {
      "current": 1,
      "pageSize": 10,
      "total": 100,
      "pages": 10
    }
  },
  "timestamp": 1700000000000
}
```

## 3. 認證與授權

### 3.1 認證方式

採用JWT（JSON Web Token）認證方式。

#### 請求頭

```
Authorization: Bearer {token}
X-Tenant-Id: {tenantId}  // 可選，優先從token中提取
```

#### Token結構

```json
{
  "sub": "user123",
  "tenantId": "tenant001",
  "username": "john",
  "roles": ["ADMIN"],
  "permissions": ["user:read", "user:write"],
  "iat": 1700000000,
  "exp": 1700086400
}
```

### 3.2 認證接口

#### 登入

```
POST /api/v1/auth/login
```

**請求參數**：

```json
{
  "username": "admin",
  "password": "password123",
  "tenantCode": "tenant001"
}
```

**響應**：

```json
{
  "code": 0,
  "message": "登入成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "管理員",
      "avatar": "https://example.com/avatar.jpg"
    }
  }
}
```

#### 登出

```
POST /api/v1/auth/logout
```

**響應**：

```json
{
  "code": 0,
  "message": "登出成功"
}
```

#### 刷新Token

```
POST /api/v1/auth/refresh
```

**請求參數**：

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**響應**：

```json
{
  "code": 0,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

## 4. 租戶管理API

### 4.1 創建租戶

```
POST /api/v1/tenants
```

**權限要求**：`tenant:create`

**請求參數**：

```json
{
  "tenantCode": "company001",
  "tenantName": "示例公司",
  "contactName": "張三",
  "contactPhone": "13800138000",
  "contactEmail": "zhangsan@example.com",
  "packageType": "STANDARD",
  "maxUsers": 50
}
```

**響應**：

```json
{
  "code": 0,
  "message": "創建成功",
  "data": {
    "id": 1,
    "tenantCode": "company001",
    "tenantName": "示例公司",
    "status": 1,
    "createdAt": "2025-11-21T10:00:00Z"
  }
}
```

### 4.2 獲取租戶列表

```
GET /api/v1/tenants
```

**權限要求**：`tenant:read`

**查詢參數**：

- `page`: 頁碼（默認1）
- `pageSize`: 每頁數量（默認10）
- `keyword`: 搜索關鍵字
- `status`: 狀態篩選

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "tenantCode": "company001",
        "tenantName": "示例公司",
        "packageType": "STANDARD",
        "status": 1,
        "createdAt": "2025-11-21T10:00:00Z"
      }
    ],
    "pagination": {
      "current": 1,
      "pageSize": 10,
      "total": 100,
      "pages": 10
    }
  }
}
```

### 4.3 獲取租戶詳情

```
GET /api/v1/tenants/{id}
```

**權限要求**：`tenant:read`

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "tenantCode": "company001",
    "tenantName": "示例公司",
    "contactName": "張三",
    "contactPhone": "13800138000",
    "contactEmail": "zhangsan@example.com",
    "packageType": "STANDARD",
    "maxUsers": 50,
    "currentUsers": 25,
    "maxStorage": 10737418240,
    "usedStorage": 5368709120,
    "isolationLevel": "ROW",
    "status": 1,
    "expireTime": "2026-11-21T10:00:00Z",
    "createdAt": "2025-11-21T10:00:00Z"
  }
}
```

### 4.4 更新租戶

```
PUT /api/v1/tenants/{id}
```

**權限要求**：`tenant:update`

**請求參數**：

```json
{
  "tenantName": "新公司名稱",
  "contactName": "李四",
  "contactPhone": "13900139000",
  "packageType": "PREMIUM",
  "maxUsers": 100
}
```

### 4.5 刪除租戶

```
DELETE /api/v1/tenants/{id}
```

**權限要求**：`tenant:delete`

## 5. 用戶管理API

### 5.1 創建用戶

```
POST /api/v1/users
```

**權限要求**：`user:create`

**請求參數**：

```json
{
  "username": "john",
  "password": "Password123!",
  "realName": "約翰",
  "email": "john@example.com",
  "phone": "13800138000",
  "departmentIds": [1, 2],
  "roleIds": [1, 2]
}
```

**響應**：

```json
{
  "code": 0,
  "message": "創建成功",
  "data": {
    "id": 100,
    "username": "john",
    "realName": "約翰",
    "email": "john@example.com",
    "status": 1,
    "createdAt": "2025-11-21T10:00:00Z"
  }
}
```

### 5.2 獲取用戶列表

```
GET /api/v1/users
```

**權限要求**：`user:read`

**查詢參數**：

- `page`: 頁碼
- `pageSize`: 每頁數量
- `keyword`: 搜索關鍵字（用戶名、姓名、郵箱）
- `status`: 狀態篩選
- `departmentId`: 部門篩選
- `roleId`: 角色篩選

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 100,
        "username": "john",
        "realName": "約翰",
        "email": "john@example.com",
        "phone": "13800138000",
        "avatar": "https://example.com/avatar.jpg",
        "status": 1,
        "departments": [
          {
            "id": 1,
            "name": "技術部",
            "isPrimary": true
          }
        ],
        "roles": [
          {
            "id": 1,
            "name": "管理員"
          }
        ],
        "lastLoginTime": "2025-11-21T09:00:00Z",
        "createdAt": "2025-11-20T10:00:00Z"
      }
    ],
    "pagination": {
      "current": 1,
      "pageSize": 10,
      "total": 50,
      "pages": 5
    }
  }
}
```

### 5.3 獲取用戶詳情

```
GET /api/v1/users/{id}
```

**權限要求**：`user:read`

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 100,
    "username": "john",
    "realName": "約翰",
    "nickname": "John",
    "email": "john@example.com",
    "phone": "13800138000",
    "avatar": "https://example.com/avatar.jpg",
    "userType": "NORMAL",
    "status": 1,
    "departments": [
      {
        "id": 1,
        "name": "技術部",
        "isPrimary": true
      }
    ],
    "roles": [
      {
        "id": 1,
        "code": "ADMIN",
        "name": "管理員"
      }
    ],
    "permissions": [
      "user:read",
      "user:write",
      "role:read"
    ],
    "lastLoginTime": "2025-11-21T09:00:00Z",
    "lastLoginIp": "192.168.1.100",
    "createdAt": "2025-11-20T10:00:00Z"
  }
}
```

### 5.4 更新用戶

```
PUT /api/v1/users/{id}
```

**權限要求**：`user:update`

**請求參數**：

```json
{
  "realName": "新姓名",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "departmentIds": [1],
  "roleIds": [1, 2]
}
```

### 5.5 刪除用戶

```
DELETE /api/v1/users/{id}
```

**權限要求**：`user:delete`

### 5.6 重置密碼

```
POST /api/v1/users/{id}/reset-password
```

**權限要求**：`user:reset-pwd`

**請求參數**：

```json
{
  "newPassword": "NewPassword123!"
}
```

### 5.7 批量刪除用戶

```
POST /api/v1/users/batch-delete
```

**權限要求**：`user:delete`

**請求參數**：

```json
{
  "userIds": [1, 2, 3]
}
```

## 6. 角色管理API

### 6.1 創建角色

```
POST /api/v1/roles
```

**權限要求**：`role:create`

**請求參數**：

```json
{
  "roleCode": "PROJECT_MANAGER",
  "roleName": "項目經理",
  "description": "負責項目管理",
  "dataScope": "DEPT",
  "permissionIds": [1, 2, 3, 4]
}
```

**響應**：

```json
{
  "code": 0,
  "message": "創建成功",
  "data": {
    "id": 10,
    "roleCode": "PROJECT_MANAGER",
    "roleName": "項目經理",
    "status": 1,
    "createdAt": "2025-11-21T10:00:00Z"
  }
}
```

### 6.2 獲取角色列表

```
GET /api/v1/roles
```

**權限要求**：`role:read`

**查詢參數**：

- `page`: 頁碼
- `pageSize`: 每頁數量
- `keyword`: 搜索關鍵字
- `status`: 狀態篩選
- `roleType`: 角色類型篩選

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 10,
        "roleCode": "PROJECT_MANAGER",
        "roleName": "項目經理",
        "roleType": "CUSTOM",
        "dataScope": "DEPT",
        "userCount": 5,
        "permissionCount": 20,
        "status": 1,
        "createdAt": "2025-11-21T10:00:00Z"
      }
    ],
    "pagination": {
      "current": 1,
      "pageSize": 10,
      "total": 20,
      "pages": 2
    }
  }
}
```

### 6.3 獲取角色詳情

```
GET /api/v1/roles/{id}
```

**權限要求**：`role:read`

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 10,
    "roleCode": "PROJECT_MANAGER",
    "roleName": "項目經理",
    "description": "負責項目管理",
    "roleType": "CUSTOM",
    "dataScope": "DEPT",
    "status": 1,
    "permissions": [
      {
        "id": 1,
        "code": "user:read",
        "name": "查看用戶"
      },
      {
        "id": 2,
        "code": "project:read",
        "name": "查看項目"
      }
    ],
    "userCount": 5,
    "createdAt": "2025-11-21T10:00:00Z"
  }
}
```

### 6.4 更新角色

```
PUT /api/v1/roles/{id}
```

**權限要求**：`role:update`

**請求參數**：

```json
{
  "roleName": "高級項目經理",
  "description": "負責重要項目管理",
  "dataScope": "DEPT_AND_CHILD",
  "permissionIds": [1, 2, 3, 4, 5]
}
```

### 6.5 刪除角色

```
DELETE /api/v1/roles/{id}
```

**權限要求**：`role:delete`

### 6.6 分配權限

```
POST /api/v1/roles/{id}/permissions
```

**權限要求**：`role:assign`

**請求參數**：

```json
{
  "permissionIds": [1, 2, 3, 4, 5]
}
```

### 6.7 分配用戶

```
POST /api/v1/roles/{id}/users
```

**權限要求**：`role:assign`

**請求參數**：

```json
{
  "userIds": [10, 20, 30]
}
```

## 7. 權限管理API

### 7.1 獲取權限樹

```
GET /api/v1/permissions/tree
```

**權限要求**：`permission:read`

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "user",
      "name": "用戶管理",
      "type": "MENU",
      "children": [
        {
          "id": 2,
          "code": "user:read",
          "name": "查看用戶",
          "type": "API",
          "children": []
        },
        {
          "id": 3,
          "code": "user:create",
          "name": "創建用戶",
          "type": "API",
          "children": []
        }
      ]
    }
  ]
}
```

### 7.2 獲取權限列表

```
GET /api/v1/permissions
```

**權限要求**：`permission:read`

**查詢參數**：

- `keyword`: 搜索關鍵字
- `type`: 權限類型（MENU/BUTTON/API/DATA）
- `status`: 狀態

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "user:read",
      "name": "查看用戶",
      "type": "API",
      "resourceType": "USER",
      "action": "READ",
      "apiPath": "/api/v1/users",
      "apiMethod": "GET",
      "status": 1
    }
  ]
}
```

### 7.3 創建權限

```
POST /api/v1/permissions
```

**權限要求**：`permission:create`

**請求參數**：

```json
{
  "permissionCode": "project:read",
  "permissionName": "查看項目",
  "permissionType": "API",
  "resourceType": "PROJECT",
  "action": "READ",
  "apiPath": "/api/v1/projects",
  "apiMethod": "GET",
  "parentId": 0
}
```

### 7.4 更新權限

```
PUT /api/v1/permissions/{id}
```

**權限要求**：`permission:update`

### 7.5 刪除權限

```
DELETE /api/v1/permissions/{id}
```

**權限要求**：`permission:delete`

## 8. 部門管理API

### 8.1 獲取部門樹

```
GET /api/v1/departments/tree
```

**權限要求**：`dept:read`

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "TECH",
      "name": "技術部",
      "level": 1,
      "userCount": 50,
      "children": [
        {
          "id": 2,
          "code": "TECH_DEV",
          "name": "開發組",
          "level": 2,
          "userCount": 30,
          "children": []
        },
        {
          "id": 3,
          "code": "TECH_TEST",
          "name": "測試組",
          "level": 2,
          "userCount": 20,
          "children": []
        }
      ]
    }
  ]
}
```

### 8.2 創建部門

```
POST /api/v1/departments
```

**權限要求**：`dept:create`

**請求參數**：

```json
{
  "parentId": 1,
  "deptCode": "TECH_OPS",
  "deptName": "運維組",
  "managerId": 10,
  "phone": "010-12345678",
  "email": "ops@example.com"
}
```

### 8.3 更新部門

```
PUT /api/v1/departments/{id}
```

**權限要求**：`dept:update`

### 8.4 刪除部門

```
DELETE /api/v1/departments/{id}
```

**權限要求**：`dept:delete`

## 9. 審計日誌API

### 9.1 獲取審計日誌

```
GET /api/v1/audit-logs
```

**權限要求**：`audit:read`

**查詢參數**：

- `page`: 頁碼
- `pageSize`: 每頁數量
- `userId`: 用戶ID
- `operationType`: 操作類型
- `module`: 操作模塊
- `startTime`: 開始時間
- `endTime`: 結束時間

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1000,
        "userId": 10,
        "username": "john",
        "operationType": "CREATE",
        "operationModule": "USER",
        "operationDesc": "創建用戶: test_user",
        "requestIp": "192.168.1.100",
        "responseStatus": 200,
        "responseTime": 150,
        "createdAt": "2025-11-21T10:00:00Z"
      }
    ],
    "pagination": {
      "current": 1,
      "pageSize": 20,
      "total": 1000,
      "pages": 50
    }
  }
}
```

### 9.2 獲取登入日誌

```
GET /api/v1/login-logs
```

**權限要求**：`audit:read`

**響應**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 100,
        "userId": 10,
        "username": "john",
        "loginTime": "2025-11-21T09:00:00Z",
        "loginIp": "192.168.1.100",
        "loginLocation": "北京市",
        "browser": "Chrome 120",
        "os": "Windows 10",
        "status": 1,
        "loginType": "PASSWORD"
      }
    ],
    "pagination": {
      "current": 1,
      "pageSize": 20,
      "total": 500,
      "pages": 25
    }
  }
}
```

## 10. 錯誤碼定義

| 錯誤碼 | 說明 | HTTP狀態碼 |
|-------|------|-----------|
| 0 | 成功 | 200 |
| 10001 | 參數錯誤 | 400 |
| 10002 | 參數缺失 | 400 |
| 10003 | 參數格式錯誤 | 400 |
| 20001 | 未登入 | 401 |
| 20002 | Token無效 | 401 |
| 20003 | Token過期 | 401 |
| 30001 | 無權限 | 403 |
| 30002 | 角色無權限 | 403 |
| 40001 | 資源不存在 | 404 |
| 40002 | 用戶不存在 | 404 |
| 40003 | 角色不存在 | 404 |
| 50001 | 用戶名已存在 | 409 |
| 50002 | 郵箱已存在 | 409 |
| 50003 | 角色編碼已存在 | 409 |
| 60001 | 租戶不存在 | 404 |
| 60002 | 租戶已禁用 | 403 |
| 60003 | 租戶已過期 | 403 |
| 60004 | 租戶用戶數已達上限 | 403 |
| 99999 | 系統內部錯誤 | 500 |

## 11. API測試示例

### 11.1 完整流程示例

```bash
# 1. 登入獲取Token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "tenantCode": "platform"
  }'

# 響應獲得token
# {"code":0,"data":{"accessToken":"eyJhbGc...","expiresIn":86400}}

# 2. 使用Token查詢用戶列表
curl -X GET "http://localhost:8080/api/v1/users?page=1&pageSize=10" \
  -H "Authorization: Bearer eyJhbGc..."

# 3. 創建用戶
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "Password123!",
    "realName": "新用戶",
    "email": "newuser@example.com",
    "roleIds": [2]
  }'

# 4. 更新用戶
curl -X PUT http://localhost:8080/api/v1/users/100 \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "更新後的姓名",
    "email": "updated@example.com"
  }'

# 5. 登出
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGc..."
```

## 12. API限流策略

### 12.1 限流規則

| 端點類型 | 限流規則 | 說明 |
|---------|---------|------|
| 登入接口 | 5次/分鐘/IP | 防止暴力破解 |
| 查詢接口 | 100次/分鐘/用戶 | 一般查詢 |
| 寫入接口 | 30次/分鐘/用戶 | 創建、更新、刪除 |
| 導出接口 | 10次/小時/用戶 | 數據導出 |

### 12.2 限流響應

```json
{
  "code": 42901,
  "message": "請求過於頻繁，請稍後再試",
  "data": {
    "retryAfter": 60
  }
}
```

HTTP狀態碼：429 Too Many Requests

## 13. API版本管理

### 13.1 版本策略

- 通過URL路徑進行版本控制：`/api/v1/`, `/api/v2/`
- 舊版本API保留至少6個月
- 在響應頭中提供棄用警告：`X-API-Deprecated: true`

### 13.2 版本遷移

```
v1 → v2 遷移示例：

v1: POST /api/v1/users
{
  "username": "john",
  "password": "123456"
}

v2: POST /api/v2/users
{
  "username": "john",
  "password": "123456",
  "passwordStrength": "STRONG",  // 新增字段
  "mfaEnabled": false             // 新增字段
}
```

## 14. Swagger文檔

系統提供Swagger UI在線API文檔：

```
開發環境：http://localhost:8080/swagger-ui.html
生產環境：https://api.example.com/swagger-ui.html
```

## 15. 總結

本API設計文檔提供了多租戶RBAC系統的完整接口規範，包括：

1. **RESTful設計**：遵循REST架構風格
2. **統一規範**：一致的請求響應格式
3. **完整覆蓋**：涵蓋所有核心業務功能
4. **安全設計**：JWT認證、權限控制、限流保護
5. **易於集成**：清晰的文檔和示例

所有API都經過精心設計，確保：

- 易用性
- 安全性
- 可擴展性
- 高性能
