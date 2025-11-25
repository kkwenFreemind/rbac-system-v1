package com.rbac.auth.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登入請求 DTO
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Data
public class LoginRequest {

    /**
     * 使用者名稱
     */
    @NotBlank(message = "使用者名稱不能為空")
    @Size(min = 3, max = 50, message = "使用者名稱長度必須在 3-50 字元之間")
    private String username;

    /**
     * 密碼
     */
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在 6-100 字元之間")
    private String password;

    /**
     * 驗證碼（可選，用於防止自動化攻擊）
     */
    private String captcha;
}