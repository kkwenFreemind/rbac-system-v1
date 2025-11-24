package com.rbac.common.web.filter;

import com.rbac.common.core.config.RbacProperties;
import com.rbac.common.core.exception.TenantException;
import com.rbac.common.database.context.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Tenant Filter - 從請求中提取租戶 ID 並設定到 TenantContextHolder
 *
 * 此過濾器負責：
 * 1. 從請求標頭、JWT token 或子網域提取租戶 ID
 * 2. 驗證租戶 ID 的有效性
 * 3. 將租戶 ID 設定到 ThreadLocal 上下文
 * 4. 在請求結束時清理上下文以防止線程池污染
 *
 * 執行順序：最高優先權（在所有其他過濾器之前）
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final RbacProperties rbacProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            // 1. 提取租戶 ID
            String tenantId = extractTenantId(request);

            // 2. 驗證租戶 ID
            validateTenantId(tenantId);

            // 3. 設定到上下文
            TenantContextHolder.setTenantId(tenantId);

            // 4. 繼續處理請求
            chain.doFilter(request, response);

        } catch (TenantException e) {
            log.warn("Tenant validation failed: {}", e.getMessage());
            handleTenantException(response, e);
        } catch (Exception e) {
            log.error("Unexpected error in TenantFilter", e);
            handleGenericException(response, e);
        } finally {
            // 關鍵：清理上下文，防止線程池污染
            TenantContextHolder.clear();
        }
    }

    /**
     * 從請求中提取租戶 ID
     *
     * 優先順序：
     * 1. 請求標頭（X-Tenant-Id）
     * 2. JWT token 中的 tenant_id 聲明
     * 3. 子網域（例如：tenant1.example.com -> tenant1）
     *
     * @param request HTTP 請求
     * @return 租戶 ID
     * @throws TenantException 如果無法提取租戶 ID
     */
    private String extractTenantId(HttpServletRequest request) {
        String tenantId = null;

        // 選項 1：從請求標頭提取
        String headerName = rbacProperties.getTenant().getHeaderName();
        tenantId = request.getHeader(headerName);

        if (tenantId != null && !tenantId.trim().isEmpty()) {
            log.debug("Extracted tenant ID from header '{}': {}", headerName, tenantId);
            return tenantId.trim();
        }

        // 選項 2：從 JWT token 提取（如果有的話）
        // 注意：此處假設 JWT token 在 Authorization 標頭中
        // 實際實作應根據您的認證機制調整
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tenantId = extractTenantFromJwt(authHeader.substring(7));
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                log.debug("Extracted tenant ID from JWT token: {}", tenantId);
                return tenantId.trim();
            }
        }

        // 選項 3：從子網域提取
        String host = request.getServerName();
        tenantId = extractTenantFromSubdomain(host);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            log.debug("Extracted tenant ID from subdomain '{}': {}", host, tenantId);
            return tenantId.trim();
        }

        // 如果都沒有找到,拋出異常
        throw new TenantException("TENANT_NOT_FOUND", "Unable to extract tenant ID from request");
    }

    /**
     * 從 JWT token 提取租戶 ID
     *
     * 注意：此為示例實作。實際應使用您的 JWT 解析邏輯。
     * 建議在 Auth 模組中實作完整的 JWT 處理。
     *
     * @param token JWT token
     * @return 租戶 ID 或 null
     */
    private String extractTenantFromJwt(String token) {
        // TODO: 實作 JWT 解析邏輯
        // 示例：使用 JJWT 庫解析 token 並提取 tenant_id 聲明
        // 此處返回 null 表示未實作
        log.debug("JWT tenant extraction not implemented yet");
        return null;
    }

    /**
     * 從子網域提取租戶 ID
     *
     * 示例：
     * - tenant1.example.com -> tenant1
     * - sub.tenant2.example.com -> tenant2
     *
     * @param host 請求的主機名
     * @return 租戶 ID 或 null
     */
    private String extractTenantFromSubdomain(String host) {
        if (host == null || host.isEmpty()) {
            return null;
        }

        // 移除端口號（如果有）
        if (host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        }

        // 分割子網域
        String[] parts = host.split("\\.");

        // 如果只有一個部分（例如：localhost），不是子網域
        if (parts.length <= 1) {
            return null;
        }

        // 假設租戶 ID 是第一個子網域
        // 注意：您可能需要調整此邏輯以適應您的網域結構
        String potentialTenantId = parts[0];

        // 驗證是否為有效的租戶 ID 格式
        if (isValidTenantIdFormat(potentialTenantId)) {
            return potentialTenantId;
        }

        return null;
    }

    /**
     * 驗證租戶 ID 格式
     *
     * @param tenantId 租戶 ID
     * @return true 如果格式有效
     */
    private boolean isValidTenantIdFormat(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return false;
        }

        // 基本驗證：只允許字母、數字、底線和連字號
        // 長度限制：1-50 個字符
        return tenantId.matches("^[a-zA-Z0-9_-]{1,50}$");
    }

    /**
     * 驗證租戶 ID
     *
     * @param tenantId 租戶 ID
     * @throws TenantException 如果租戶 ID 無效
     */
    private void validateTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new TenantException("TENANT_INVALID", "Tenant ID cannot be null or empty");
        }

        if (!isValidTenantIdFormat(tenantId)) {
            throw new TenantException("TENANT_INVALID_FORMAT",
                "Tenant ID contains invalid characters or exceeds length limit");
        }

        // TODO: 可選 - 檢查租戶是否存在於資料庫中
        // 此檢查應在 Auth 模組中實作，以避免循環依賴
    }

    /**
     * 處理租戶異常
     *
     * @param response HTTP 響應
     * @param e 租戶異常
     * @throws IOException 如果寫入響應失敗
     */
    private void handleTenantException(HttpServletResponse response, TenantException e) throws IOException {
        // 設定 HTTP 狀態碼為 400 Bad Request 或 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");

        // 返回簡單的錯誤訊息
        String errorJson = String.format(
            "{\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}",
            e.getCode(),
            e.getMessage(),
            System.currentTimeMillis()
        );

        response.getWriter().write(errorJson);
    }

    /**
     * 處理一般異常
     *
     * @param response HTTP 響應
     * @param e 一般異常
     * @throws IOException 如果寫入響應失敗
     */
    private void handleGenericException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");

        String errorJson = String.format(
            "{\"code\":\"SYSTEM_ERROR\",\"message\":\"Internal server error\",\"timestamp\":%d}",
            System.currentTimeMillis()
        );

        response.getWriter().write(errorJson);
    }
}