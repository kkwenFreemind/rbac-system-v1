package com.rbac.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Authentication Module 主應用程式
 * 
 * 排除的自動配置說明：
 * - DataSourceAutoConfiguration: 不使用 JDBC DataSource
 * - DataSourceTransactionManagerAutoConfiguration: 不使用 JDBC 事務管理
 * - HibernateJpaAutoConfiguration: 不使用 JPA/Hibernate（本專案使用 MyBatis）
 * - SpringDataWebAutoConfiguration: 不使用 Spring Data Web（避免 JPA 依賴）
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    SpringDataWebAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "com.rbac.auth",
    "com.rbac.common"
})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}