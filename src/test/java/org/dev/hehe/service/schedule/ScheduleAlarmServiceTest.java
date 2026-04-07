package org.dev.hehe.service.schedule;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.schedule.Schedule;
import org.dev.hehe.dto.schedule.ScheduleAlarmResponse;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ScheduleAlarmService 단위 테스트
 * - addAlarm / removeAlarm 비즈니스 로직 검증
 * - 공통 로직(validateAlarmType, findScheduleOrThrow, resolveAlarmTime) 포함
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleAlarmService 테스트")
class ScheduleAlarmServiceTest {

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleAlarmService scheduleAlarmService;

    /** 테스트용 Schedule 도메인 객체 생성 헬퍼 */
    private Schedule createSchedule(Long scheduleId, Long visitTime) {
        Schedule schedule = new Schedule();
        ReflectionTestUtils.setField(schedule, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(schedule, "visitTime", visitTime);
        return schedule;
    }

    // =============================================
    // addAlarm 테스트
    // =============================================

    @Test
    @DisplayName("알림 등록 성공 - 1H")
    void addAlarm_success_1H() {
        // given
        long scheduleId = 1001L;
        long visitTime = 1741680000L;
        Schedule schedule = createSchedule(scheduleId, visitTime);

        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));
        given(scheduleMapper.insertScheduleAlarmIfNotExists(eq(scheduleId), eq("1H"), anyLong())).willReturn(1);

        // when
        ScheduleAlarmResponse result = scheduleAlarmService.addAlarm(scheduleId, "1H");

        // then
        assertThat(result.getAlarmType()).isEqualTo("1H");
        assertThat(result.getAlarmTime()).isEqualTo(visitTime - 3_600);
        assertThat(result.getIsSent()).isFalse();
    }

    @Test
    @DisplayName("알림 등록 성공 - 3D: visitTime - 259200초")
    void addAlarm_success_3D() {
        // given
        long scheduleId = 1001L;
        long visitTime = 1741680000L;
        Schedule schedule = createSchedule(scheduleId, visitTime);

        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));
        given(scheduleMapper.insertScheduleAlarmIfNotExists(eq(scheduleId), eq("3D"), anyLong())).willReturn(1);

        // when
        ScheduleAlarmResponse result = scheduleAlarmService.addAlarm(scheduleId, "3D");

        // then
        assertThat(result.getAlarmTime()).isEqualTo(visitTime - 259_200);
    }

    @Test
    @DisplayName("알림 등록 실패 - 이미 존재하는 alarmType → ALARM_ALREADY_EXISTS(S002)")
    void addAlarm_fail_alreadyExists() {
        // given: INSERT가 0을 반환 (already exists)
        long scheduleId = 1001L;
        Schedule schedule = createSchedule(scheduleId, 1741680000L);

        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));
        given(scheduleMapper.insertScheduleAlarmIfNotExists(anyLong(), anyString(), anyLong())).willReturn(0);

        // when & then
        assertThatThrownBy(() -> scheduleAlarmService.addAlarm(scheduleId, "1H"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ALARM_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("알림 등록 실패 - 존재하지 않는 scheduleId → SCHEDULE_NOT_FOUND(S001)")
    void addAlarm_fail_scheduleNotFound() {
        // given
        given(scheduleMapper.findScheduleById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleAlarmService.addAlarm(999L, "1H"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND));

        // 스케줄 없으면 INSERT 호출하지 않음
        verify(scheduleMapper, never()).insertScheduleAlarmIfNotExists(anyLong(), anyString(), anyLong());
    }

    @Test
    @DisplayName("알림 등록 실패 - 지원하지 않는 alarmType → INVALID_INPUT(C002)")
    void addAlarm_fail_invalidAlarmType() {
        // when & then: validateAlarmType에서 즉시 예외, DB 조회 없음
        assertThatThrownBy(() -> scheduleAlarmService.addAlarm(1001L, "2H"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));

        verify(scheduleMapper, never()).findScheduleById(anyLong());
    }

    // =============================================
    // removeAlarm 테스트
    // =============================================

    @Test
    @DisplayName("알림 삭제 성공")
    void removeAlarm_success() {
        // given
        long scheduleId = 1001L;
        Schedule schedule = createSchedule(scheduleId, 1741680000L);

        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));
        given(scheduleMapper.deleteScheduleAlarm(scheduleId, "1D")).willReturn(1);

        // when & then: 예외 없이 정상 완료
        scheduleAlarmService.removeAlarm(scheduleId, "1D");

        verify(scheduleMapper).deleteScheduleAlarm(scheduleId, "1D");
    }

    @Test
    @DisplayName("알림 삭제 실패 - 등록되지 않은 alarmType → ALARM_NOT_FOUND(S003)")
    void removeAlarm_fail_alarmNotFound() {
        // given: DELETE가 0을 반환 (not found)
        long scheduleId = 1001L;
        Schedule schedule = createSchedule(scheduleId, 1741680000L);

        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));
        given(scheduleMapper.deleteScheduleAlarm(scheduleId, "3D")).willReturn(0);

        // when & then
        assertThatThrownBy(() -> scheduleAlarmService.removeAlarm(scheduleId, "3D"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ALARM_NOT_FOUND));
    }

    @Test
    @DisplayName("알림 삭제 실패 - 존재하지 않는 scheduleId → SCHEDULE_NOT_FOUND(S001)")
    void removeAlarm_fail_scheduleNotFound() {
        // given
        given(scheduleMapper.findScheduleById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleAlarmService.removeAlarm(999L, "1H"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND));

        verify(scheduleMapper, never()).deleteScheduleAlarm(anyLong(), anyString());
    }

    @Test
    @DisplayName("알림 삭제 실패 - 지원하지 않는 alarmType → INVALID_INPUT(C002)")
    void removeAlarm_fail_invalidAlarmType() {
        assertThatThrownBy(() -> scheduleAlarmService.removeAlarm(1001L, "INVALID"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));

        verify(scheduleMapper, never()).findScheduleById(anyLong());
    }

    // =============================================
    // resolveAlarmTime 테스트
    // =============================================

    @Test
    @DisplayName("resolveAlarmTime - 1H: visitTime - 3600")
    void resolveAlarmTime_1H() {
        assertThat(scheduleAlarmService.resolveAlarmTime("1H", 1741680000L))
                .isEqualTo(1741680000L - 3_600);
    }

    @Test
    @DisplayName("resolveAlarmTime - 1D: visitTime - 86400")
    void resolveAlarmTime_1D() {
        assertThat(scheduleAlarmService.resolveAlarmTime("1D", 1741680000L))
                .isEqualTo(1741680000L - 86_400);
    }

    @Test
    @DisplayName("resolveAlarmTime - 3D: visitTime - 259200")
    void resolveAlarmTime_3D() {
        assertThat(scheduleAlarmService.resolveAlarmTime("3D", 1741680000L))
                .isEqualTo(1741680000L - 259_200);
    }
}