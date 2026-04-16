package org.dev.hehe.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성·검증·파싱 유틸리티
 * - accessToken  : 만료 1시간
 * - refreshToken : 만료 14일
 */
@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry * 1000L;   // 초 → 밀리초
        this.refreshTokenExpiry = refreshTokenExpiry * 1000L;
    }

    /**
     * Access Token 발급
     *
     * @param userId 유저 PK
     * @return 서명된 JWT 문자열
     */
    public String generateAccessToken(Long userId) {
        return buildToken(userId, accessTokenExpiry);
    }

    /**
     * Refresh Token 발급
     *
     * @param userId 유저 PK
     * @return 서명된 JWT 문자열
     */
    public String generateRefreshToken(Long userId) {
        return buildToken(userId, refreshTokenExpiry);
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token JWT 문자열
     * @return 유효하면 true
     * @throws CommonException AU001 (유효하지 않은 토큰), AU002 (만료된 토큰)
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰: {}", e.getMessage());
            throw new CommonException(ErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("[JWT] 유효하지 않은 토큰: {}", e.getMessage());
            throw new CommonException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰에서 userId 추출
     *
     * @param token JWT 문자열
     * @return userId (Long)
     */
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private String buildToken(Long userId, long expiryMs) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}