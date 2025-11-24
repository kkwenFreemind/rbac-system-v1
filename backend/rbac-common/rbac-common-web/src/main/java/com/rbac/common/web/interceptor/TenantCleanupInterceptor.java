package com.rbac.common.web.interceptor;

import com.rbac.common.database.context.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Tenant Cleanup Interceptor - 租戶上下文備援清理攔截器
 *
 * 此攔截器作為租戶上下文清理的第二道防線，提供備援清理機制。
 * 主要功能：
 * 1. 在請求完成後（無論成功或失敗）執行清理
 * 2. 作為防禦性措施，防止 TenantFilter 被繞過時的上下文洩漏
 * 3. 確保 ThreadLocal 不會在請求間洩漏租戶資訊
 *
 * 執行順序：在所有業務邏輯執行完畢後，但仍在響應發送前
 * 這是多層清理策略的第二層（Filter -> Interceptor -> AOP）
 *
 * 注意：此攔截器不會設定租戶上下文，只負責清理。
 * 租戶上下文的設定由 TenantFilter 負責。
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Component
@Slf4j
public class TenantCleanupInterceptor implements HandlerInterceptor {

    /**
     * 在請求完成後清理租戶上下文
     *
     * 此方法在整個請求處理完成後呼叫，包括：
     * - 正常請求完成
     * - 異常拋出
     * - 響應已準備發送
     *
     * 這是防禦性清理的最後機會，確保不會有租戶上下文洩漏。
     *
     * @param request HTTP 請求
     * @param response HTTP 響應
     * @param handler 處理請求的控制器方法
     * @param ex 如果請求處理期間發生異常，則為該異常；否則為 null
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // 防禦性清理，以防過濾器被繞過
        // 即使 TenantFilter 正常工作，此清理也不會造成傷害
        String tenantId = TenantContextHolder.getTenantId();

        if (tenantId != null) {
            // 記錄清理操作（僅在 debug 模式，因為這是常規操作）
            if (log.isDebugEnabled()) {
                log.debug("TenantCleanupInterceptor: Cleaning up tenant context for request: {} {} (Tenant ID: {})",
                         request.getMethod(), request.getRequestURI(), tenantId);
            }

            // 執行清理
            TenantContextHolder.clear();

            // 如果發生異常，記錄額外的警告
            if (ex != null) {
                log.warn("TenantCleanupInterceptor: Cleaned up tenant context after exception in request: {} {} (Tenant ID: {}, Exception: {})",
                        request.getMethod(), request.getRequestURI(), tenantId, ex.getClass().getSimpleName());
            }
        } else {
            // 如果沒有租戶上下文，這是正常的（例如：靜態資源請求）
            if (log.isTraceEnabled()) {
                log.trace("TenantCleanupInterceptor: No tenant context to clean for request: {} {}",
                         request.getMethod(), request.getRequestURI());
            }
        }
    }
}