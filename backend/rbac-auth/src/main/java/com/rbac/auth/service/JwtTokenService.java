package com.rbac.auth.service;

import com.rbac.auth.context.UserContext;

/**
 * JWT Token 服務介面
 *
 * <p>定義 JWT Token 的生成、驗證、解析等操作</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
public interface JwtTokenService {

    /**
     * 生成 JWT Token
     *
     * @param userContext 使用者上下文
     * @return JWT Token 字串
     */
    String generateToken(UserContext userContext);

    /**
     * 驗證 JWT Token
     *
     * @param token JWT Token
     * @return 如果有效返回 true
     */
    boolean validateToken(String token);

    /**
     * 從 Token 提取 Claims
     *
     * @param token JWT Token
     * @return Claims 物件
     */
    io.jsonwebtoken.Claims extractClaims(String token);

    /**
     * 從 Token 提取 UserContext
     *
     * @param token JWT Token
     * @return UserContext 物件
     */
    UserContext extractUserContext(String token);

    /**
     * 檢查 Token 是否在黑名單中
     *
     * @param jti Token ID
     * @return 如果在黑名單中返回 true
     */
    boolean isTokenBlacklisted(String jti);

    /**
     * 將 Token 加入黑名單
     *
     * @param jti Token ID
     * @param ttlSeconds TTL 秒數
     */
    void addToBlacklist(String jti, long ttlSeconds);

    /**
     * 計算 Token 剩餘有效期
     *
     * @param token JWT Token
     * @return 剩餘有效期（秒）
     */
    long calculateRemainingValidity(String token);
}