package com.rbac.auth.service;

import com.rbac.auth.config.JwtConfig;
import com.rbac.auth.context.UserContext;
import com.rbac.common.redis.util.CacheService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token 服務實作
 *
 * @author CHANG SHOU-WEN, AI-Enhanced
 * @since 2025/11/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtConfig jwtConfig;
    private final CacheService cacheService;

    /**
     * JWT 密鑰
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    @Override
    public String generateToken(UserContext userContext) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtConfig.getExpiration(), ChronoUnit.SECONDS);
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .header()
                    .type("JWT")
                    .and()
                .issuer(jwtConfig.getIssuer())
                .subject(jwtConfig.getSubject())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("user_id", userContext.getUserId())
                .claim("tenant_id", userContext.getTenantId())
                .claim("username", userContext.getUsername())
                .claim("roles", userContext.getRoles())
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        log.debug("Generated JWT token for user: {}, jti: {}", userContext.getUsername(), jti);
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);

            // 檢查是否過期
            if (claims.getExpiration().before(new Date())) {
                log.debug("Token expired");
                return false;
            }

            // 檢查是否在黑名單中
            String jti = claims.getId();
            if (isTokenBlacklisted(jti)) {
                log.debug("Token is blacklisted: {}", jti);
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public UserContext extractUserContext(String token) {
        Claims claims = extractClaims(token);

        UserContext context = new UserContext();
        context.setUserId(claims.get("user_id", Long.class));
        context.setTenantId(claims.get("tenant_id", Long.class));
        context.setUsername(claims.get("username", String.class));
        context.setRoles(claims.get("roles", java.util.List.class));
        context.setJti(claims.getId());

        return context;
    }

    @Override
    public boolean isTokenBlacklisted(String jti) {
        String blacklistKey = "auth:blacklist:" + jti;
        return cacheService.exists(blacklistKey);
    }

    @Override
    public void addToBlacklist(String jti, long ttlSeconds) {
        String blacklistKey = "auth:blacklist:" + jti;
        cacheService.set(blacklistKey, "blacklisted", ttlSeconds);
        log.debug("Added token to blacklist: {}, TTL: {}s", jti, ttlSeconds);
    }

    @Override
    public long calculateRemainingValidity(String token) {
        try {
            Claims claims = extractClaims(token);
            long expirationTime = claims.getExpiration().getTime() / 1000; // 轉換為秒
            long currentTime = Instant.now().getEpochSecond();
            return Math.max(0, expirationTime - currentTime);
        } catch (Exception e) {
            log.debug("Failed to calculate remaining validity: {}", e.getMessage());
            return 0;
        }
    }
}