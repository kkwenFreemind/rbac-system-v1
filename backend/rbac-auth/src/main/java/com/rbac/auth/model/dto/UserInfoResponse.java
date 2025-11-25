package com.rbac.auth.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用者資訊回應 DTO
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Data
public class UserInfoResponse {

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
     * 電子郵件
     */
    private String email;

    /**
     * 最後登入時間
     */
    private LocalDateTime lastLoginAt;
}