package com.rbac.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用者實體
 *
 * <p>初版用於 MockUserRepository，後續可擴展為 JPA Entity</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 使用者 ID（唯一標識）
     */
    private Long userId;

    /**
     * 使用者名稱（登入帳號，唯一）
     */
    private String username;

    /**
     * BCrypt 雜湊後的密碼
     * <p>格式：$2a$10$[53 字元 BCrypt Hash]</p>
     */
    private String passwordHash;

    /**
     * 租戶 ID（初版固定為 1，待 Tenant Module 整合後動態設定）
     */
    private Long tenantId;

    /**
     * 使用者角色列表（初版固定為 ["ROLE_USER"]，待 Role Module 整合後動態載入）
     */
    private List<String> roles;

    /**
     * 帳號狀態
     * <ul>
     *   <li>ACTIVE：啟用中</li>
     *   <li>LOCKED：已鎖定（5 次密碼錯誤）</li>
     *   <li>DISABLED：已停用</li>
     * </ul>
     */
    private UserStatus status;

    /**
     * 電子郵件（選擇性，用於密碼重設）
     */
    private String email;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 最後登入時間
     */
    private LocalDateTime lastLoginAt;
}