package com.rbac.auth.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登入回應 DTO
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Data
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token 類型
     */
    private String tokenType = "Bearer";

    /**
     * Token 有效期（秒）
     */
    private Long expiresIn;

    /**
     * Token 到期時間
     */
    private LocalDateTime expiresAt;

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
}