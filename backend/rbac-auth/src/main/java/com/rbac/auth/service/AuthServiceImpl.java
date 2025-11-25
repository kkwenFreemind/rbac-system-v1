package com.rbac.auth.service;

import com.rbac.auth.context.UserContext;
import com.rbac.auth.exception.AccountLockedException;
import com.rbac.auth.exception.AuthenticationException;
import com.rbac.auth.model.dto.LoginRequest;
import com.rbac.auth.model.dto.LoginResponse;
import com.rbac.auth.model.entity.User;
import com.rbac.auth.repository.UserRepository;
import com.rbac.common.redis.util.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 認證服務實作
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final CacheService cacheService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_SECONDS = 900; // 15 分鐘

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();

        // 1. 檢查帳號是否被鎖定
        if (isAccountLocked(username)) {
            Long lockUntil = getLockUntil(username);
            throw new AccountLockedException("帳號已被鎖定", lockUntil);
        }

        // 2. 查詢使用者
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    recordFailedAttempt(username);
                    return new AuthenticationException("帳號或密碼錯誤");
                });

        // 3. 驗證密碼
        if (!userRepository.validatePassword(username, request.getPassword())) {
            recordFailedAttempt(username);
            throw new AuthenticationException("帳號或密碼錯誤");
        }

        // 4. 重置失敗嘗試次數（登入成功）
        resetFailedAttempts(username);

        // 5. 生成 JWT Token
        UserContext userContext = createUserContext(user);
        String token = jwtTokenService.generateToken(userContext);

        // 6. 更新最後登入時間
        userRepository.updateLastLoginTime(username);

        // 7. 記錄登入日誌
        log.info("User logged in successfully: userId={}, username={}, tenantId={}, ip={}",
                user.getUserId(), user.getUsername(), user.getTenantId(), "unknown");

        // 8. 建構回應
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(86400L); // 24 小時
        response.setExpiresAt(LocalDateTime.now().plusSeconds(86400));
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setTenantId(user.getTenantId());
        response.setRoles(user.getRoles());

        return response;
    }

    @Override
    public void logout() {
        // 登出邏輯在 Controller 中實作，因為需要從請求中提取 Token
        log.info("User logged out");
    }

    /**
     * 實作登出邏輯
     *
     * @param token JWT Token
     */
    public void logout(String token) {
        try {
            // 從 Token 提取 jti 和剩餘有效期
            io.jsonwebtoken.Claims claims = jwtTokenService.extractClaims(token);
            String jti = claims.getId();
            long remainingValidity = jwtTokenService.calculateRemainingValidity(token);

            // 將 Token 加入黑名單
            jwtTokenService.addToBlacklist(jti, remainingValidity);

            log.info("User logged out successfully, token blacklisted: {}", jti);
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw new RuntimeException("登出失敗", e);
        }
    }

    @Override
    public boolean validatePassword(String username, String rawPassword) {
        return userRepository.validatePassword(username, rawPassword);
    }

    @Override
    public void recordFailedAttempt(String username) {
        String attemptsKey = "auth:attempts:" + username;
        String lockKey = "auth:lock:" + username;

        // 取得當前失敗次數
        Integer attempts = cacheService.get(attemptsKey, Integer.class);
        if (attempts == null) {
            attempts = 0;
        }

        attempts++;
        cacheService.set(attemptsKey, attempts, 3600); // 1 小時過期

        log.debug("Failed login attempt for user: {}, attempts: {}", username, attempts);

        // 如果達到最大失敗次數，鎖定帳號
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            long lockUntil = System.currentTimeMillis() / 1000 + LOCK_DURATION_SECONDS;
            cacheService.set(lockKey, lockUntil, LOCK_DURATION_SECONDS);
            log.warn("Account locked due to too many failed attempts: {}", username);
        }
    }

    @Override
    public boolean isAccountLocked(String username) {
        String lockKey = "auth:lock:" + username;
        Long lockUntil = cacheService.get(lockKey, Long.class);
        if (lockUntil == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        boolean isLocked = lockUntil > currentTime;

        if (!isLocked) {
            // 鎖定已過期，清除鎖定狀態
            cacheService.delete(lockKey);
            resetFailedAttempts(username);
        }

        return isLocked;
    }

    /**
     * 取得鎖定結束時間
     */
    private Long getLockUntil(String username) {
        String lockKey = "auth:lock:" + username;
        return cacheService.get(lockKey, Long.class);
    }

    /**
     * 重置失敗嘗試次數
     */
    private void resetFailedAttempts(String username) {
        String attemptsKey = "auth:attempts:" + username;
        cacheService.delete(attemptsKey);
    }

    /**
     * 從 User 實體建立 UserContext
     */
    private UserContext createUserContext(User user) {
        UserContext context = new UserContext();
        context.setUserId(user.getUserId());
        context.setUsername(user.getUsername());
        context.setTenantId(user.getTenantId());
        context.setRoles(user.getRoles());
        return context;
    }
}