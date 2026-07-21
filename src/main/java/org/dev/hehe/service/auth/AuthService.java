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
     * 소셜 로그인
     *
     * 1. provider access token으로 소셜 유저 정보 조회
     * 2. DB 조회 (provider + socialId)
     *    - 기존 유저: 닉네임 최신화 후 JWT 발급 (exists=true)
     *    - 미가입 유저: INSERT 없이 exists=false만 반환 → FE가 회원가입 절차(동의 화면)로 유도
     *
     * @param provider    소셜 제공자 (kakao / naver)
     * @param accessToken FE로부터 받은 provider access token
     * @return exists=false(미가입) 또는 exists=true + accessToken/refreshToken/유저 정보
     * @throws CommonException AU004 (소셜 유저 정보 조회 실패)
     */
    public AuthLoginResponse login(String provider, String accessToken) {
        // 1. 소셜 유저 정보 조회
        OAuthUserInfo userInfo = getOAuthUserInfo(provider, accessToken);
        String providerUpper = provider.toUpperCase();

        log.info("[Auth] 소셜 로그인 시도 - provider: {}, socialId: {}", providerUpper, userInfo.getSocialId());

        // 2. DB 조회 — 없으면 회원가입 필요 응답
        Optional<User> existingUser = userMapper.findByProviderAndSocialId(providerUpper, userInfo.getSocialId());
        if (existingUser.isEmpty()) {
            log.info("[Auth] 미가입 유저 - provider: {}, socialId: {}", providerUpper, userInfo.getSocialId());
            return AuthLoginResponse.notFound();
        }

        // 3. 기존 유저: 닉네임/이메일 최신화 후 로그인 처리
        Long userId = existingUser.get().getUserId();
        String nickname = userInfo.getNickname();
        userMapper.updateProfile(userId, nickname, userInfo.getEmail());
        log.info("[Auth] 로그인 성공 - userId: {}, provider: {}, nickname: {}", userId, providerUpper, nickname);

        return issueTokens(userId, nickname);
    }

    /**
     * 회원가입
     *
     * 1. provider access token으로 소셜 유저 정보 재조회 (로그인 시도와 별개 호출)
     * 2. 이미 가입된 유저면 idempotent하게 로그인 처리 (중복 INSERT 방지)
     * 3. 신규면 동의값과 함께 INSERT 후 로그인 처리
     *
     * @param provider     소셜 제공자 (kakao / naver)
     * @param accessToken  FE로부터 받은 provider access token
     * @param pushAgreed   일반 푸시 동의
     * @param nightAgreed  야간 푸시 동의
     * @param mktAgreed    마케팅 수신 동의
     * @param isOverAge    14세 이상 동의 (컨트롤러 단에서 @AssertTrue로 이미 검증됨)
     * @param termsVersion 가입 시점 약관 버전
     * @return accessToken, refreshToken, 유저 정보
     * @throws CommonException AU004 (소셜 유저 정보 조회 실패)
     */
    public AuthLoginResponse signup(String provider, String accessToken,
                                    boolean pushAgreed, boolean nightAgreed, boolean mktAgreed,
                                    boolean isOverAge, String termsVersion) {
        OAuthUserInfo userInfo = getOAuthUserInfo(provider, accessToken);
        String providerUpper = provider.toUpperCase();
        String nickname = userInfo.getNickname();

        Optional<User> existingUser = userMapper.findByProviderAndSocialId(providerUpper, userInfo.getSocialId());
        if (existingUser.isPresent()) {
            // 이미 가입된 유저 — 중복 가입 요청을 에러 없이 로그인으로 처리 (idempotent)
            Long userId = existingUser.get().getUserId();
            log.info("[Auth] 이미 가입된 유저의 회원가입 재요청 - userId: {}, provider: {}", userId, providerUpper);
            return issueTokens(userId, existingUser.get().getNickname());
        }

        Long userId = generateUserId();
        userMapper.insertUser(userId, userInfo.getSocialId(), providerUpper, nickname, userInfo.getEmail(),
                pushAgreed, nightAgreed, mktAgreed, isOverAge, termsVersion);
        log.info("[Auth] 회원가입 완료 - userId: {}, provider: {}, socialId: {}, nickname: {}",
                userId, providerUpper, userInfo.getSocialId(), nickname);

        return issueTokens(userId, nickname);
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

    /**
     * 앱 JWT 발급 + Redis에 refresh token 저장 (로그인/회원가입 공통)
     */
    private AuthLoginResponse issueTokens(Long userId, String nickname) {
        String appAccessToken = jwtProvider.generateAccessToken(userId);
        String appRefreshToken = jwtProvider.generateRefreshToken(userId);
        redisTokenService.save(userId, appRefreshToken);

        return AuthLoginResponse.of(
                appAccessToken,
                appRefreshToken,
                new AuthLoginResponse.UserInfo(userId, nickname)
        );
    }
}