package com.rbac.common.web.config;

import com.rbac.common.web.interceptor.TenantCleanupInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置類
 *
 * 此配置類負責註冊 Web 層的攔截器和過濾器，主要功能：
 * 1. 註冊 TenantCleanupInterceptor 作為租戶上下文的備援清理機制
 * 2. 配置攔截器的執行順序和路徑匹配
 * 3. 提供擴展點用於未來添加更多的 Web MVC 配置
 *
 * 注意：過濾器（Filter）通過 @Component 註解自動註冊，此處主要處理攔截器（Interceptor）。
 *
 * @author RBAC System
 * @since 1.0.0
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantCleanupInterceptor tenantCleanupInterceptor;

    /**
     * 註冊攔截器
     *
     * 配置攔截器的執行順序和適用路徑：
     * - TenantCleanupInterceptor：應用於所有請求路徑 /**，作為最後一道清理防線
     *
     * 攔截器執行順序：
     * 1. 過濾器鏈（TenantFilter -> TraceIdFilter -> RequestLogFilter）
     * 2. 攔截器（TenantCleanupInterceptor）
     * 3. 控制器方法執行
     * 4. 響應處理
     *
     * @param registry 攔截器註冊器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering web interceptors...");

        // 註冊租戶清理攔截器
        registry.addInterceptor(tenantCleanupInterceptor)
                .addPathPatterns("/**")  // 應用於所有請求路徑
                .excludePathPatterns(     // 排除靜態資源路徑
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/webjars/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**"  // Spring Boot Actuator 端點
                )
                .order(Integer.MAX_VALUE); // 最後執行，確保在所有業務邏輯之後清理

        log.info("Web interceptors registered successfully");
    }

    // 擴展點：可以添加更多的 Web MVC 配置方法
    // 例如：
    // - addResourceHandlers() 用於靜態資源處理
    // - addCorsMappings() 用於 CORS 配置（如果需要手動配置）
    // - configureMessageConverters() 用於自訂消息轉換器
    // - addArgumentResolvers() 用於自訂參數解析器
}