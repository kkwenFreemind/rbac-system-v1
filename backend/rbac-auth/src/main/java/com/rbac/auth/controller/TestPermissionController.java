package com.rbac.auth.controller;

import com.rbac.common.core.result.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 權限測試控制器
 * 用於測試 @PreAuthorize 註解的功能
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestPermissionController {

    /**
     * 測試管理員權限 - 需要 ROLE_ADMIN 角色
     *
     * @return 成功訊息
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> adminOnlyEndpoint() {
        return Result.success("Admin access granted");
    }

    /**
     * 測試使用者權限 - 需要 ROLE_USER 角色
     *
     * @return 成功訊息
     */
    @GetMapping("/user-only")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Result<String> userOnlyEndpoint() {
        return Result.success("User access granted");
    }

    /**
     * 測試多角色權限 - 需要 ROLE_ADMIN 或 ROLE_USER 角色
     *
     * @return 成功訊息
     */
    @GetMapping("/admin-or-user")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Result<String> adminOrUserEndpoint() {
        return Result.success("Admin or User access granted");
    }

    /**
     * 測試任意角色權限 - 需要至少一個角色
     *
     * @return 成功訊息
     */
    @GetMapping("/any-role")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public Result<String> anyRoleEndpoint() {
        return Result.success("Any role access granted");
    }

    /**
     * 公開端點 - 不需要任何權限
     *
     * @return 成功訊息
     */
    @GetMapping("/public")
    public Result<String> publicEndpoint() {
        return Result.success("Public access granted");
    }
}