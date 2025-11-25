package com.rbac.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置類
 *
 * <p>從 application.yml 載入 JWT 相關配置</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Configuration
@ConfigurationProperties(prefix = "rbac.auth.jwt")
@Data
public class JwtConfig {

    /**
     * JWT 簽章密鑰
     * 建議使用 64 字元 hex 字串
     */
    private String secret;

    /**
     * JWT Token 有效期（秒）
     * 預設 86400 秒（24 小時）
     */
    private Long expiration = 86400L;

    /**
     * JWT Token 發行者
     */
    private String issuer = "rbac-auth";

    /**
     * JWT Token 主題
     */
    private String subject = "rbac-user";

    /**
     * JWT Token 演算法
     */
    private String algorithm = "HS256";
}