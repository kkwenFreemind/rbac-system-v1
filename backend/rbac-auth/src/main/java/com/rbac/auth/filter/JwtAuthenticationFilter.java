package com.rbac.auth.filter;

import com.rbac.auth.context.UserContext;
import com.rbac.auth.context.UserContextHolder;
import com.rbac.auth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 認證過濾器
 *
 * <p>驗證 JWT Token 並將 UserContext 注入到 ThreadLocal</p>
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && jwtTokenService.validateToken(token)) {
                UserContext userContext = jwtTokenService.extractUserContext(token);
                UserContextHolder.setContext(userContext);

                log.debug("JWT token validated successfully for user: {}", userContext.getUsername());
            } else if (token != null) {
                log.debug("Invalid JWT token provided");
            }
        } catch (Exception e) {
            log.warn("Error processing JWT token: {}", e.getMessage());
            // 不拋出異常，讓請求繼續，但不設定 UserContext
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                // 確保在請求結束時清除 UserContext，防止記憶體洩漏
                UserContextHolder.clear();
            }
        }
    }

    /**
     * 從請求中提取 JWT Token
     *
     * @param request HTTP 請求
     * @return JWT Token，如果沒有則返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}