package com.rbac.auth.config;

import com.rbac.auth.filter.JwtAuthenticationFilter;
import com.rbac.auth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenService jwtTokenService;

    /**
     * 配置安全過濾鏈
     *
     * @param http HTTP 安全配置
     * @return 安全過濾鏈
     * @throws Exception 配置異常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF，因為使用 JWT
            .csrf(csrf -> csrf.disable())

            // 配置授權規則
            .authorizeHttpRequests(authz -> authz
                // 登入端點允許匿名存取
                .requestMatchers("/api/v1/auth/login").permitAll()
                // 其他所有請求需要認證
                .anyRequest().authenticated()
            )

            // 配置會話管理 - 無狀態，因為使用 JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 添加 JWT 認證過濾器
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT 認證過濾器 Bean
     *
     * @return JWT 認證過濾器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenService);
    }
}