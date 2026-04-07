package org.dev.hehe.service.schedule;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.schedule.ScheduleCreateRequest;
import org.dev.hehe.dto.schedule.ScheduleCreateResponse;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ScheduleService.createSchedule() 단위 테스트
 * - 알림 등록은 별도 알림 API에서 처리되므로 createSchedule 에서 테스트하지 않음
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService - 일정 생성 테스트")
class ScheduleCreateServiceTest {

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleService scheduleService;

    /** ScheduleCreateRequest 생성 헬퍼 */
    private ScheduleCreateRequest createRequest(Long userId, String hospitalName,
                                                String procedureName, Long visitTime) {
        ScheduleCreateRequest req = new ScheduleCreateRequest();
        ReflectionTestUtils.setField(req, "userId", userId);
        ReflectionTestUtils.setField(req, "hospitalName", hospitalName);
        ReflectionTestUtils.setField(req, "procedureName", procedureName);
        ReflectionTestUtils.setField(req, "visitTime", visitTime);
        return req;
    }

    @Test
    @DisplayName("일정 생성 성공 - scheduleId 반환")
    void createSchedule_success() {
        // given
        ScheduleCreateRequest request = createRequest(1L, "강남 제모 클리닉", null, 1741680000L);

        // when
        ScheduleCreateResponse response = scheduleService.createSchedule(request);

        // then
        assertThat(response.getScheduleId()).isNotNull();
        verify(scheduleMapper).insertSchedule(anyLong(), anyLong(), anyString(), isNull(), anyLong(), anyBoolean());
        // 알림 INSERT 는 호출되지 않아야 함
        verify(scheduleMapper, never()).insertScheduleAlarms(any());
    }

    @Test
    @DisplayName("alarmEnabled 는 항상 true 로 저장")
    void createSchedule_alarmEnabled_alwaysTrue() {
        // given
        ScheduleCreateRequest request = createRequest(1L, "강남 클리닉", null, 1741680000L);

        // when
        scheduleService.createSchedule(request);

        // then: insertSchedule 의 alarmEnabled 파라미터가 항상 true 인지 확인
        ArgumentCaptor<Boolean> alarmEnabledCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(scheduleMapper).insertSchedule(anyLong(), anyLong(), anyString(), isNull(),
                anyLong(), alarmEnabledCaptor.capture());
        assertThat(alarmEnabledCaptor.getValue()).isTrue();
    }

    @Test
    @DisplayName("시술명 포함 일정 생성 성공")
    void createSchedule_success_withProcedureName() {
        // given
        ScheduleCreateRequest request = createRequest(1L, "강남 클리닉", "겨드랑이 레이저 제모", 1741680000L);

        // when
        ScheduleCreateResponse response = scheduleService.createSchedule(request);

        // then
        assertThat(response.getScheduleId()).isNotNull();
        verify(scheduleMapper).insertSchedule(anyLong(), anyLong(), anyString(), anyString(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("userId 누락 시 INVALID_INPUT 예외 발생")
    void createSchedule_fail_missingUserId() {
        ScheduleCreateRequest request = createRequest(null, "강남 클리닉", null, 1741680000L);

        assertThatThrownBy(() -> scheduleService.createSchedule(request))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("hospitalName 누락 시 INVALID_INPUT 예외 발생")
    void createSchedule_fail_missingHospitalName() {
        ScheduleCreateRequest request = createRequest(1L, null, null, 1741680000L);

        assertThatThrownBy(() -> scheduleService.createSchedule(request))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("visitTime 누락 시 INVALID_INPUT 예외 발생")
    void createSchedule_fail_missingVisitTime() {
        ScheduleCreateRequest request = createRequest(1L, "강남 클리닉", null, null);

        assertThatThrownBy(() -> scheduleService.createSchedule(request))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }
}