package com.rbac.auth.service;

import com.rbac.auth.model.dto.LoginRequest;
import com.rbac.auth.model.dto.LoginResponse;

/**
 * 認證服務介面
 *
 * <p>定義認證相關的業務邏輯操作</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
public interface AuthService {

    /**
     * 使用者登入
     *
     * @param request 登入請求
     * @return 登入回應
     */
    LoginResponse login(LoginRequest request);

    /**
     * 使用者登出
     */
    void logout();

    /**
     * 驗證密碼
     *
     * @param username 使用者名稱
     * @param rawPassword 原始密碼
     * @return 如果密碼正確返回 true
     */
    boolean validatePassword(String username, String rawPassword);

    /**
     * 記錄失敗嘗試
     *
     * @param username 使用者名稱
     */
    void recordFailedAttempt(String username);

    /**
     * 檢查帳號是否被鎖定
     *
     * @param username 使用者名稱
     * @return 如果被鎖定返回 true
     */
    boolean isAccountLocked(String username);
}