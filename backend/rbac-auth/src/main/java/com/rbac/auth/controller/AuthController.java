package com.rbac.auth.controller;

import com.rbac.auth.model.dto.LoginRequest;
import com.rbac.auth.model.dto.LoginResponse;
import com.rbac.auth.model.dto.UserInfoResponse;
import com.rbac.auth.model.entity.User;
import com.rbac.auth.repository.UserRepository;
import com.rbac.auth.service.AuthService;
import com.rbac.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 認證控制器
 *
 * <p>處理認證相關的 HTTP 請求</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 使用者登入
     *
     * @param request 登入請求
     * @return 登入回應
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        LoginResponse response = authService.login(request);

        log.info("Login successful for username: {}", request.getUsername());
        return Result.success(response);
    }

    /**
     * 使用者登出
     *
     * @param authorization Authorization header
     * @return 登出結果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(401, "無效的授權標頭");
        }

        String token = authorization.substring(7); // Remove "Bearer " prefix

        // 實作登出邏輯
        ((com.rbac.auth.service.AuthServiceImpl) authService).logout(token);

        log.info("User logged out successfully");
        return Result.success(null);
    }

    /**
     * 取得當前使用者資訊
     *
     * @return 使用者資訊
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        com.rbac.auth.context.UserContext userContext = com.rbac.auth.context.UserContextHolder.getContext();

        if (userContext == null) {
            return Result.error(401, "未認證的使用者");
        }

        // 從 UserRepository 取得完整的用戶資訊
        User user = userRepository.findByUsername(userContext.getUsername())
                .orElseThrow(() -> new RuntimeException("使用者不存在"));

        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setTenantId(user.getTenantId());
        response.setRoles(user.getRoles());
        response.setEmail(user.getEmail());
        response.setLastLoginAt(user.getLastLoginAt());

        return Result.success(response);
    }
}