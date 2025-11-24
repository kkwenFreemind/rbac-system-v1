package com.rbac.common.database.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.rbac.common.core.exception.TenantException;
import com.rbac.common.database.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置類
 *
 * 配置租戶隔離攔截器和分頁支援
 *
 * @author RBAC System
 */
@Configuration
@Slf4j
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 攔截器
     *
     * @return MybatisPlusInterceptor 實例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 租戶隔離攔截器 - 自動為所有查詢添加 tenant_id 過濾條件
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = TenantContextHolder.getTenantId();
                if (tenantId == null) {
                    log.error("租戶上下文未設定！無法執行資料庫操作。");
                    throw new TenantException("租戶上下文未設定，無法執行資料庫操作");
                }
                try {
                    return new LongValue(Long.parseLong(tenantId));
                } catch (NumberFormatException e) {
                    log.error("無效的租戶 ID 格式: {}", tenantId, e);
                    throw new TenantException("無效的租戶 ID 格式: " + tenantId);
                }
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 系統表不進行租戶隔離（例如：租戶主表、配置表等）
                return "sys_tenant".equals(tableName) ||
                       "sys_config".equals(tableName) ||
                       tableName.startsWith("flyway_") ||
                       tableName.startsWith("databasechangelog");
            }
        });

        // 分頁攔截器 - 支援分頁查詢
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        // 設定資料庫類型（PostgreSQL）
        // paginationInterceptor.setDbType(DbType.POSTGRESQL);

        // 添加攔截器到 MyBatis-Plus 攔截器鏈
        interceptor.addInnerInterceptor(tenantInterceptor);
        interceptor.addInnerInterceptor(paginationInterceptor);

        log.info("MyBatis-Plus 攔截器配置完成：租戶隔離和分頁支援已啟用");
        return interceptor;
    }
}