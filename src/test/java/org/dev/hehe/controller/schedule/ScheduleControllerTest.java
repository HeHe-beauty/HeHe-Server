package org.dev.hehe.controller.schedule;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.schedule.ScheduleAlarmResponse;
import org.dev.hehe.dto.schedule.ScheduleResponse;
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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ScheduleController 단위 테스트
 * - @WebMvcTest: Controller 레이어만 로드 (Service는 Mock 처리)
 * - @Import(SecurityConfig): JWT 필터 체인 적용
 * - JwtProvider를 Mock으로 등록하여 테스트 토큰("test-token") 인증 처리
 */
@WebMvcTest(ScheduleController.class)
@Import(SecurityConfig.class)
@DisplayName("ScheduleController 테스트")
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    // SecurityConfig가 JwtProvider에 의존하므로 Mock 등록
    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String TEST_TOKEN = "test-token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setup() {
        // "test-token"을 유효한 토큰으로 처리하고 userId=1L 반환
        given(jwtProvider.getUserIdFromToken(TEST_TOKEN)).willReturn(TEST_USER_ID);
        // validateToken은 void이므로 예외 미발생(기본 동작)으로 유효한 토큰 처리
    }

    // =============================================
    // GET /api/v1/schedules/{scheduleId} 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/schedules/{scheduleId} - 알림 포함 단건 조회 성공")
    void getSchedule_success() throws Exception {
        // given
        ScheduleResponse mockSchedule = ScheduleResponse.builder()
                .scheduleId(1001L)
                .hospitalName("강남 제모 클리닉")
                .procedureName("겨드랑이 레이저 제모")
                .visitTime(1741680000L)
                .alarmEnabled(true)
                .alarms(List.of(
                        ScheduleAlarmResponse.builder().alarmType("1H").alarmTime(1741676400L).isSent(false).build(),
                        ScheduleAlarmResponse.builder().alarmType("1D").alarmTime(1741593600L).isSent(false).build()
                ))
                .build();

        given(scheduleService.getScheduleById(1001L)).willReturn(mockSchedule);

        // when & then
        mockMvc.perform(get("/api/v1/schedules/1001")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleId").value(1001))
                .andExpect(jsonPath("$.data.hospitalName").value("강남 제모 클리닉"))
                .andExpect(jsonPath("$.data.procedureName").value("겨드랑이 레이저 제모"))
                .andExpect(jsonPath("$.data.alarmEnabled").value(true))
                .andExpect(jsonPath("$.data.alarms").isArray())
                .andExpect(jsonPath("$.data.alarms.length()").value(2))
                .andExpect(jsonPath("$.data.alarms[0].alarmType").value("1H"))
                .andExpect(jsonPath("$.data.alarms[1].alarmType").value("1D"));
    }

    @Test
    @DisplayName("GET /api/v1/schedules/{scheduleId} - 알림 없는 일정 단건 조회")
    void getSchedule_success_noAlarms() throws Exception {
        // given
        ScheduleResponse mockSchedule = ScheduleResponse.builder()
                .scheduleId(1002L)
                .hospitalName("홍대 스킨케어")
                .visitTime(1741766400L)
                .alarmEnabled(true)
                .alarms(List.of())
                .build();

        given(scheduleService.getScheduleById(1002L)).willReturn(mockSchedule);

        // when & then
        mockMvc.perform(get("/api/v1/schedules/1002")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleId").value(1002))
                .andExpect(jsonPath("$.data.procedureName").doesNotExist())
                .andExpect(jsonPath("$.data.alarms").isArray())
                .andExpect(jsonPath("$.data.alarms").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/schedules/{scheduleId} - 존재하지 않는 ID 요청 시 404 반환")
    void getSchedule_notFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(scheduleService).getScheduleById(999L);

        // when & then
        mockMvc.perform(get("/api/v1/schedules/999")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S001"))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    // =============================================
    // GET /api/v1/schedules/upcoming 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/schedules/upcoming - 예정 일정 N건 정상 반환")
    void getUpcomingSchedules_success() throws Exception {
        // given
        List<ScheduleResponse> mockSchedules = List.of(
                ScheduleResponse.builder()
                        .scheduleId(1001L)
                        .hospitalName("강남 제모 클리닉")
                        .procedureName("겨드랑이 레이저 제모")
                        .visitTime(1741680000L)
                        .alarmEnabled(true)
                        .alarms(List.of(
                                ScheduleAlarmResponse.builder().alarmType("1H").alarmTime(1741676400L).isSent(false).build()
                        ))
                        .build(),
                ScheduleResponse.builder()
                        .scheduleId(1002L)
                        .hospitalName("홍대 스킨케어")
                        .visitTime(1741766400L)
                        .alarmEnabled(false)
                        .alarms(List.of())
                        .build()
        );
        given(scheduleService.getUpcomingSchedules(TEST_USER_ID, 5)).willReturn(mockSchedules);

        // when & then
        mockMvc.perform(get("/api/v1/schedules/upcoming")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("limit", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].scheduleId").value(1001))
                .andExpect(jsonPath("$.data[0].procedureName").value("겨드랑이 레이저 제모"))
                .andExpect(jsonPath("$.data[0].alarms[0].alarmType").value("1H"))
                .andExpect(jsonPath("$.data[0].alarms[0].isSent").value(false))
                .andExpect(jsonPath("$.data[1].procedureName").doesNotExist())
                .andExpect(jsonPath("$.data[1].alarms").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/schedules/upcoming - 예정 일정 없을 때 빈 배열 반환")
    void getUpcomingSchedules_emptyList() throws Exception {
        // given
        given(scheduleService.getUpcomingSchedules(TEST_USER_ID, 5)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/schedules/upcoming")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("limit", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/schedules/upcoming - limit 누락 시 400 반환")
    void getUpcomingSchedules_missingLimit() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/upcoming")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    @Test
    @DisplayName("GET /api/v1/schedules/upcoming - 서비스 RuntimeException 발생 시 500 반환")
    void getUpcomingSchedules_serviceException() throws Exception {
        // given
        willThrow(new RuntimeException("DB error"))
                .given(scheduleService).getUpcomingSchedules(TEST_USER_ID, 5);

        // when & then
        mockMvc.perform(get("/api/v1/schedules/upcoming")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("limit", "5"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C001"));
    }

    // =============================================
    // GET /api/v1/schedules/daily 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/schedules/daily - 정상 조회 성공")
    void getSchedulesByDate_success() throws Exception {
        // given
        List<ScheduleResponse> mockSchedules = List.of(
                ScheduleResponse.builder()
                        .scheduleId(1001L)
                        .hospitalName("강남 제모 클리닉")
                        .procedureName("겨드랑이 레이저 제모")
                        .visitTime(1744077600L)
                        .alarmEnabled(true)
                        .alarms(List.of(
                                ScheduleAlarmResponse.builder().alarmType("1H").alarmTime(1744074000L).isSent(false).build()
                        ))
                        .build()
        );
        given(scheduleService.getSchedulesByDate(TEST_USER_ID, "2026-04-08")).willReturn(mockSchedules);

        // when & then
        mockMvc.perform(get("/api/v1/schedules/daily")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("date", "2026-04-08"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].scheduleId").value(1001))
                .andExpect(jsonPath("$.data[0].hospitalName").value("강남 제모 클리닉"))
                .andExpect(jsonPath("$.data[0].alarms[0].alarmType").value("1H"));
    }

    @Test
    @DisplayName("GET /api/v1/schedules/daily - 해당 날짜 일정 없을 때 빈 배열 반환")
    void getSchedulesByDate_emptyList() throws Exception {
        // given
        given(scheduleService.getSchedulesByDate(TEST_USER_ID, "2026-04-08")).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/schedules/daily")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .param("date", "2026-04-08"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/schedules/daily - date 누락 시 400 반환")
    void getSchedulesByDate_missingDate() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/daily")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    // =============================================
    // PATCH /api/v1/schedules/{scheduleId} 테스트
    // =============================================

    @Test
    @DisplayName("PATCH /api/v1/schedules/{scheduleId} - 수정 성공")
    void updateSchedule_success() throws Exception {
        // given
        ScheduleResponse mockResponse = ScheduleResponse.builder()
                .scheduleId(1001L)
                .hospitalName("수정된 클리닉")
                .procedureName("전신 레이저 제모")
                .visitTime(1741766400L)
                .alarmEnabled(true)
                .alarms(List.of(
                        ScheduleAlarmResponse.builder().alarmType("1H").alarmTime(1741762800L).isSent(false).build()
                ))
                .build();

        given(scheduleService.updateSchedule(eq(1001L), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/1001")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hospitalName": "수정된 클리닉",
                                  "visitTime": 1741766400
                                }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleId").value(1001))
                .andExpect(jsonPath("$.data.hospitalName").value("수정된 클리닉"))
                .andExpect(jsonPath("$.data.alarms.length()").value(1))
                .andExpect(jsonPath("$.data.alarms[0].alarmType").value("1H"));
    }

    @Test
    @DisplayName("PATCH /api/v1/schedules/{scheduleId} - 존재하지 않는 일정 404 반환")
    void updateSchedule_notFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(scheduleService).updateSchedule(eq(999L), any());

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/999")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "hospitalName": "클리닉" }
                                """))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S001"));
    }

    @Test
    @DisplayName("PATCH /api/v1/schedules/{scheduleId} - 수정 필드 없음 400 반환")
    void updateSchedule_noFields() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.INVALID_INPUT))
                .given(scheduleService).updateSchedule(eq(1001L), any());

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/1001")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    // =============================================
    // DELETE /api/v1/schedules/{scheduleId} 테스트
    // =============================================

    @Test
    @DisplayName("DELETE /api/v1/schedules/{scheduleId} - 삭제 성공")
    void deleteSchedule_success() throws Exception {
        // given
        willDoNothing().given(scheduleService).deleteSchedule(1001L);

        // when & then
        mockMvc.perform(delete("/api/v1/schedules/1001")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/schedules/{scheduleId} - 존재하지 않는 일정 404 반환")
    void deleteSchedule_notFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(scheduleService).deleteSchedule(999L);

        // when & then
        mockMvc.perform(delete("/api/v1/schedules/999")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S001"));
    }
}