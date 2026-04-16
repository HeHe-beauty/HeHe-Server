package org.dev.hehe.controller.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.schedule.ScheduleCreateResponse;
import org.dev.hehe.service.schedule.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ScheduleController.createSchedule() 단위 테스트
 * - JWT 인증: @MockitoBean JwtProvider + Authorization 헤더
 * - userId는 JWT에서 자동 추출 (@LoginUser)
 */
@WebMvcTest(ScheduleController.class)
@Import(SecurityConfig.class)
@DisplayName("ScheduleController - 일정 생성 테스트")
class ScheduleCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String TEST_TOKEN = "test-token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setup() {
        given(jwtProvider.getUserIdFromToken(TEST_TOKEN)).willReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("POST /api/v1/schedules - 일정 생성 성공 (201 반환)")
    void createSchedule_success() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
                "hospitalName", "강남 제모 클리닉",
                "procedureName", "겨드랑이 레이저 제모",
                "visitTime", 1741680000L
        );

        given(scheduleService.createSchedule(anyLong(), any()))
                .willReturn(ScheduleCreateResponse.builder().scheduleId(1741680000000L).build());

        // when & then
        mockMvc.perform(post("/api/v1/schedules")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleId").value(1741680000000L));
    }

    @Test
    @DisplayName("POST /api/v1/schedules - 시술명 없이 생성 성공")
    void createSchedule_success_noProcedureName() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
                "hospitalName", "홍대 스킨케어",
                "visitTime", 1741766400L
        );

        given(scheduleService.createSchedule(anyLong(), any()))
                .willReturn(ScheduleCreateResponse.builder().scheduleId(1741766400000L).build());

        // when & then
        mockMvc.perform(post("/api/v1/schedules")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleId").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/schedules - 필수값 누락 시 400 반환")
    void createSchedule_fail_invalidInput() throws Exception {
        // given: service에서 INVALID_INPUT 예외 발생
        willThrow(new CommonException(ErrorCode.INVALID_INPUT))
                .given(scheduleService).createSchedule(anyLong(), any());

        Map<String, Object> requestBody = Map.of("hospitalName", "강남 클리닉"); // visitTime 누락

        // when & then
        mockMvc.perform(post("/api/v1/schedules")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }
}