package org.dev.hehe.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.auth.AuthLoginResponse;
import org.dev.hehe.dto.auth.TokenRefreshResponse;
import org.dev.hehe.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 단위 테스트
 * - @WebMvcTest: Controller 레이어만 로드
 * - @Import(SecurityConfig): JWT 필터 체인 적용 (auth 경로는 permitAll)
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // SecurityConfig가 JwtProvider에 의존하므로 Mock 등록
    @MockitoBean
    private JwtProvider jwtProvider;

    // ── POST /api/v1/auth/login ──────────────────────────────────────────────

    @Test
    @DisplayName("소셜 로그인 성공 - 200 응답 및 토큰 반환")
    void login_success() throws Exception {
        // given
        AuthLoginResponse.UserInfo userInfo = new AuthLoginResponse.UserInfo(1L, "홍길동");
        AuthLoginResponse response = new AuthLoginResponse("access_token", "refresh_token", userInfo);

        given(authService.login(anyString(), anyString())).willReturn(response);

        Map<String, String> request = Map.of(
                "provider", "kakao",
                "accessToken", "provider_token"
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access_token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh_token"))
                .andExpect(jsonPath("$.data.user.userId").value(1))
                .andExpect(jsonPath("$.data.user.nickname").value("홍길동"));
    }

    @Test
    @DisplayName("소셜 로그인 - provider 누락 시 400 응답")
    void login_missingProvider_returns400() throws Exception {
        Map<String, String> request = Map.of("accessToken", "provider_token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    @Test
    @DisplayName("소셜 로그인 - 잘못된 provider 값 시 400 응답")
    void login_invalidProvider_returns400() throws Exception {
        Map<String, String> request = Map.of(
                "provider", "google",
                "accessToken", "provider_token"
        );

        given(authService.login(anyString(), anyString()))
                .willThrow(new CommonException(ErrorCode.INVALID_INPUT));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("소셜 로그인 - 소셜 유저 정보 조회 실패 시 502 응답")
    void login_oauthFailed_returns502() throws Exception {
        Map<String, String> request = Map.of(
                "provider", "kakao",
                "accessToken", "invalid_token"
        );

        given(authService.login(anyString(), anyString()))
                .willThrow(new CommonException(ErrorCode.OAUTH_USER_INFO_FAILED));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AU004"));
    }

    // ── POST /api/v1/auth/token/refresh ─────────────────────────────────────

    @Test
    @DisplayName("Access Token 재발급 성공 - 200 응답")
    void refresh_success() throws Exception {
        // given
        TokenRefreshResponse response = new TokenRefreshResponse("new_access_token");
        given(authService.refresh(anyString())).willReturn(response);

        Map<String, String> request = Map.of("refreshToken", "valid_refresh_token");

        // when & then
        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"));
    }

    @Test
    @DisplayName("Access Token 재발급 - refreshToken 누락 시 400 응답")
    void refresh_missingToken_returns400() throws Exception {
        Map<String, String> request = Map.of();

        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    @Test
    @DisplayName("Access Token 재발급 - 만료된 토큰 시 401 응답")
    void refresh_expiredToken_returns401() throws Exception {
        Map<String, String> request = Map.of("refreshToken", "expired_token");

        given(authService.refresh(anyString()))
                .willThrow(new CommonException(ErrorCode.EXPIRED_TOKEN));

        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AU002"));
    }
}