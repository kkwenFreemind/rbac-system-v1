package com.rbac.auth.repository;

import com.rbac.auth.model.entity.User;

import java.util.Optional;

/**
 * 使用者倉儲介面
 *
 * <p>定義使用者資料存取操作，採用依賴倒置設計</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
public interface UserRepository {

    /**
     * 根據使用者名稱查詢使用者
     *
     * @param username 使用者名稱
     * @return 使用者 Optional，如果不存在返回 empty
     */
    Optional<User> findByUsername(String username);

    /**
     * 驗證密碼
     *
     * @param username 使用者名稱
     * @param rawPassword 原始密碼
     * @return 如果密碼正確返回 true
     */
    boolean validatePassword(String username, String rawPassword);

    /**
     * 更新最後登入時間
     *
     * @param username 使用者名稱
     */
    void updateLastLoginTime(String username);

    /**
     * 檢查使用者是否存在
     *
     * @param username 使用者名稱
     * @return 如果存在返回 true
     */
    boolean existsByUsername(String username);

    /**
     * 根據使用者名稱取得使用者狀態
     *
     * @param username 使用者名稱
     * @return 使用者狀態，如果不存在返回 null
     */
    com.rbac.auth.model.entity.UserStatus getUserStatus(String username);
}