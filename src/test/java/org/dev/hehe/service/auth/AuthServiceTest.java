package org.dev.hehe.service.auth;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * AuthService 단위 테스트
 * - @ExtendWith(MockitoExtension): 순수 Mockito 기반 (Spring Context 불필요)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Mock
    private NaverOAuthClient naverOAuthClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTokenService redisTokenService;

    // ── 테스트용 OAuthUserInfo 구현 ─────────────────────────────────────────

    private OAuthUserInfo mockOAuthUserInfo(String provider, String socialId, String nickname) {
        return new OAuthUserInfo() {
            @Override public String getSocialId() { return socialId; }
            @Override public String getNickname() { return nickname; }
            @Override public String getProvider() { return provider; }
        };
    }

    private User mockUser(Long userId, String nickname) {
        User user = new User();
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "nickname", nickname);
        return user;
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("카카오 기존 유저 로그인 성공 - INSERT 없이 JWT 발급")
    void login_kakao_existingUser_success() {
        // given
        String provider = "kakao";
        String providerToken = "kakao_token";
        Long userId = 1000L;
        String nickname = "홍길동";

        OAuthUserInfo userInfo = mockOAuthUserInfo(provider, "kakao_social_1", nickname);
        User existingUser = mockUser(userId, nickname);

        given(kakaoOAuthClient.getUserInfo(providerToken)).willReturn(userInfo);
        given(userMapper.findByProviderAndSocialId("KAKAO", "kakao_social_1"))
                .willReturn(Optional.of(existingUser));
        given(jwtProvider.generateAccessToken(userId)).willReturn("access_token");
        given(jwtProvider.generateRefreshToken(userId)).willReturn("refresh_token");
        willDoNothing().given(userMapper).updateNickname(anyLong(), anyString());
        willDoNothing().given(redisTokenService).save(anyLong(), anyString());

        // when
        AuthLoginResponse response = authService.login(provider, providerToken);

        // then
        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.refreshToken()).isEqualTo("refresh_token");
        assertThat(response.user().userId()).isEqualTo(userId);
        assertThat(response.user().nickname()).isEqualTo(nickname);

        verify(userMapper, never()).insertUser(anyLong(), anyString(), anyString(), anyString());
        verify(userMapper).updateNickname(userId, nickname);
        verify(redisTokenService).save(userId, "refresh_token");
    }

    @Test
    @DisplayName("네이버 신규 유저 로그인 성공 - INSERT 후 JWT 발급")
    void login_naver_newUser_success() {
        // given
        String provider = "naver";
        String providerToken = "naver_token";
        String nickname = "김철수";

        OAuthUserInfo userInfo = mockOAuthUserInfo(provider, "naver_social_1", nickname);

        given(naverOAuthClient.getUserInfo(providerToken)).willReturn(userInfo);
        given(userMapper.findByProviderAndSocialId("NAVER", "naver_social_1"))
                .willReturn(Optional.empty());
        given(jwtProvider.generateAccessToken(anyLong())).willReturn("access_token");
        given(jwtProvider.generateRefreshToken(anyLong())).willReturn("refresh_token");
        willDoNothing().given(userMapper).insertUser(anyLong(), anyString(), anyString(), anyString());
        willDoNothing().given(redisTokenService).save(anyLong(), anyString());

        // when
        AuthLoginResponse response = authService.login(provider, providerToken);

        // then
        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.refreshToken()).isEqualTo("refresh_token");
        assertThat(response.user().nickname()).isEqualTo(nickname);

        verify(userMapper).insertUser(anyLong(), eq("naver_social_1"), eq("NAVER"), eq(nickname));
        verify(userMapper, never()).updateNickname(anyLong(), anyString());
    }

    @Test
    @DisplayName("잘못된 provider 입력 시 INVALID_INPUT 예외 발생")
    void login_invalidProvider_throwsException() {
        assertThatThrownBy(() -> authService.login("google", "some_token"))
                .isInstanceOf(CommonException.class)
                .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 - Redis에서 refresh token 삭제")
    void logout_success() {
        // given
        Long userId = 1L;
        willDoNothing().given(redisTokenService).delete(userId);

        // when
        authService.logout(userId);

        // then
        verify(redisTokenService).delete(userId);
    }

    // ── refresh ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Access Token 재발급 성공")
    void refresh_success() {
        // given
        String refreshToken = "valid_refresh_token";
        Long userId = 1L;

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(redisTokenService.get(userId)).willReturn(refreshToken);
        given(jwtProvider.generateAccessToken(userId)).willReturn("new_access_token");

        // when
        TokenRefreshResponse response = authService.refresh(refreshToken);

        // then
        assertThat(response.accessToken()).isEqualTo("new_access_token");
    }

    @Test
    @DisplayName("Redis에 저장된 토큰과 불일치 시 UNAUTHORIZED 예외 발생")
    void refresh_tokenMismatch_throwsException() {
        // given
        String refreshToken = "valid_refresh_token";
        Long userId = 1L;

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(redisTokenService.get(userId)).willReturn("different_token"); // 불일치

        // when & then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(CommonException.class)
                .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Redis에 저장된 토큰이 없을 때 UNAUTHORIZED 예외 발생")
    void refresh_noTokenInRedis_throwsException() {
        // given
        String refreshToken = "valid_refresh_token";
        Long userId = 1L;

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(redisTokenService.get(userId)).willReturn(null); // Redis에 없음

        // when & then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(CommonException.class)
                .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHORIZED));
    }
}