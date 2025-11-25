package com.rbac.auth.service;

import com.rbac.auth.context.UserContext;
import com.rbac.auth.exception.AccountLockedException;
import com.rbac.auth.exception.AuthenticationException;
import com.rbac.auth.model.dto.LoginRequest;
import com.rbac.auth.model.dto.LoginResponse;
import com.rbac.auth.model.entity.User;
import com.rbac.auth.model.entity.UserStatus;
import com.rbac.auth.repository.UserRepository;
import com.rbac.common.redis.util.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 單元測試
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 建立測試使用者
        testUser = User.builder()
                .userId(1L)
                .username("admin")
                .passwordHash("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EH") // admin123 的 BCrypt hash
                .tenantId(1L)
                .roles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
                .status(UserStatus.ACTIVE)
                .email("admin@example.com")
                .createdAt(LocalDateTime.now())
                .lastLoginAt(null)
                .build();

        // 建立登入請求
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");
    }

    @Test
    void testLoginSuccess() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(userRepository.validatePassword("admin", "admin123")).thenReturn(true);
        when(jwtTokenService.generateToken(any(UserContext.class))).thenReturn("mock.jwt.token");

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400L, response.getExpiresIn());
        assertNotNull(response.getExpiresAt());
        assertEquals(1L, response.getUserId());
        assertEquals("admin", response.getUsername());
        assertEquals(1L, response.getTenantId());
        assertEquals(Arrays.asList("ROLE_ADMIN", "ROLE_USER"), response.getRoles());

        // Verify 方法調用
        verify(userRepository).findByUsername("admin");
        verify(userRepository).validatePassword("admin", "admin123");
        verify(jwtTokenService).generateToken(any(UserContext.class));
        verify(userRepository).updateLastLoginTime("admin");
        verify(cacheService).delete("auth:attempts:admin"); // 重置失敗嘗試次數
    }

    @Test
    void testLoginFailedWithWrongPassword() {
        // Given
        loginRequest.setPassword("wrongpassword"); // 修改密碼為錯誤的
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(userRepository.validatePassword("admin", "wrongpassword")).thenReturn(false);
        when(cacheService.get("auth:attempts:admin", Integer.class)).thenReturn(null); // 第一次失敗
        when(cacheService.get("auth:lock:admin", Long.class)).thenReturn(null); // 帳號未鎖定

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("帳號或密碼錯誤", exception.getMessage());

        // Verify 方法調用
        verify(userRepository).findByUsername("admin");
        verify(userRepository).validatePassword("admin", "wrongpassword");
        verify(cacheService).set("auth:attempts:admin", 1, 3600); // 記錄失敗嘗試
        verify(jwtTokenService, never()).generateToken(any(UserContext.class));
        verify(userRepository, never()).updateLastLoginTime(anyString());
    }

    @Test
    void testAccountLockedAfter5Failures() {
        // Given - 模擬第5次失敗嘗試
        loginRequest.setPassword("wrongpassword");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(userRepository.validatePassword("admin", "wrongpassword")).thenReturn(false);
        when(cacheService.get("auth:attempts:admin", Integer.class)).thenReturn(4); // 第4次失敗後，現在是第5次
        when(cacheService.get("auth:lock:admin", Long.class)).thenReturn(null); // 帳號未鎖定

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("帳號或密碼錯誤", exception.getMessage());

        // Verify 帳號被鎖定
        verify(cacheService).set(eq("auth:attempts:admin"), eq(5), eq(3600L));
        verify(cacheService).set(eq("auth:lock:admin"), anyLong(), eq(900L)); // 15分鐘鎖定
    }

    @Test
    void testLoginWithLockedAccount() {
        // Given - 帳號已被鎖定
        when(cacheService.get("auth:lock:admin", Long.class)).thenReturn(System.currentTimeMillis() / 1000 + 1000L);

        // When & Then
        AccountLockedException exception = assertThrows(AccountLockedException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("帳號已被鎖定", exception.getMessage());
        assertNotNull(exception.getLockUntil());

        // Verify 不會執行後續驗證
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).validatePassword(anyString(), anyString());
    }

    @Test
    void testLoginWithNonExistentUser() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(cacheService.get("auth:attempts:nonexistent", Integer.class)).thenReturn(null);
        when(cacheService.get("auth:lock:nonexistent", Long.class)).thenReturn(null); // 帳號未鎖定

        loginRequest.setUsername("nonexistent");

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("帳號或密碼錯誤", exception.getMessage());

        // Verify 記錄失敗嘗試
        verify(cacheService).set("auth:attempts:nonexistent", 1, 3600);
    }

    @Test
    @DisplayName("登出成功 - Token 加入黑名單")
    void testLogoutSuccess() {
        // Given
        String token = "valid.jwt.token";
        String jti = "test-jti-123";
        long expectedTtlSeconds = 3600L; // 1 hour in seconds

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn(jti);

        when(jwtTokenService.extractClaims(token)).thenReturn(claims);
        when(jwtTokenService.calculateRemainingValidity(token)).thenReturn(expectedTtlSeconds);

        // When
        authService.logout(token);

        // Then
        verify(jwtTokenService).addToBlacklist(jti, expectedTtlSeconds);
    }
}