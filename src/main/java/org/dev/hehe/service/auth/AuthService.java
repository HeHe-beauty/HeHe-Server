package org.dev.hehe.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.domain.user.User;
import org.dev.hehe.dto.auth.AuthLoginResponse;
import org.dev.hehe.dto.auth.TokenRefreshResponse;
import org.dev.hehe.mapper.user.UserMapper;
import org.dev.hehe.service.auth.oauth.KakaoOAuthClient;
import org.dev.hehe.service.auth.oauth.NaverOAuthClient;
import org.dev.hehe.service.auth.oauth.OAuthUserInfo;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 인증 서비스
 * - 소셜 로그인 (카카오/네이버 Token Flow)
 * - 로그아웃
 * - Access Token 재발급
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final NaverOAuthClient naverOAuthClient;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final RedisTokenService redisTokenService;

    /**
     * 소셜 로그인 / 자동 회원가입
     *
     * 1. provider access token으로 소셜 유저 정보 조회
     * 2. DB 조회 (provider + socialId)
     *    - 기존 유저: 닉네임 최신화 후 로그인
     *    - 신규 유저: INSERT 후 로그인
     * 3. 앱 JWT 발급 및 Redis에 refresh token 저장
     *
     * @param provider    소셜 제공자 (kakao / naver)
     * @param accessToken FE로부터 받은 provider access token
     * @return accessToken, refreshToken, 유저 정보
     * @throws CommonException AU004 (소셜 유저 정보 조회 실패)
     */
    public AuthLoginResponse login(String provider, String accessToken) {
        // 1. 소셜 유저 정보 조회
        OAuthUserInfo userInfo = getOAuthUserInfo(provider, accessToken);
        String providerUpper = provider.toUpperCase();

        log.info("[Auth] 소셜 로그인 시도 - provider: {}, socialId: {}", providerUpper, userInfo.getSocialId());

        // 2. DB 조회 → 신규이면 INSERT
        Optional<User> existingUser = userMapper.findByProviderAndSocialId(providerUpper, userInfo.getSocialId());

        Long userId;
        String nickname = userInfo.getNickname();

        if (existingUser.isPresent()) {
            // 기존 유저: 닉네임 최신화
            userId = existingUser.get().getUserId();
            userMapper.updateNickname(userId, nickname);
            log.info("[Auth] 기존 유저 로그인 - userId: {}", userId);
        } else {
            // 신규 유저: 회원가입
            userId = generateUserId();
            userMapper.insertUser(userId, userInfo.getSocialId(), providerUpper, nickname);
            log.info("[Auth] 신규 유저 가입 - userId: {}, provider: {}", userId, providerUpper);
        }

        // 3. 앱 JWT 발급
        String appAccessToken = jwtProvider.generateAccessToken(userId);
        String appRefreshToken = jwtProvider.generateRefreshToken(userId);

        // 4. Redis에 refresh token 저장
        redisTokenService.save(userId, appRefreshToken);

        return new AuthLoginResponse(
                appAccessToken,
                appRefreshToken,
                new AuthLoginResponse.UserInfo(userId, nickname)
        );
    }

    /**
     * 로그아웃 — Redis에서 Refresh Token 삭제
     *
     * @param userId 비즈니스 유저 ID (JWT SecurityContext에서 추출)
     */
    public void logout(Long userId) {
        redisTokenService.delete(userId);
        log.info("[Auth] 로그아웃 - userId: {}", userId);
    }

    /**
     * Access Token 재발급
     *
     * 1. refresh token 유효성 검증 (JwtProvider)
     * 2. Redis에 저장된 refresh token과 일치 여부 확인
     * 3. 새 access token 발급
     *
     * @param refreshToken 클라이언트가 보낸 refresh token
     * @return 새로 발급된 access token
     * @throws CommonException AU001/AU002 (토큰 검증 실패), AU003 (Redis 불일치)
     */
    public TokenRefreshResponse refresh(String refreshToken) {
        // 1. JWT 유효성 검증 (만료·형식 오류 시 예외)
        jwtProvider.validateToken(refreshToken);
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        // 2. Redis에 저장된 토큰과 일치 여부 확인
        String savedToken = redisTokenService.get(userId);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            log.warn("[Auth] Refresh Token 불일치 또는 만료 - userId: {}", userId);
            throw new CommonException(ErrorCode.UNAUTHORIZED);
        }

        // 3. 새 access token 발급
        String newAccessToken = jwtProvider.generateAccessToken(userId);
        log.info("[Auth] Access Token 재발급 - userId: {}", userId);

        return new TokenRefreshResponse(newAccessToken);
    }

    // ── private ──────────────────────────────────────────────────────────────

    /**
     * provider에 따라 적절한 OAuth 클라이언트 선택 후 유저 정보 조회
     */
    private OAuthUserInfo getOAuthUserInfo(String provider, String accessToken) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakaoOAuthClient.getUserInfo(accessToken);
            case "naver" -> naverOAuthClient.getUserInfo(accessToken);
            default -> throw new CommonException(ErrorCode.INVALID_INPUT);
        };
    }

    /**
     * 비즈니스 유저 ID 생성 (양수 랜덤 Long)
     */
    private Long generateUserId() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }
}