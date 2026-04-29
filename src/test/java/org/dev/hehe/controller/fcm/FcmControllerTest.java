package org.dev.hehe.controller.fcm;

import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.fcm.FcmTestResponse;
import org.dev.hehe.service.fcm.FcmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FcmController 단위 테스트
 */
@WebMvcTest(FcmController.class)
@Import(SecurityConfig.class)
@DisplayName("FcmController 테스트")
class FcmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FcmService fcmService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        given(jwtProvider.getUserIdFromToken("test-token")).willReturn(1L);
    }

    @Test
    @DisplayName("POST /api/v1/fcm/test - 발송 성공")
    void sendTestPush_success() throws Exception {
        given(fcmService.sendTestPush(eq(1L))).willReturn(new FcmTestResponse(1, 0));

        mockMvc.perform(post("/api/v1/fcm/test")
                        .header("Authorization", "Bearer test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failCount").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/fcm/test - 활성 토큰 없을 때 successCount=0 반환")
    void sendTestPush_noToken_returnsZero() throws Exception {
        given(fcmService.sendTestPush(eq(1L))).willReturn(new FcmTestResponse(0, 0));

        mockMvc.perform(post("/api/v1/fcm/test")
                        .header("Authorization", "Bearer test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failCount").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/fcm/test - 인증 없으면 403")
    void sendTestPush_noAuth_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/fcm/test"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}