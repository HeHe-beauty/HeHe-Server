package org.dev.hehe.service.schedule;

import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.domain.schedule.Schedule;
import org.dev.hehe.domain.schedule.ScheduleAlarm;
import org.dev.hehe.dto.schedule.ScheduleDateCountDto;
import org.dev.hehe.dto.schedule.ScheduleResponse;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.schedule.ScheduleUpdateRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * ScheduleService 단위 테스트
 * - Spring Context 없이 Mockito로 Mapper를 Mock 처리하여 Service 비즈니스 로직만 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService 테스트")
@Slf4j
class ScheduleServiceTest {

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleService scheduleService;

    /**
     * 테스트용 Schedule 도메인 객체 생성 헬퍼
     * MyBatis는 리플렉션으로 필드를 주입하므로 ReflectionTestUtils 사용
     */
    private Schedule createSchedule(Long scheduleId, String hospitalName, String procedureName,
                                    Long visitTime, Boolean alarmEnabled) {
        Schedule schedule = new Schedule();
        ReflectionTestUtils.setField(schedule, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(schedule, "userId", 1L);
        ReflectionTestUtils.setField(schedule, "hospitalName", hospitalName);
        ReflectionTestUtils.setField(schedule, "procedureName", procedureName);
        ReflectionTestUtils.setField(schedule, "visitTime", visitTime);
        ReflectionTestUtils.setField(schedule, "alarmEnabled", alarmEnabled);
        return schedule;
    }

    /**
     * 테스트용 ScheduleAlarm 도메인 객체 생성 헬퍼
     */
    private ScheduleAlarm createAlarm(Long scheduleId, String alarmType, Long alarmTime, Boolean isSent) {
        ScheduleAlarm alarm = new ScheduleAlarm();
        ReflectionTestUtils.setField(alarm, "scheduleId", scheduleId);
        ReflectionTestUtils.setField(alarm, "alarmType", alarmType);
        ReflectionTestUtils.setField(alarm, "alarmTime", alarmTime);
        ReflectionTestUtils.setField(alarm, "isSent", isSent);
        return alarm;
    }

    @Test
    @DisplayName("예정 일정 N건 조회 성공 - 알림 포함")
    void getUpcomingSchedules_success_withAlarms() {
        // given
        long visitTime = ZoneId.systemDefault().getRules().getOffset(java.time.Instant.now()).getTotalSeconds()
                + System.currentTimeMillis() / 1000 + 3600;
        Schedule schedule = createSchedule(1001L, "강남 제모 클리닉", "겨드랑이 레이저 제모", visitTime, true);

        ScheduleAlarm alarm1 = createAlarm(1001L, "1H", visitTime - 3600, false);
        ScheduleAlarm alarm2 = createAlarm(1001L, "1D", visitTime - 86400, false);

        given(scheduleMapper.findUpcomingSchedulesByUserId(eq(1L), anyLong(), eq(5)))
                .willReturn(List.of(schedule));
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(1001L)))
                .willReturn(List.of(alarm1, alarm2));

        // when
        List<ScheduleResponse> result = scheduleService.getUpcomingSchedules(1L, 5);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduleId()).isEqualTo(1001L);
        assertThat(result.get(0).getHospitalName()).isEqualTo("강남 제모 클리닉");
        assertThat(result.get(0).getProcedureName()).isEqualTo("겨드랑이 레이저 제모");
        assertThat(result.get(0).getAlarms()).hasSize(2);
        assertThat(result.get(0).getAlarms().get(0).getAlarmType()).isEqualTo("1H");
        assertThat(result.get(0).getAlarms().get(1).getAlarmType()).isEqualTo("1D");
    }

    @Test
    @DisplayName("예정 일정 N건 조회 성공 - 알림 없는 일정 (빈 리스트)")
    void getUpcomingSchedules_success_noAlarms() {
        // given
        long visitTime = System.currentTimeMillis() / 1000 + 7200;
        Schedule schedule = createSchedule(1002L, "홍대 스킨케어", null, visitTime, false);

        given(scheduleMapper.findUpcomingSchedulesByUserId(eq(1L), anyLong(), eq(3)))
                .willReturn(List.of(schedule));
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(1002L)))
                .willReturn(List.of());

        // when
        List<ScheduleResponse> result = scheduleService.getUpcomingSchedules(1L, 3);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProcedureName()).isNull();
        assertThat(result.get(0).getAlarms()).isEmpty();
    }

    @Test
    @DisplayName("예정 일정 없을 때 빈 리스트 반환 — findAlarmsByScheduleIds 호출 없음")
    void getUpcomingSchedules_emptySchedules_noAlarmQuery() {
        // given
        given(scheduleMapper.findUpcomingSchedulesByUserId(eq(1L), anyLong(), eq(5)))
                .willReturn(List.of());

        // when
        List<ScheduleResponse> result = scheduleService.getUpcomingSchedules(1L, 5);

        // then: 일정이 없으면 알림 쿼리는 실행되면 안 됨 (IN () 방지)
        assertThat(result).isEmpty();
        verify(scheduleMapper).findUpcomingSchedulesByUserId(eq(1L), anyLong(), eq(5));
        verifyNoMoreInteractions(scheduleMapper);
    }

    @Test
    @DisplayName("복수 일정 조회 시 각 일정의 알림이 올바르게 매핑되는지 검증")
    void getUpcomingSchedules_multipleSchedules_alarmGroupingCorrect() {
        // given: 일정 2개, 각각 알림 다르게 설정
        long base = System.currentTimeMillis() / 1000;
        Schedule s1 = createSchedule(1001L, "강남 클리닉", null, base + 3600, true);
        Schedule s2 = createSchedule(1002L, "홍대 클리닉", null, base + 7200, true);

        ScheduleAlarm a1 = createAlarm(1001L, "1H", base, false);
        ScheduleAlarm a2 = createAlarm(1002L, "3D", base, false);

        given(scheduleMapper.findUpcomingSchedulesByUserId(eq(1L), anyLong(), eq(5)))
                .willReturn(List.of(s1, s2));
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(1001L, 1002L)))
                .willReturn(List.of(a1, a2));

        // when
        List<ScheduleResponse> result = scheduleService.getUpcomingSchedules(1L, 5);

        // then: 각 일정에 해당하는 알림만 포함되는지 확인
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAlarms()).hasSize(1);
        assertThat(result.get(0).getAlarms().get(0).getAlarmType()).isEqualTo("1H");
        assertThat(result.get(1).getAlarms()).hasSize(1);
        assertThat(result.get(1).getAlarms().get(0).getAlarmType()).isEqualTo("3D");
    }

    @Test
    @DisplayName("nowTime 이 현재 시각으로 전달되는지 검증")
    void getUpcomingSchedules_verifyNowTime() {
        // given
        long beforeCall = System.currentTimeMillis() / 1000;

        given(scheduleMapper.findUpcomingSchedulesByUserId(anyLong(), anyLong(), anyInt()))
                .willReturn(List.of());

        // when
        scheduleService.getUpcomingSchedules(1L, 5);

        long afterCall = System.currentTimeMillis() / 1000;

        // then: nowTime이 호출 전후 사이의 값인지 확인
        ArgumentCaptor<Long> nowCaptor = ArgumentCaptor.forClass(Long.class);
        verify(scheduleMapper).findUpcomingSchedulesByUserId(eq(1L), nowCaptor.capture(), eq(5));

        assertThat(nowCaptor.getValue()).isBetween(beforeCall, afterCall + 1);
    }

    @Test
    @DisplayName("Mapper에서 RuntimeException 발생 시 예외 전파")
    void getUpcomingSchedules_mapperThrowsException() {
        // given
        willThrow(new RuntimeException("DB connection error"))
                .given(scheduleMapper).findUpcomingSchedulesByUserId(anyLong(), anyLong(), anyInt());

        // when & then
        assertThatThrownBy(() -> scheduleService.getUpcomingSchedules(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB connection error");
    }

    // =============================================
    // getScheduleById 테스트
    // =============================================

    @Test
    @DisplayName("일정 단건 조회 성공 - 알림 포함")
    void getScheduleById_success_withAlarms() {
        // given
        long scheduleId = 1001L;
        long visitTime = 1741680000L;
        Schedule schedule = createSchedule(scheduleId, "강남 제모 클리닉", "겨드랑이 레이저 제모", visitTime, true);

        ScheduleAlarm alarm1 = createAlarm(scheduleId, "1H", visitTime - 3_600, false);
        ScheduleAlarm alarm2 = createAlarm(scheduleId, "1D", visitTime - 86_400, false);

        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(scheduleId))).willReturn(List.of(alarm1, alarm2));

        // when
        ScheduleResponse result = scheduleService.getScheduleById(scheduleId);

        // then
        assertThat(result.getScheduleId()).isEqualTo(scheduleId);
        assertThat(result.getHospitalName()).isEqualTo("강남 제모 클리닉");
        assertThat(result.getProcedureName()).isEqualTo("겨드랑이 레이저 제모");
        assertThat(result.getAlarmEnabled()).isTrue();
        assertThat(result.getAlarms()).hasSize(2);
        assertThat(result.getAlarms().get(0).getAlarmType()).isEqualTo("1H");
        assertThat(result.getAlarms().get(1).getAlarmType()).isEqualTo("1D");
    }

    @Test
    @DisplayName("일정 단건 조회 성공 - 알림 없음 (빈 리스트)")
    void getScheduleById_success_noAlarms() {
        // given
        long scheduleId = 1002L;
        Schedule schedule = createSchedule(scheduleId, "홍대 스킨케어", null, 1741766400L, true);

        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(scheduleId))).willReturn(List.of());

        // when
        ScheduleResponse result = scheduleService.getScheduleById(scheduleId);

        // then
        assertThat(result.getScheduleId()).isEqualTo(scheduleId);
        assertThat(result.getProcedureName()).isNull();
        assertThat(result.getAlarms()).isEmpty();
    }

    // =============================================
    // updateSchedule 테스트
    // =============================================

    /**
     * 테스트용 ScheduleUpdateRequest 생성 헬퍼
     */
    private ScheduleUpdateRequest createUpdateRequest(String hospitalName, String procedureName, Long visitTime) {
        ScheduleUpdateRequest request = new ScheduleUpdateRequest();
        ReflectionTestUtils.setField(request, "hospitalName", hospitalName);
        ReflectionTestUtils.setField(request, "procedureName", procedureName);
        ReflectionTestUtils.setField(request, "visitTime", visitTime);
        return request;
    }

    @Test
    @DisplayName("일정 수정 성공 - hospitalName 만 변경")
    void updateSchedule_success_hospitalNameOnly() {
        // given
        long scheduleId = 1001L;
        long visitTime = 1741680000L;
        Schedule existing = createSchedule(scheduleId, "구 클리닉", "제모", visitTime, true);
        Schedule updated  = createSchedule(scheduleId, "신 클리닉", "제모", visitTime, true);
        ScheduleUpdateRequest request = createUpdateRequest("신 클리닉", null, null);

        given(scheduleMapper.findScheduleById(scheduleId))
                .willReturn(Optional.of(existing))   // 존재 확인
                .willReturn(Optional.of(updated));    // 수정 후 재조회
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(scheduleId))).willReturn(List.of());

        // when
        ScheduleResponse result = scheduleService.updateSchedule(scheduleId, request);

        // then
        assertThat(result.getHospitalName()).isEqualTo("신 클리닉");
        verify(scheduleMapper).updateSchedule(eq(scheduleId), eq("신 클리닉"), isNull(), isNull());
    }

    @Test
    @DisplayName("일정 수정 성공 - 모든 필드 변경")
    void updateSchedule_success_allFields() {
        // given
        long scheduleId = 1001L;
        Schedule existing = createSchedule(scheduleId, "구 클리닉", "구 시술", 1741680000L, true);
        Schedule updated  = createSchedule(scheduleId, "신 클리닉", "신 시술", 1741766400L, true);
        ScheduleUpdateRequest request = createUpdateRequest("신 클리닉", "신 시술", 1741766400L);

        given(scheduleMapper.findScheduleById(scheduleId))
                .willReturn(Optional.of(existing))
                .willReturn(Optional.of(updated));
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(scheduleId))).willReturn(List.of());

        // when
        ScheduleResponse result = scheduleService.updateSchedule(scheduleId, request);

        // then
        assertThat(result.getHospitalName()).isEqualTo("신 클리닉");
        assertThat(result.getProcedureName()).isEqualTo("신 시술");
        assertThat(result.getVisitTime()).isEqualTo(1741766400L);
        verify(scheduleMapper).updateSchedule(eq(scheduleId), eq("신 클리닉"), eq("신 시술"), eq(1741766400L));
    }

    @Test
    @DisplayName("일정 수정 실패 - 존재하지 않는 scheduleId → S001")
    void updateSchedule_notFound() {
        // given
        ScheduleUpdateRequest request = createUpdateRequest("클리닉", null, null);
        given(scheduleMapper.findScheduleById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(999L, request))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND));

        verify(scheduleMapper, never()).updateSchedule(anyLong(), anyString(), any(), any());
    }

    @Test
    @DisplayName("일정 수정 실패 - 모든 필드 null → INVALID_INPUT")
    void updateSchedule_allFieldsNull() {
        // given
        ScheduleUpdateRequest request = createUpdateRequest(null, null, null);

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(1001L, request))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));

        // 검증 실패이므로 mapper 호출 없어야 함
        verifyNoMoreInteractions(scheduleMapper);
    }

    @Test
    @DisplayName("일정 수정 실패 - hospitalName 공백 → INVALID_INPUT")
    void updateSchedule_blankHospitalName() {
        // given
        ScheduleUpdateRequest request = createUpdateRequest("   ", null, null);

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(1001L, request))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));

        verifyNoMoreInteractions(scheduleMapper);
    }

    // =============================================
    // deleteSchedule 테스트
    // =============================================

    @Test
    @DisplayName("일정 삭제 성공 - 알림 먼저 삭제 후 일정 삭제 순서 검증")
    void deleteSchedule_success() {
        // given
        long scheduleId = 1001L;
        Schedule existing = createSchedule(scheduleId, "강남 클리닉", null, 1741680000L, true);
        given(scheduleMapper.findScheduleById(scheduleId)).willReturn(Optional.of(existing));

        // when
        scheduleService.deleteSchedule(scheduleId);

        // then: 알림 전체 삭제 → 일정 삭제 순서 보장
        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(scheduleMapper);
        inOrder.verify(scheduleMapper).findScheduleById(scheduleId);
        inOrder.verify(scheduleMapper).deleteAllAlarmsByScheduleId(scheduleId);
        inOrder.verify(scheduleMapper).deleteSchedule(scheduleId);
    }

    @Test
    @DisplayName("일정 삭제 실패 - 존재하지 않는 scheduleId → S001")
    void deleteSchedule_notFound() {
        // given
        given(scheduleMapper.findScheduleById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(999L))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND));

        verify(scheduleMapper).findScheduleById(999L);
        verifyNoMoreInteractions(scheduleMapper);
    }

    // =============================================
    // getSchedulesByDate 테스트
    // =============================================

    @Test
    @DisplayName("날짜별 일정 조회 성공 - 알림 포함")
    void getSchedulesByDate_success_withAlarms() {
        // given
        String date = "2026-04-08";
        long dayStart = LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long visitTime = dayStart + 3600;

        Schedule schedule = createSchedule(1001L, "강남 제모 클리닉", "겨드랑이 레이저 제모", visitTime, true);
        ScheduleAlarm alarm = createAlarm(1001L, "1H", visitTime - 3600, false);

        given(scheduleMapper.findSchedulesByUserIdAndPeriod(eq(1L), anyLong(), anyLong()))
                .willReturn(List.of(schedule));
        given(scheduleMapper.findAlarmsByScheduleIds(List.of(1001L)))
                .willReturn(List.of(alarm));

        // when
        List<ScheduleResponse> result = scheduleService.getSchedulesByDate(1L, date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduleId()).isEqualTo(1001L);
        assertThat(result.get(0).getHospitalName()).isEqualTo("강남 제모 클리닉");
        assertThat(result.get(0).getAlarms()).hasSize(1);
        assertThat(result.get(0).getAlarms().get(0).getAlarmType()).isEqualTo("1H");
    }

    @Test
    @DisplayName("날짜별 일정 조회 성공 - 해당 날짜 일정 없으면 빈 리스트 반환")
    void getSchedulesByDate_empty() {
        // given
        given(scheduleMapper.findSchedulesByUserIdAndPeriod(eq(1L), anyLong(), anyLong()))
                .willReturn(List.of());

        // when
        List<ScheduleResponse> result = scheduleService.getSchedulesByDate(1L, "2026-04-08");

        // then: 일정이 없으면 알림 쿼리 미호출
        assertThat(result).isEmpty();
        verify(scheduleMapper).findSchedulesByUserIdAndPeriod(eq(1L), anyLong(), anyLong());
        verifyNoMoreInteractions(scheduleMapper);
    }

    @Test
    @DisplayName("날짜별 일정 조회 - 조회 범위가 해당 날짜 00:00:00 ~ 다음 날 00:00:00 인지 검증")
    void getSchedulesByDate_verifyTimeRange() {
        // given
        String date = "2026-04-08";
        long expectedStart = LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long expectedEnd   = LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).plusDays(1).toEpochSecond();

        given(scheduleMapper.findSchedulesByUserIdAndPeriod(anyLong(), anyLong(), anyLong()))
                .willReturn(List.of());

        // when
        scheduleService.getSchedulesByDate(1L, date);

        // then
        ArgumentCaptor<Long> startCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> endCaptor   = ArgumentCaptor.forClass(Long.class);
        verify(scheduleMapper).findSchedulesByUserIdAndPeriod(eq(1L), startCaptor.capture(), endCaptor.capture());

        assertThat(startCaptor.getValue()).isEqualTo(expectedStart);
        assertThat(endCaptor.getValue()).isEqualTo(expectedEnd);
    }

    @Test
    @DisplayName("날짜별 일정 조회 - 잘못된 날짜 형식이면 INVALID_INPUT 예외 발생")
    void getSchedulesByDate_invalidDateFormat() {
        // when & then
        assertThatThrownBy(() -> scheduleService.getSchedulesByDate(1L, "2026/04/08"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));

        verifyNoMoreInteractions(scheduleMapper);
    }

    // =============================================
    // getScheduleSummary 테스트
    // =============================================

    /**
     * 테스트용 ScheduleDateCountDto 생성 헬퍼
     */
    private ScheduleDateCountDto createDateCountDto(String date, int count) {
        ScheduleDateCountDto dto = new ScheduleDateCountDto();
        ReflectionTestUtils.setField(dto, "date", date);
        ReflectionTestUtils.setField(dto, "count", count);
        return dto;
    }

    @Test
    @DisplayName("전체 일정 요약 조회 성공 - 날짜별 건수 Map 반환")
    void getScheduleSummary_success() {
        // given
        List<ScheduleDateCountDto> mockResult = List.of(
                createDateCountDto("2026-04-15", 2),
                createDateCountDto("2026-04-20", 1),
                createDateCountDto("2026-05-03", 3)
        );
        given(scheduleMapper.findScheduleCountGroupByDate(1L)).willReturn(mockResult);

        // when
        Map<String, Integer> result = scheduleService.getScheduleSummary(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get("2026-04-15")).isEqualTo(2);
        assertThat(result.get("2026-04-20")).isEqualTo(1);
        assertThat(result.get("2026-05-03")).isEqualTo(3);
    }

    @Test
    @DisplayName("전체 일정 요약 조회 - 일정 없으면 빈 Map 반환")
    void getScheduleSummary_empty() {
        // given
        given(scheduleMapper.findScheduleCountGroupByDate(1L)).willReturn(List.of());

        // when
        Map<String, Integer> result = scheduleService.getScheduleSummary(1L);

        // then
        assertThat(result).isEmpty();
        verify(scheduleMapper).findScheduleCountGroupByDate(1L);
    }

    @Test
    @DisplayName("전체 일정 요약 조회 - 날짜 오름차순 정렬 유지 검증")
    void getScheduleSummary_orderByDateAsc() {
        // given
        List<ScheduleDateCountDto> mockResult = List.of(
                createDateCountDto("2026-04-01", 1),
                createDateCountDto("2026-04-15", 2),
                createDateCountDto("2026-05-01", 3)
        );
        given(scheduleMapper.findScheduleCountGroupByDate(1L)).willReturn(mockResult);

        // when
        Map<String, Integer> result = scheduleService.getScheduleSummary(1L);

        // then: LinkedHashMap이므로 삽입 순서(= 날짜 오름차순) 유지
        List<String> keys = List.copyOf(result.keySet());
        assertThat(keys).containsExactly("2026-04-01", "2026-04-15", "2026-05-01");
    }

    @Test
    @DisplayName("존재하지 않는 scheduleId 조회 시 SCHEDULE_NOT_FOUND 예외 발생")
    void getScheduleById_notFound() {
        // given: mapper가 빈 Optional 반환
        given(scheduleMapper.findScheduleById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.getScheduleById(999L))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> {
                    CommonException ce = (CommonException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
                    assertThat(ce.getErrorCode().getCode()).isEqualTo("S001");
                });

        // 일정이 없으면 알림 쿼리는 호출되면 안 됨
        verify(scheduleMapper).findScheduleById(999L);
        verifyNoMoreInteractions(scheduleMapper);
    }
}