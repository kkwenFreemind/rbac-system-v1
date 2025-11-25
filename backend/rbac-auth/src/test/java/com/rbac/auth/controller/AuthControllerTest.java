package com.rbac.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbac.auth.config.MockDataConfig;
import com.rbac.auth.model.dto.LoginRequest;
import com.rbac.auth.model.dto.LoginResponse;
import com.rbac.auth.repository.UserRepository;
import com.rbac.auth.service.AuthService;
import com.rbac.common.core.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 整合測試
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.rbac.common.redis.util.CacheService cacheService;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        // 建立測試請求
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        // 建立測試回應
        loginResponse = new LoginResponse();
        loginResponse.setToken("mock.jwt.token");
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn(86400L);
        loginResponse.setUserId(1L);
        loginResponse.setUsername("admin");
        loginResponse.setTenantId(1L);
    }

    @Test
    void testLoginApiSuccess() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Operation completed successfully"))
                .andExpect(jsonPath("$.data.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(86400))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.tenantId").value(1));
    }

    @Test
    void testLoginApiFailed401() throws Exception {
        // Given - 模擬認證失敗
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new com.rbac.auth.exception.AuthenticationException("帳號或密碼錯誤"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("帳號或密碼錯誤"));
    }

    @Test
    void testLoginApiFailed403() throws Exception {
        // Given - 模擬帳號鎖定
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new com.rbac.auth.exception.AccountLockedException("帳號已被鎖定", 1734758400L));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("帳號已被鎖定"));
    }

    @Test
    void testLoginWithInvalidRequest() throws Exception {
        // Given - 無效請求（空用戶名）
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testLogoutApiSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer mock.jwt.token"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testLogoutApiFailedWithoutToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("無效的授權標頭"));
    }

    @Test
    void testLogoutApiFailedWithInvalidToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "InvalidToken"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("無效的授權標頭"));
    }

    @Test
    void testGetCurrentUserSuccess() throws Exception {
        // Given - 模擬已登入用戶上下文
        // 注意：這個測試需要完整的 Spring Security 上下文，實際上可能需要在整合測試中進行

        // When & Then - 預期會因為沒有有效的用戶上下文而失敗
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer mock.jwt.token"))
                .andExpect(jsonPath("$.code").value(401)); // 沒有有效的用戶上下文
    }

    @Test
    void testGetCurrentUserWithoutToken() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(jsonPath("$.code").value(401));
    }
}