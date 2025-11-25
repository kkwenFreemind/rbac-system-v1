package com.rbac.auth.repository;

import com.rbac.auth.config.MockDataConfig;
import com.rbac.auth.model.entity.User;
import com.rbac.auth.model.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock 使用者倉儲實作
 *
 * <p>開發環境使用的記憶體實作，從 MockDataConfig 載入測試資料</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Slf4j
@Repository
@Profile("dev")
@RequiredArgsConstructor
public class MockUserRepository implements UserRepository {

    private final MockDataConfig mockDataConfig;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 記憶體儲存的使用者資料
     */
    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    /**
     * 初始化方法，從配置載入測試使用者
     */
    @PostConstruct
    public void init() {
        if (mockDataConfig.getMockUsers() != null) {
            for (MockDataConfig.MockUser mockUser : mockDataConfig.getMockUsers()) {
                User user = new User();
                user.setUserId(System.currentTimeMillis() + userStore.size()); // 簡單的 ID 生成
                user.setUsername(mockUser.getUsername());
                user.setPasswordHash(mockUser.getPasswordHash());
                user.setTenantId(mockUser.getTenantId());
                user.setRoles(mockUser.getRoles());
                user.setEmail(mockUser.getEmail());
                user.setStatus(UserStatus.valueOf(mockUser.getStatus()));
                user.setCreatedAt(LocalDateTime.now());
                user.setLastLoginAt(null);

                userStore.put(user.getUsername(), user);
                log.info("Loaded mock user: {}", user.getUsername());
            }
        }
        log.info("MockUserRepository initialized with {} users", userStore.size());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userStore.get(username));
    }

    @Override
    public boolean validatePassword(String username, String rawPassword) {
        User user = userStore.get(username);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    @Override
    public void updateLastLoginTime(String username) {
        User user = userStore.get(username);
        if (user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            log.debug("Updated last login time for user: {}", username);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userStore.containsKey(username);
    }

    @Override
    public UserStatus getUserStatus(String username) {
        User user = userStore.get(username);
        return user != null ? user.getStatus() : null;
    }
}