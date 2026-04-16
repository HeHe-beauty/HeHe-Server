package org.dev.hehe.controller.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.schedule.ScheduleAlarmResponse;
import org.dev.hehe.service.schedule.ScheduleAlarmService;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ScheduleAlarmController 단위 테스트
 * - @WebMvcTest: Controller 레이어만 로드 (Service는 Mock 처리)
 * - JWT 인증: @MockitoBean JwtProvider + Authorization 헤더
 */
@WebMvcTest(ScheduleAlarmController.class)
@Import(SecurityConfig.class)
@DisplayName("ScheduleAlarmController 테스트")
class ScheduleAlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleAlarmService scheduleAlarmService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String TEST_TOKEN = "test-token";

    @BeforeEach
    void setup() {
        given(jwtProvider.getUserIdFromToken(TEST_TOKEN)).willReturn(1L);
    }

    // =============================================
    // POST /api/v1/schedules/{scheduleId}/alarms 테스트
    // =============================================

    @Test
    @DisplayName("POST /api/v1/schedules/{scheduleId}/alarms - 알림 등록 성공 (201)")
    void addAlarm_success() throws Exception {
        // given
        long scheduleId = 1001L;
        ScheduleAlarmResponse mockResponse = ScheduleAlarmResponse.builder()
                .alarmType("1H")
                .alarmTime(1741676400L)
                .isSent(false)
                .build();

        given(scheduleAlarmService.addAlarm(scheduleId, "1H")).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/alarms", scheduleId)
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("alarmType", "1H"))))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.alarmType").value("1H"))
                .andExpect(jsonPath("$.data.alarmTime").value(1741676400L))
                .andExpect(jsonPath("$.data.isSent").value(false));
    }

    @Test
    @DisplayName("POST - 이미 등록된 alarmType → 409 반환")
    void addAlarm_fail_alreadyExists() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.ALARM_ALREADY_EXISTS))
                .given(scheduleAlarmService).addAlarm(1001L, "1H");

        // when & then
        mockMvc.perform(post("/api/v1/schedules/1001/alarms")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("alarmType", "1H"))))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S002"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALARM_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("POST - 존재하지 않는 scheduleId → 404 반환")
    void addAlarm_fail_scheduleNotFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(scheduleAlarmService).addAlarm(999L, "1D");

        // when & then
        mockMvc.perform(post("/api/v1/schedules/999/alarms")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("alarmType", "1D"))))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S001"));
    }

    @Test
    @DisplayName("POST - 지원하지 않는 alarmType → 400 반환")
    void addAlarm_fail_invalidAlarmType() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.INVALID_INPUT))
                .given(scheduleAlarmService).addAlarm(1001L, "2H");

        // when & then
        mockMvc.perform(post("/api/v1/schedules/1001/alarms")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("alarmType", "2H"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    // =============================================
    // DELETE /api/v1/schedules/{scheduleId}/alarms/{alarmType} 테스트
    // =============================================

    @Test
    @DisplayName("DELETE /api/v1/schedules/{scheduleId}/alarms/{alarmType} - 알림 삭제 성공 (200)")
    void removeAlarm_success() throws Exception {
        // given
        willDoNothing().given(scheduleAlarmService).removeAlarm(1001L, "1D");

        // when & then
        mockMvc.perform(delete("/api/v1/schedules/1001/alarms/1D")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("DELETE - 등록되지 않은 alarmType → 404 반환")
    void removeAlarm_fail_alarmNotFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.ALARM_NOT_FOUND))
                .given(scheduleAlarmService).removeAlarm(1001L, "3D");

        // when & then
        mockMvc.perform(delete("/api/v1/schedules/1001/alarms/3D")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S003"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALARM_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("DELETE - 존재하지 않는 scheduleId → 404 반환")
    void removeAlarm_fail_scheduleNotFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(scheduleAlarmService).removeAlarm(999L, "1H");

        // when & then
        mockMvc.perform(delete("/api/v1/schedules/999/alarms/1H")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S001"));
    }

    @Test
    @DisplayName("DELETE - 지원하지 않는 alarmType → 400 반환")
    void removeAlarm_fail_invalidAlarmType() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.INVALID_INPUT))
                .given(scheduleAlarmService).removeAlarm(1001L, "INVALID");

        // when & then
        mockMvc.perform(delete("/api/v1/schedules/1001/alarms/INVALID")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }
}