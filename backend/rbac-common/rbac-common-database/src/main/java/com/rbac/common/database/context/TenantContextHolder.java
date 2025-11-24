package com.rbac.common.database.context;

/**
 * 租戶上下文持有器
 * 使用 ThreadLocal 存儲當前請求的租戶 ID
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public class TenantContextHolder {

    private static final ThreadLocal<String> tenantIdHolder = new ThreadLocal<>();

    /**
     * 設置租戶 ID 到當前執行緒
     *
     * @param tenantId 租戶 ID
     * @throws IllegalArgumentException 如果 tenantId 為 null 或空字符串
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        tenantIdHolder.set(tenantId);
    }

    /**
     * 獲取當前執行緒的租戶 ID
     *
     * @return 租戶 ID，如果未設置則返回 null
     */
    public static String getTenantId() {
        return tenantIdHolder.get();
    }

    /**
     * 清除當前執行緒的租戶 ID
     * 必須在請求結束時調用以防止執行緒池污染
     */
    public static void clear() {
        tenantIdHolder.remove();
    }

    /**
     * 檢查是否已設置租戶 ID
     *
     * @return true 如果租戶 ID 已設置
     */
    public static boolean hasTenantId() {
        return getTenantId() != null;
    }
}