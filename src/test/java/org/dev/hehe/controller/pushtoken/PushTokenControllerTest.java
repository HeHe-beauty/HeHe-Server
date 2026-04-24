package org.dev.hehe.controller.pushtoken;

import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.service.pushtoken.PushTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PushTokenController 단위 테스트
 */
@WebMvcTest(PushTokenController.class)
@Import(SecurityConfig.class)
@DisplayName("PushTokenController 테스트")
class PushTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PushTokenService pushTokenService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        given(jwtProvider.getUserIdFromToken("test-token")).willReturn(1L);
    }

    @Test
    @DisplayName("POST /api/v1/push-tokens - 토큰 등록 성공")
    void registerToken_success() throws Exception {
        willDoNothing().given(pushTokenService).registerToken(eq(1L), any());

        mockMvc.perform(post("/api/v1/push-tokens")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "test-fcm-token",
                                  "platform": "ANDROID",
                                  "notificationPermissionGranted": true
                                }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/push-tokens - 필수 값 누락 시 400")
    void registerToken_missingField() throws Exception {
        mockMvc.perform(post("/api/v1/push-tokens")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform": "ANDROID",
                                  "notificationPermissionGranted": true
                                }
                                """))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    @Test
    @DisplayName("POST /api/v1/push-tokens - 유효하지 않은 플랫폼 시 400")
    void registerToken_invalidPlatform() throws Exception {
        mockMvc.perform(post("/api/v1/push-tokens")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "test-fcm-token",
                                  "platform": "WINDOWS",
                                  "notificationPermissionGranted": true
                                }
                                """))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    @Test
    @DisplayName("DELETE /api/v1/push-tokens - 토큰 비활성화 성공")
    void deactivateToken_success() throws Exception {
        willDoNothing().given(pushTokenService).deactivateToken(eq(1L), any());

        mockMvc.perform(delete("/api/v1/push-tokens")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "test-fcm-token" }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/push-tokens - 토큰 없어도 200 (idempotent)")
    void deactivateToken_notFound_stillOk() throws Exception {
        willDoNothing().given(pushTokenService).deactivateToken(eq(1L), any());

        mockMvc.perform(delete("/api/v1/push-tokens")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "non-existent-token" }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
