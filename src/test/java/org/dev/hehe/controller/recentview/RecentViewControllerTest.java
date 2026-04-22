package org.dev.hehe.controller.recentview;

import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.recentview.RecentViewResponse;
import org.dev.hehe.service.recentview.RecentViewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RecentViewController 단위 테스트
 */
@WebMvcTest(RecentViewController.class)
@Import(SecurityConfig.class)
@DisplayName("RecentViewController 테스트")
class RecentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecentViewService recentViewService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String TEST_TOKEN = "test-token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setup() {
        given(jwtProvider.getUserIdFromToken(TEST_TOKEN)).willReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/v1/recent-views - 최근 본 병원 조회 성공")
    void getRecentViews_success() throws Exception {
        // given
        List<RecentViewResponse> mockList = List.of(
                RecentViewResponse.builder()
                        .hospitalId(101L).name("강남 제모 클리닉").address("서울 강남구")
                        .tags(List.of("젠틀맥스프로")).viewedAt(LocalDateTime.of(2026, 4, 22, 10, 30))
                        .build()
        );

        given(recentViewService.getRecentViews(TEST_USER_ID)).willReturn(mockList);

        // when & then
        mockMvc.perform(get("/api/v1/recent-views")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].hospitalId").value(101))
                .andExpect(jsonPath("$.data[0].name").value("강남 제모 클리닉"));
    }

    @Test
    @DisplayName("GET /api/v1/recent-views - 최근 본 병원 없음 (빈 배열)")
    void getRecentViews_empty() throws Exception {
        // given
        given(recentViewService.getRecentViews(TEST_USER_ID)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/recent-views")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/recent-views/{hospitalId} - 최근 본 병원 기록 성공")
    void recordRecentView_success() throws Exception {
        // given
        willDoNothing().given(recentViewService).recordRecentView(eq(TEST_USER_ID), eq(101L));

        // when & then
        mockMvc.perform(post("/api/v1/recent-views/101")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/recent-views - 인증 토큰 없이 호출 시 4xx")
    void getRecentViews_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/recent-views"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/v1/recent-views/{hospitalId} - 인증 토큰 없이 호출 시 4xx")
    void recordRecentView_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/recent-views/101"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}