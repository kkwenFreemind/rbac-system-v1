package com.rbac.auth.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 使用者上下文
 *
 * <p>ThreadLocal 管理的當前使用者資訊，供跨模組使用</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    /**
     * 使用者 ID
     */
    private Long userId;

    /**
     * 使用者名稱
     */
    private String username;

    /**
     * 租戶 ID
     */
    private Long tenantId;

    /**
     * 角色清單
     */
    private List<String> roles;

    /**
     * JWT Token ID (jti)
     */
    private String jti;

    /**
     * 檢查是否具有指定角色
     *
     * @param role 角色名稱
     * @return 如果具有該角色返回 true
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 檢查是否具有任一指定角色
     *
     * @param roles 角色名稱陣列
     * @return 如果具有任一角色返回 true
     */
    public boolean hasAnyRole(String... roles) {
        if (roles == null || this.roles == null) {
            return false;
        }
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 檢查是否具有所有指定角色
     *
     * @param roles 角色名稱陣列
     * @return 如果具有所有角色返回 true
     */
    public boolean hasAllRoles(String... roles) {
        if (roles == null || this.roles == null) {
            return false;
        }
        for (String role : roles) {
            if (!this.roles.contains(role)) {
                return false;
            }
        }
        return true;
    }
}