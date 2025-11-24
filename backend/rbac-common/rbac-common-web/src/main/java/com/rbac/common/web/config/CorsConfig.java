package com.rbac.common.web.config;

import com.rbac.common.core.config.RbacProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 配置類
 *
 * 配置跨域資源共享（CORS）設定，允許前端應用程式安全地從不同域發送請求。
 * 此配置支援：
 * 1. 可配置的允許來源、方法和標頭
 * 2. 認證憑證支援（cookies、授權標頭等）
 * 3. 預檢請求快取優化
 * 4. 生產環境安全性考量
 *
 * 預設配置適用於常見的前端開發場景，可通過 application.yml 進行自訂。
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final RbacProperties rbacProperties;

    /**
     * 配置 CORS 設定
     *
     * 根據 RbacProperties 中的 CORS 配置來設定跨域規則：
     * - 允許的來源：預設支援常見的開發和生產來源
     * - 允許的方法：GET、POST、PUT、DELETE、OPTIONS 等標準 HTTP 方法
     * - 允許的標頭：常見的請求標頭，包括自訂標頭
     * - 認證支援：允許攜帶認證資訊（如 JWT tokens）
     * - 預檢快取：設定預檢請求的快取時間以提升效能
     *
     * @param registry CORS 註冊器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        RbacProperties.CorsProperties corsProps = rbacProperties.getCors();

        if (!corsProps.isEnabled()) {
            log.info("CORS is disabled");
            return;
        }

        log.info("Configuring CORS with settings: enabled={}, allowedOrigins={}, allowCredentials={}",
                corsProps.isEnabled(), corsProps.getAllowedOrigins(), corsProps.isAllowCredentials());

        registry.addMapping("/api/**")  // 僅對 API 端點應用 CORS
                .allowedOrigins(getAllowedOrigins(corsProps))
                .allowedMethods(getAllowedMethods(corsProps))
                .allowedHeaders(getAllowedHeaders(corsProps))
                .exposedHeaders(getExposedHeaders(corsProps))
                .allowCredentials(corsProps.isAllowCredentials())
                .maxAge(corsProps.getMaxAge());

        log.info("CORS configuration applied successfully");
    }

    /**
     * 獲取允許的來源列表
     *
     * 如果配置中沒有指定允許的來源，則使用預設的開發和生產來源。
     * 生產環境應明確配置允許的來源以增強安全性。
     *
     * @param corsProps CORS 配置屬性
     * @return 允許的來源陣列
     */
    private String[] getAllowedOrigins(RbacProperties.CorsProperties corsProps) {
        if (corsProps.getAllowedOrigins() != null && !corsProps.getAllowedOrigins().isEmpty()) {
            return corsProps.getAllowedOrigins().toArray(new String[0]);
        }

        // 預設允許的來源（開發環境）
        return new String[]{
                "http://localhost:3000",    // React 開發伺服器
                "http://localhost:8080",    // Vue.js 開發伺服器
                "http://localhost:4200",    // Angular 開發伺服器
                "http://127.0.0.1:3000",    // 本地 IP
                "http://127.0.0.1:8080",
                "http://127.0.0.1:4200"
        };
    }

    /**
     * 獲取允許的 HTTP 方法列表
     *
     * 如果配置中沒有指定允許的方法，則使用標準的 REST API 方法。
     *
     * @param corsProps CORS 配置屬性
     * @return 允許的方法陣列
     */
    private String[] getAllowedMethods(RbacProperties.CorsProperties corsProps) {
        if (corsProps.getAllowedMethods() != null && !corsProps.getAllowedMethods().isEmpty()) {
            return corsProps.getAllowedMethods().toArray(new String[0]);
        }

        // 預設允許的 HTTP 方法
        return new String[]{
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        };
    }

    /**
     * 獲取允許的請求標頭列表
     *
     * 如果配置中沒有指定允許的標頭，則允許所有常見的請求標頭。
     *
     * @param corsProps CORS 配置屬性
     * @return 允許的標頭陣列
     */
    private String[] getAllowedHeaders(RbacProperties.CorsProperties corsProps) {
        if (corsProps.getAllowedHeaders() != null && !corsProps.getAllowedHeaders().isEmpty()) {
            return corsProps.getAllowedHeaders().toArray(new String[0]);
        }

        // 預設允許的請求標頭
        return new String[]{
                "Accept",
                "Accept-Encoding",
                "Accept-Language",
                "Cache-Control",
                "Content-Type",
                "Content-Length",
                "Authorization",           // JWT tokens
                "X-Tenant-Id",            // 租戶標識
                "X-Trace-Id",             // 追蹤標識
                "X-Requested-With",
                "Origin",
                "Referer",
                "User-Agent"
        };
    }

    /**
     * 獲取暴露的響應標頭列表
     *
     * 這些標頭將對前端 JavaScript 程式碼可見。
     *
     * @param corsProps CORS 配置屬性
     * @return 暴露的標頭陣列
     */
    private String[] getExposedHeaders(RbacProperties.CorsProperties corsProps) {
        if (corsProps.getExposedHeaders() != null && !corsProps.getExposedHeaders().isEmpty()) {
            return corsProps.getExposedHeaders().toArray(new String[0]);
        }

        // 預設暴露的響應標頭
        return new String[]{
                "X-Tenant-Id",            // 當前租戶標識
                "X-Trace-Id",             // 請求追蹤標識
                "Content-Disposition"     // 檔案下載標頭
        };
    }
}