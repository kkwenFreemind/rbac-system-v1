package com.rbac.auth.context;

import lombok.extern.slf4j.Slf4j;

/**
 * UserContext 持有者
 *
 * <p>使用 ThreadLocal 管理 UserContext，提供跨模組的當前使用者資訊存取</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Slf4j
public class UserContextHolder {

    /**
     * ThreadLocal 儲存 UserContext
     */
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 設定當前執行緒的 UserContext
     *
     * @param context UserContext 物件
     */
    public static void setContext(UserContext context) {
        if (context != null) {
            log.debug("Setting UserContext for user: {}, tenant: {}", context.getUsername(), context.getTenantId());
        } else {
            log.debug("Clearing UserContext");
        }
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 取得當前執行緒的 UserContext
     *
     * @return UserContext 物件，如果不存在返回 null
     */
    public static UserContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 取得當前使用者的 ID
     *
     * @return 使用者 ID，如果不存在返回 null
     */
    public static Long getCurrentUserId() {
        UserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 取得當前租戶的 ID
     *
     * @return 租戶 ID，如果不存在返回 null
     */
    public static Long getCurrentTenantId() {
        UserContext context = getContext();
        return context != null ? context.getTenantId() : null;
    }

    /**
     * 取得當前使用者的名稱
     *
     * @return 使用者名稱，如果不存在返回 null
     */
    public static String getCurrentUsername() {
        UserContext context = getContext();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 取得當前使用者的角色清單
     *
     * @return 角色清單，如果不存在返回 null
     */
    public static java.util.List<String> getCurrentRoles() {
        UserContext context = getContext();
        return context != null ? context.getRoles() : null;
    }

    /**
     * 檢查當前使用者是否具有指定角色
     *
     * @param role 角色名稱
     * @return 如果具有該角色返回 true
     */
    public static boolean hasRole(String role) {
        UserContext context = getContext();
        return context != null && context.hasRole(role);
    }

    /**
     * 檢查當前使用者是否具有任一指定角色
     *
     * @param roles 角色名稱陣列
     * @return 如果具有任一角色返回 true
     */
    public static boolean hasAnyRole(String... roles) {
        UserContext context = getContext();
        return context != null && context.hasAnyRole(roles);
    }

    /**
     * 清除當前執行緒的 UserContext
     * <p>必須在請求結束時呼叫以防止記憶體洩漏</p>
     */
    public static void clear() {
        UserContext context = getContext();
        if (context != null) {
            log.debug("Clearing UserContext for user: {}", context.getUsername());
        }
        CONTEXT_HOLDER.remove();
    }

    /**
     * 檢查是否存在 UserContext
     *
     * @return 如果存在返回 true
     */
    public static boolean hasContext() {
        return getContext() != null;
    }

    private UserContextHolder() {
        // Utility class
    }
}