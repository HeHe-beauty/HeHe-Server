package org.dev.hehe.controller.user;

import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.user.UserSummaryResponse;
import org.dev.hehe.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController 단위 테스트
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String TEST_TOKEN = "test-token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setup() {
        given(jwtProvider.getUserIdFromToken(TEST_TOKEN)).willReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/v1/users/summary - 마이페이지 요약 조회 성공")
    void getSummary_success() throws Exception {
        // given
        UserSummaryResponse mockResponse = UserSummaryResponse.builder()
                .bookmarkCount(5)
                .contactCount(3)
                .scheduleCount(2)
                .build();

        given(userService.getSummary(TEST_USER_ID)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/users/summary")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookmarkCount").value(5))
                .andExpect(jsonPath("$.data.contactCount").value(3))
                .andExpect(jsonPath("$.data.scheduleCount").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/users/summary - 모든 항목 0건")
    void getSummary_allZero() throws Exception {
        // given
        UserSummaryResponse mockResponse = UserSummaryResponse.builder()
                .bookmarkCount(0)
                .contactCount(0)
                .scheduleCount(0)
                .build();

        given(userService.getSummary(TEST_USER_ID)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/users/summary")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookmarkCount").value(0))
                .andExpect(jsonPath("$.data.contactCount").value(0))
                .andExpect(jsonPath("$.data.scheduleCount").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/users/summary - 인증 토큰 없이 호출 시 4xx")
    void getSummary_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/summary"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
