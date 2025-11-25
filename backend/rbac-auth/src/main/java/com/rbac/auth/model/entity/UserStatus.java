package com.rbac.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 使用者狀態列舉
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE("啟用"),
    LOCKED("鎖定"),
    DISABLED("停用");

    private final String description;
}