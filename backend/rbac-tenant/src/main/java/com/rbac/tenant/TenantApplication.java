package com.rbac.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Tenant Management Module 主應用程式
 *
 * 此模組提供租戶管理的核心功能，包括：
 * - 租戶 CRUD 操作
 * - 多租戶資料隔離
 * - 租戶狀態管理
 *
 * @author CHANG SHOU-WEN
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.rbac.tenant",
    "com.rbac.common"
})
public class TenantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantApplication.class, args);
    }
}