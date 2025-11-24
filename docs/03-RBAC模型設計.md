# RBAC模型設計文檔

## 1. RBAC模型概述

RBAC（Role-Based Access Control，基於角色的訪問控制）是目前最廣泛使用的權限管理模型。本文檔基於RBAC模型擴展設計，支持多租戶場景。

## 2. RBAC核心概念

### 2.1 五要素模型（RBAC0）

```
用戶(User) ←→ 角色(Role) ←→ 權限(Permission) ←→ 資源(Resource)
                                                    ↓
                                                操作(Action)
```

#### 核心實體關係

```
┌─────────┐      ┌─────────┐      ┌─────────┐      ┌─────────┐
│  User   │─────▶│  Role   │─────▶│Permission│────▶│Resource │
│ (用戶)   │  N:M  │ (角色)   │  N:M  │ (權限)   │  N:M  │ (資源)  │
└─────────┘      └─────────┘      └─────────┘      └─────────┘
     │                                                    │
     │                                                    ▼
     │                                              ┌─────────┐
     │                                              │ Action  │
     │                                              │ (操作)   │
     │                                              └─────────┘
     │
     ▼
┌─────────┐
│ Tenant  │
│ (租戶)   │
└─────────┘
```

### 2.2 實體定義

#### User（用戶）

- 系統的使用者
- 可分配多個角色
- 屬於某個租戶
- 可以是人或系統賬號

#### Role（角色）

- 權限的集合
- 代表一類用戶的職責
- 可分配給多個用戶
- 可包含多個權限

#### Permission（權限）

- 對資源的操作許可
- 格式：`資源:操作`（如 `user:read`）
- 可分配給多個角色
- 精細化控制訪問

#### Resource（資源）

- 系統中的實體對象
- 包括：API、頁面、數據、功能模塊等
- 可設置訪問權限
- 支持層級結構

#### Action（操作）

- 對資源的具體動作
- 常見操作：CREATE、READ、UPDATE、DELETE
- 可自定義操作類型

## 3. 數據模型設計

### 3.1 租戶表（sys_tenant）

```sql
CREATE TABLE sys_tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租戶ID',
    tenant_code VARCHAR(64) NOT NULL UNIQUE COMMENT '租戶編碼',
    tenant_name VARCHAR(128) NOT NULL COMMENT '租戶名稱',
    contact_name VARCHAR(64) COMMENT '聯繫人',
    contact_phone VARCHAR(20) COMMENT '聯繫電話',
    contact_email VARCHAR(100) COMMENT '聯繫郵箱',
    
    -- 套餐配置
    package_type VARCHAR(32) DEFAULT 'BASIC' COMMENT '套餐類型：BASIC/STANDARD/PREMIUM/ENTERPRISE',
    max_users INT DEFAULT 10 COMMENT '最大用戶數',
    max_storage BIGINT DEFAULT 1073741824 COMMENT '最大存儲空間(字節)',
    
    -- 隔離策略
    isolation_level VARCHAR(32) DEFAULT 'ROW' COMMENT '隔離級別：ROW/SCHEMA/DATABASE',
    db_schema VARCHAR(64) COMMENT '數據庫Schema名稱',
    
    -- 狀態管理
    status TINYINT DEFAULT 1 COMMENT '狀態：0-禁用 1-啟用 2-試用 3-過期',
    expire_time DATETIME COMMENT '過期時間',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_by BIGINT COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '刪除標記：0-正常 1-已刪除',
    
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租戶表';
```

### 3.2 用戶表（sys_user）

```sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用戶ID',
    tenant_id BIGINT NOT NULL COMMENT '租戶ID',
    username VARCHAR(64) NOT NULL COMMENT '用戶名',
    password VARCHAR(255) NOT NULL COMMENT '密碼（加密）',
    
    -- 基本信息
    real_name VARCHAR(64) COMMENT '真實姓名',
    nickname VARCHAR(64) COMMENT '昵稱',
    avatar VARCHAR(512) COMMENT '頭像URL',
    email VARCHAR(100) COMMENT '郵箱',
    phone VARCHAR(20) COMMENT '手機號',
    
    -- 用戶類型
    user_type VARCHAR(32) DEFAULT 'NORMAL' COMMENT '用戶類型：SYSTEM/TENANT_ADMIN/NORMAL',
    
    -- 狀態管理
    status TINYINT DEFAULT 1 COMMENT '狀態：0-禁用 1-啟用 2-鎖定',
    last_login_time DATETIME COMMENT '最後登入時間',
    last_login_ip VARCHAR(64) COMMENT '最後登入IP',
    password_update_time DATETIME COMMENT '密碼最後修改時間',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_by BIGINT COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '刪除標記',
    
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用戶表';
```

### 3.3 角色表（sys_role）

```sql
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    tenant_id BIGINT NOT NULL COMMENT '租戶ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色編碼',
    role_name VARCHAR(128) NOT NULL COMMENT '角色名稱',
    description VARCHAR(512) COMMENT '角色描述',
    
    -- 角色類型
    role_type VARCHAR(32) DEFAULT 'CUSTOM' COMMENT '角色類型：SYSTEM/TENANT/CUSTOM',
    
    -- 角色層級（用於角色繼承）
    role_level INT DEFAULT 0 COMMENT '角色層級：0-最高',
    parent_id BIGINT COMMENT '父角色ID',
    
    -- 數據權限範圍
    data_scope VARCHAR(32) DEFAULT 'SELF' COMMENT '數據範圍：ALL/DEPT/DEPT_AND_CHILD/SELF/CUSTOM',
    
    -- 狀態
    status TINYINT DEFAULT 1 COMMENT '狀態：0-禁用 1-啟用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_by BIGINT COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '刪除標記',
    
    UNIQUE KEY uk_tenant_role_code (tenant_id, role_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_role_type (role_type),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 3.4 權限表（sys_permission）

```sql
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '權限ID',
    tenant_id BIGINT COMMENT '租戶ID（NULL表示系統級權限）',
    
    -- 權限標識
    permission_code VARCHAR(128) NOT NULL COMMENT '權限編碼：如 user:read',
    permission_name VARCHAR(128) NOT NULL COMMENT '權限名稱',
    description VARCHAR(512) COMMENT '權限描述',
    
    -- 權限分類
    permission_type VARCHAR(32) DEFAULT 'API' COMMENT '權限類型：MENU/BUTTON/API/DATA',
    category VARCHAR(64) COMMENT '權限分類：用戶管理/角色管理等',
    
    -- 資源信息
    resource_type VARCHAR(32) COMMENT '資源類型：USER/ROLE/DEPT等',
    action VARCHAR(32) COMMENT '操作：CREATE/READ/UPDATE/DELETE/EXPORT等',
    
    -- API資源
    api_path VARCHAR(256) COMMENT 'API路徑',
    api_method VARCHAR(16) COMMENT 'HTTP方法：GET/POST/PUT/DELETE',
    
    -- 菜單資源
    parent_id BIGINT COMMENT '父權限ID（用於菜單樹）',
    menu_url VARCHAR(256) COMMENT '菜單URL',
    menu_icon VARCHAR(128) COMMENT '菜單圖標',
    
    -- 狀態
    status TINYINT DEFAULT 1 COMMENT '狀態：0-禁用 1-啟用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_by BIGINT COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '刪除標記',
    
    UNIQUE KEY uk_permission_code (permission_code, tenant_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_resource_type (resource_type),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='權限表';
```

### 3.5 用戶角色關聯表（sys_user_role）

```sql
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id BIGINT NOT NULL COMMENT '租戶ID',
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    
    -- 有效期（可選）
    effective_time DATETIME COMMENT '生效時間',
    expire_time DATETIME COMMENT '過期時間',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    
    UNIQUE KEY uk_user_role (tenant_id, user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用戶角色關聯表';
```

### 3.6 角色權限關聯表（sys_role_permission）

```sql
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id BIGINT NOT NULL COMMENT '租戶ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '權限ID',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    
    UNIQUE KEY uk_role_permission (tenant_id, role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色權限關聯表';
```

### 3.7 部門表（sys_department）

```sql
CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部門ID',
    tenant_id BIGINT NOT NULL COMMENT '租戶ID',
    
    parent_id BIGINT DEFAULT 0 COMMENT '父部門ID',
    dept_code VARCHAR(64) NOT NULL COMMENT '部門編碼',
    dept_name VARCHAR(128) NOT NULL COMMENT '部門名稱',
    dept_path VARCHAR(512) COMMENT '部門路徑：/1/2/3/',
    
    -- 部門信息
    manager_id BIGINT COMMENT '部門負責人ID',
    phone VARCHAR(20) COMMENT '聯繫電話',
    email VARCHAR(100) COMMENT '郵箱',
    
    -- 層級信息
    level INT DEFAULT 1 COMMENT '部門層級',
    sort_order INT DEFAULT 0 COMMENT '排序',
    
    -- 狀態
    status TINYINT DEFAULT 1 COMMENT '狀態：0-禁用 1-啟用',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_by BIGINT COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted TINYINT DEFAULT 0 COMMENT '刪除標記',
    
    UNIQUE KEY uk_tenant_dept_code (tenant_id, dept_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_dept_path (dept_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部門表';
```

### 3.8 用戶部門關聯表（sys_user_department）

```sql
CREATE TABLE sys_user_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id BIGINT NOT NULL COMMENT '租戶ID',
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    department_id BIGINT NOT NULL COMMENT '部門ID',
    
    is_primary TINYINT DEFAULT 0 COMMENT '是否主部門：0-否 1-是',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    
    UNIQUE KEY uk_user_dept (tenant_id, user_id, department_id),
    INDEX idx_user_id (user_id),
    INDEX idx_department_id (department_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用戶部門關聯表';
```

## 4. 擴展RBAC模型

### 4.1 數據權限模型

#### 數據範圍類型

```java
public enum DataScope {
    ALL,              // 全部數據權限
    TENANT,           // 本租戶數據權限
    DEPT,             // 本部門數據權限
    DEPT_AND_CHILD,   // 本部門及子部門數據權限
    SELF,             // 僅本人數據權限
    CUSTOM            // 自定義數據權限
}
```

#### 自定義數據權限表（sys_role_data_scope）

```sql
CREATE TABLE sys_role_data_scope (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id BIGINT NOT NULL COMMENT '租戶ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    
    -- 自定義範圍
    scope_type VARCHAR(32) NOT NULL COMMENT '範圍類型：DEPARTMENT/USER',
    scope_value BIGINT NOT NULL COMMENT '範圍值：部門ID/用戶ID',
    
    -- 審計字段
    created_by BIGINT COMMENT '創建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    
    INDEX idx_role_id (role_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色數據權限範圍表';
```

### 4.2 角色繼承模型

支持角色繼承，子角色自動繼承父角色的所有權限。

```
超級管理員 (SUPER_ADMIN)
    │
    ├─→ 管理員 (ADMIN)
    │       │
    │       ├─→ 部門管理員 (DEPT_ADMIN)
    │       └─→ 審計員 (AUDITOR)
    │
    └─→ 普通用戶 (USER)
            │
            └─→ 訪客 (GUEST)
```

### 4.3 動態權限模型

支持運行時動態調整權限，無需重新登錄。

#### 權限變更事件

```java
// 偽代碼示例
public class PermissionChangeEvent {
    private Long userId;
    private Long tenantId;
    private ChangeType changeType; // ADD/REMOVE/UPDATE
    private List<String> permissions;
}
```

#### 權限緩存策略

```
用戶登入
   │
   ├─→ 加載用戶權限
   │   查詢：用戶 → 角色 → 權限
   │
   ├─→ 緩存權限列表
   │   Redis Key: permission:tenant123:user456
   │   TTL: 30分鐘
   │
   ├─→ 權限變更時
   │   - 發布權限變更事件
   │   - 清除相關用戶緩存
   │   - 用戶下次請求重新加載
   │
   └─→ 權限校驗
       - 優先從緩存讀取
       - 緩存未命中則查庫
```

## 5. 權限校驗策略

### 5.1 基於註解的權限校驗

```java
// 偽代碼示例
@RequiresPermissions("user:read")
public List<User> listUsers() {
    // 業務邏輯
}

@RequiresRoles("ADMIN")
public void deleteUser(Long userId) {
    // 業務邏輯
}

@RequiresDataScope(DataScope.DEPT)
public List<User> listDepartmentUsers() {
    // 自動過濾部門數據
}
```

### 5.2 基於AOP的攔截器

```
HTTP請求
   │
   ├─→ 認證過濾器
   │   驗證Token，提取用戶信息
   │
   ├─→ 租戶過濾器
   │   設置租戶上下文
   │
   ├─→ 權限攔截器
   │   ┌─────────────────────┐
   │   │ 1. 獲取目標方法     │
   │   │ 2. 解析權限註解     │
   │   │ 3. 加載用戶權限     │
   │   │ 4. 權限匹配校驗     │
   │   │ 5. 通過/拒絕        │
   │   └─────────────────────┘
   │
   ├─→ 數據權限攔截器
   │   動態注入數據範圍SQL
   │
   └─→ 業務邏輯執行
```

### 5.3 URL權限映射

```java
// 權限與URL映射配置
public class PermissionUrlMapping {
    Map<String, List<String>> mapping = {
        "user:read": ["/api/users", "/api/users/{id}"],
        "user:create": ["/api/users"],
        "user:update": ["/api/users/{id}"],
        "user:delete": ["/api/users/{id}"]
    };
}
```

### 5.4 權限表達式

支持複雜的權限表達式：

```java
// AND邏輯
@RequiresPermissions(value = {"user:read", "user:export"}, logical = AND)

// OR邏輯
@RequiresPermissions(value = {"user:admin", "user:manager"}, logical = OR)

// 動態權限
@RequiresPermissions("#user.department.id == #currentUser.department.id")
```

### 5.5 數據範圍（Data Scope）實作細節

數據範圍是RBAC模型中實現數據級權限控制的核心機制，以下說明如何將`sys_role_data_scope`表中的配置轉化為實際的SQL過濾條件。

#### 數據範圍類型映射

| 數據範圍類型 | scope_value | SQL過濾邏輯 | 適用場景 |
|------------|-------------|------------|----------|
| ALL | null | 無過濾條件 | 超級管理員查看所有數據 |
| TENANT | {tenantId} | `tenant_id = ?` | 租戶管理員查看本租戶所有數據 |
| DEPT | {deptId} | `dept_id = ?` | 部門管理員查看本部門數據 |
| DEPT_AND_CHILD | {deptId} | `dept_id IN (SELECT id FROM sys_dept WHERE path LIKE 'deptId%')` | 查看本部門及下級部門 |
| SELF | {userId} | `created_by = ? OR user_id = ?` | 只查看自己創建的數據 |
| CUSTOM | {customRule} | 自定義SQL片段 | 複雜業務場景 |

#### 實現方式一：MyBatis攔截器（推薦）

```java
// 偽代碼示例
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class DataScopeInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 獲取當前用戶的數據範圍配置
        User currentUser = SecurityContext.getCurrentUser();
        List<DataScopeConfig> scopes = dataService.getUserDataScopes(currentUser.getId());
        
        // 2. 解析SQL
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
        String originalSql = boundSql.getSql();
        
        // 3. 根據數據範圍類型構建過濾條件
        String dataScopeFilter = buildDataScopeFilter(scopes, currentUser);
        
        // 4. 注入過濾條件到SQL
        String newSql = injectDataScopeFilter(originalSql, dataScopeFilter);
        
        // 5. 執行修改後的SQL
        return executeWithNewSql(invocation, newSql);
    }
    
    private String buildDataScopeFilter(List<DataScopeConfig> scopes, User user) {
        if (scopes.isEmpty()) {
            return ""; // 無數據權限
        }
        
        List<String> conditions = new ArrayList<>();
        
        for (DataScopeConfig scope : scopes) {
            switch (scope.getScopeType()) {
                case "ALL":
                    return ""; // 查看全部，無需過濾
                    
                case "TENANT":
                    conditions.add("tenant_id = '" + user.getTenantId() + "'");
                    break;
                    
                case "DEPT":
                    conditions.add("dept_id = " + scope.getScopeValue());
                    break;
                    
                case "DEPT_AND_CHILD":
                    List<Long> deptIds = deptService.getChildDeptIds(Long.parseLong(scope.getScopeValue()));
                    conditions.add("dept_id IN (" + StringUtils.join(deptIds, ",") + ")");
                    break;
                    
                case "SELF":
                    conditions.add("(created_by = " + user.getId() + " OR user_id = " + user.getId() + ")");
                    break;
                    
                case "CUSTOM":
                    // 解析並驗證自定義規則（需防止SQL注入）
                    conditions.add(parseSafeCustomRule(scope.getScopeValue()));
                    break;
            }
        }
        
        // 多個條件用OR連接（用戶擁有多個數據範圍）
        return conditions.isEmpty() ? "1=0" : "(" + String.join(" OR ", conditions) + ")";
    }
    
    private String injectDataScopeFilter(String originalSql, String filter) {
        if (filter.isEmpty()) {
            return originalSql;
        }
        
        // 解析SQL，找到WHERE子句插入位置
        // 簡化示例：實際需要使用SQL解析器
        if (originalSql.toUpperCase().contains("WHERE")) {
            return originalSql.replaceFirst("(?i)WHERE", "WHERE " + filter + " AND ");
        } else {
            // 在FROM子句後插入WHERE
            return originalSql.replaceFirst("(?i)FROM\\s+\\w+", "$0 WHERE " + filter);
        }
    }
}
```

#### 實現方式二：Repository層手動注入

```java
// 偽代碼示例
@Repository
public class UserRepository {
    
    @Autowired
    private DataScopeService dataScopeService;
    
    public List<User> findUsers(UserQuery query) {
        // 1. 獲取當前用戶的數據範圍
        String dataScopeFilter = dataScopeService.buildFilter();
        
        // 2. 構建查詢條件
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", TenantContext.getTenantId());
        
        // 3. 添加數據範圍過濾
        if (!dataScopeFilter.isEmpty()) {
            wrapper.apply(dataScopeFilter);
        }
        
        // 4. 執行查詢
        return userMapper.selectList(wrapper);
    }
}
```

#### 實現方式三：AOP切面

```java
// 偽代碼示例
@Aspect
@Component
public class DataScopeAspect {
    
    @Around("@annotation(dataScope)")
    public Object applyDataScope(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        try {
            // 1. 獲取數據範圍配置
            User currentUser = SecurityContext.getCurrentUser();
            String filter = buildDataScopeFilter(currentUser, dataScope.value());
            
            // 2. 設置到ThreadLocal供SQL攔截器使用
            DataScopeContext.setFilter(filter);
            
            // 3. 執行業務方法
            return joinPoint.proceed();
            
        } finally {
            // 4. 清理上下文
            DataScopeContext.clear();
        }
    }
}

// 使用示例
@Service
public class UserService {
    
    @DataScope("user")  // 指定數據範圍應用的資源類型
    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
```

#### 性能優化建議

1. **緩存數據範圍配置**：

   ```java
   // Redis緩存Key：data_scope:{userId}
   String cacheKey = "data_scope:" + userId;
   List<DataScopeConfig> scopes = redisTemplate.opsForValue().get(cacheKey);
   if (scopes == null) {
       scopes = loadFromDatabase(userId);
       redisTemplate.opsForValue().set(cacheKey, scopes, 30, TimeUnit.MINUTES);
   }
   ```

2. **部門層級緩存**：

   ```java
   // 緩存部門樹結構，避免每次查詢都遞歸
   String deptTreeKey = "dept_tree:" + tenantId;
   ```

3. **SQL預編譯**：
   - 對於固定模式的數據範圍（如DEPT、SELF），使用PreparedStatement
   - 避免動態拼接SQL，防止SQL注入

4. **索引優化**：

   ```sql
   -- 確保數據範圍過濾字段建立索引
   CREATE INDEX idx_dept_id ON sys_user(tenant_id, dept_id);
   CREATE INDEX idx_created_by ON sys_user(tenant_id, created_by);
   ```

#### 測試建議

1. **單元測試**：測試各數據範圍類型的SQL生成邏輯
2. **集成測試**：驗證不同角色查詢結果的正確性
3. **性能測試**：對比有無數據範圍過濾的查詢性能
4. **安全測試**：嘗試繞過數據範圍限制，驗證防護有效性

## 6. 預置角色與權限

### 6.1 系統預置角色

#### 平台級角色（跨租戶）

| 角色代碼 | 角色名稱 | 描述 |
|---------|---------|------|
| PLATFORM_ADMIN | 平台超級管理員 | 管理所有租戶 |
| PLATFORM_OPERATOR | 平台運營人員 | 查看租戶數據 |
| PLATFORM_AUDITOR | 平台審計員 | 查看審計日誌 |

#### 租戶級角色

| 角色代碼 | 角色名稱 | 描述 |
|---------|---------|------|
| TENANT_ADMIN | 租戶管理員 | 管理租戶所有資源 |
| DEPT_ADMIN | 部門管理員 | 管理本部門資源 |
| USER_MANAGER | 用戶管理員 | 管理用戶賬號 |
| ROLE_MANAGER | 角色管理員 | 管理角色權限 |
| AUDITOR | 審計員 | 查看審計日誌 |
| NORMAL_USER | 普通用戶 | 基本操作權限 |
| GUEST | 訪客 | 只讀權限 |

### 6.2 系統預置權限

#### 用戶管理權限

```
user:create    - 創建用戶
user:read      - 查看用戶
user:update    - 更新用戶
user:delete    - 刪除用戶
user:export    - 導出用戶
user:import    - 導入用戶
user:reset-pwd - 重置密碼
```

#### 角色管理權限

```
role:create    - 創建角色
role:read      - 查看角色
role:update    - 更新角色
role:delete    - 刪除角色
role:assign    - 分配角色
```

#### 權限管理權限

```
permission:create - 創建權限
permission:read   - 查看權限
permission:update - 更新權限
permission:delete - 刪除權限
permission:assign - 分配權限
```

#### 部門管理權限

```
dept:create    - 創建部門
dept:read      - 查看部門
dept:update    - 更新部門
dept:delete    - 刪除部門
```

## 7. 特殊場景處理

### 7.1 臨時授權

支持臨時授予某個用戶特定權限，到期自動回收。

```sql
CREATE TABLE sys_user_permission_temp (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    
    effective_time DATETIME NOT NULL COMMENT '生效時間',
    expire_time DATETIME NOT NULL COMMENT '過期時間',
    reason VARCHAR(512) COMMENT '授權原因',
    
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用戶臨時權限表';
```

### 7.2 權限委派

用戶可以將自己的部分權限臨時委派給其他用戶。

```sql
CREATE TABLE sys_permission_delegation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    
    from_user_id BIGINT NOT NULL COMMENT '委派人',
    to_user_id BIGINT NOT NULL COMMENT '被委派人',
    permission_id BIGINT NOT NULL COMMENT '權限ID',
    
    effective_time DATETIME NOT NULL,
    expire_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1 COMMENT '狀態：0-已撤銷 1-生效中',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='權限委派表';
```

### 7.3 權限申請審批

支持用戶申請權限，需管理員審批。

```sql
CREATE TABLE sys_permission_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    
    applicant_id BIGINT NOT NULL COMMENT '申請人',
    apply_type VARCHAR(32) NOT NULL COMMENT '申請類型：ROLE/PERMISSION',
    apply_target_id BIGINT NOT NULL COMMENT '申請目標ID',
    apply_reason VARCHAR(512) COMMENT '申請原因',
    
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '狀態：PENDING/APPROVED/REJECTED',
    approver_id BIGINT COMMENT '審批人',
    approve_time DATETIME COMMENT '審批時間',
    approve_comment VARCHAR(512) COMMENT '審批意見',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='權限申請表';
```

## 8. 性能優化建議

### 8.1 權限緩存策略

```
三級緩存架構：

Level 1: 應用本地緩存（Caffeine）
- 緩存用戶權限列表
- TTL: 5分鐘
- 大小限制: 10000條

Level 2: 分布式緩存（Redis）
- 緩存用戶權限、角色信息
- TTL: 30分鐘
- 權限變更時主動失效

Level 3: 數據庫
- 持久化存儲
- 緩存未命中時查詢
```

### 8.2 權限查詢優化

```sql
-- 優化前：多次查詢
SELECT * FROM sys_user WHERE id = ?;
SELECT * FROM sys_user_role WHERE user_id = ?;
SELECT * FROM sys_role WHERE id IN (?);
SELECT * FROM sys_role_permission WHERE role_id IN (?);
SELECT * FROM sys_permission WHERE id IN (?);

-- 優化後：一次查詢獲取所有權限
SELECT DISTINCT p.*
FROM sys_user u
INNER JOIN sys_user_role ur ON u.id = ur.user_id
INNER JOIN sys_role r ON ur.role_id = r.id
INNER JOIN sys_role_permission rp ON r.id = rp.role_id
INNER JOIN sys_permission p ON rp.permission_id = p.id
WHERE u.id = ? 
  AND u.tenant_id = ?
  AND u.status = 1
  AND r.status = 1
  AND p.status = 1;
```

### 8.3 權限樹預加載

對於菜單權限，預先構建權限樹並緩存：

```java
// 偽代碼
public class PermissionTree {
    private Long id;
    private String code;
    private String name;
    private List<PermissionTree> children;
}

// 租戶啟動時預加載
Map<Long, PermissionTree> tenantPermissionTreeCache;
```

## 9. 安全建議

### 9.1 最小權限原則

- 默認拒絕所有訪問
- 僅授予必要權限
- 定期審計權限使用情況
- 及時回收不用的權限

### 9.2 權限分離

- 管理權限與業務權限分離
- 讀權限與寫權限分離
- 敏感操作需要二次確認
- 關鍵操作需要審批流程

### 9.3 權限審計

- 記錄所有權限變更
- 記錄權限校驗失敗
- 定期生成權限報告
- 異常權限訪問告警

## 10. 總結

本RBAC模型設計具有以下特點：

1. **多租戶支持**：完整的租戶隔離機制
2. **靈活擴展**：支持角色繼承、數據權限、動態權限
3. **高性能**：多級緩存、查詢優化
4. **安全性**：最小權限原則、審計追蹤
5. **易維護**：清晰的數據模型、完善的文檔

適用於中大型SaaS系統的權限管理需求。
