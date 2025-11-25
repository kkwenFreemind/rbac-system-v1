package com.rbac.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Mock 資料配置類
 *
 * <p>從 application.yml 載入測試使用者資料</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Configuration
@ConfigurationProperties(prefix = "rbac.auth")
@Data
public class MockDataConfig {

    /**
     * Mock 使用者清單
     */
    private List<MockUser> mockUsers;

    /**
     * Mock 使用者資料
     */
    @Data
    public static class MockUser {
        /**
         * 使用者名稱
         */
        private String username;

        /**
         * BCrypt 加密後的密碼
         */
        private String passwordHash;

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
         * 帳號狀態
         */
        private String status;
    }
}