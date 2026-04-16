package org.dev.hehe.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis Refresh Token 관리 서비스
 * - key 형식: RT:{userId}
 * - TTL: 14일
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private static final String KEY_PREFIX = "RT:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 14;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Refresh Token 저장
     *
     * @param userId       비즈니스 유저 ID
     * @param refreshToken 저장할 refresh token
     */
    public void save(Long userId, String refreshToken) {
        String key = buildKey(userId);
        stringRedisTemplate.opsForValue()
                .set(key, refreshToken, REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);
        log.debug("[Redis] Refresh Token 저장 - key: {}", key);
    }

    /**
     * Refresh Token 조회
     *
     * @param userId 비즈니스 유저 ID
     * @return refresh token (없으면 null)
     */
    public String get(Long userId) {
        return stringRedisTemplate.opsForValue().get(buildKey(userId));
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     *
     * @param userId 비즈니스 유저 ID
     */
    public void delete(Long userId) {
        String key = buildKey(userId);
        stringRedisTemplate.delete(key);
        log.info("[Redis] Refresh Token 삭제 - key: {}", key);
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}