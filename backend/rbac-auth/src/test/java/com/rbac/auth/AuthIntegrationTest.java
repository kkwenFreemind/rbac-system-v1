package com.rbac.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbac.auth.config.MockDataConfig;
import com.rbac.auth.model.dto.LoginRequest;
import com.rbac.auth.model.dto.LoginResponse;
import com.rbac.auth.model.dto.UserInfoResponse;
import com.rbac.common.core.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 認證模組整合測試
 *
 * <p>測試完整的認證流程：登入、Token 驗證、獲取用戶信息、登出</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("dev")
class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockDataConfig mockDataConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 測試完整的認證流程
     */
    @Test
    void testCompleteAuthenticationFlow() throws Exception {
        // 1. 登入
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("password123");

        String loginResponseJson = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<LoginResponse> loginResult = objectMapper.readValue(loginResponseJson,
            objectMapper.getTypeFactory().constructParametricType(Result.class, LoginResponse.class));
        String token = loginResult.getData().getToken();

        // 2. 使用 Token 獲取用戶信息
        String userInfoResponseJson = mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.tenantId").exists())
                .andExpect(jsonPath("$.data.roles").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<UserInfoResponse> userInfoResult = objectMapper.readValue(userInfoResponseJson,
            objectMapper.getTypeFactory().constructParametricType(Result.class, UserInfoResponse.class));

        // 驗證用戶信息
        assert userInfoResult.getData().getUsername().equals("admin");
        assert userInfoResult.getData().getTenantId() != null;
        assert userInfoResult.getData().getRoles() != null && !userInfoResult.getData().getRoles().isEmpty();

        // 3. 登出
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 驗證 Token 在登出後失效
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 測試無效憑據的登入
     */
    @Test
    void testInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 測試無 Token 的請求
     */
    @Test
    void testRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 測試無效 Token 的請求
     */
    @Test
    void testRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 測試參數驗證
     */
    @Test
    void testValidationErrors() throws Exception {
        // 測試空用戶名
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        // 測試短密碼
        loginRequest.setUsername("admin");
        loginRequest.setPassword("123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 測試帳號鎖定機制
     */
    @Test
    void testAccountLocking() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("wrongpassword");

        // 多次嘗試錯誤密碼
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // 第6次應該被鎖定
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
}