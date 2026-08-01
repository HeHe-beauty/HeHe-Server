package org.dev.hehe.service.fcm;

import org.dev.hehe.dto.fcm.PendingAlarmDto;
import org.dev.hehe.mapper.fcm.FcmMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FcmSchedulerTest {

    @InjectMocks
    private FcmScheduler fcmScheduler;

    @Mock
    private FcmMapper fcmMapper;

    @Mock
    private FcmSendService fcmSendService;

    @Test
    @DisplayName("발송 대상 알람이 없으면 아무 작업도 하지 않는다")
    void sendScheduledAlarms_noPendingAlarms_doesNothing() {
        given(fcmMapper.findPendingAlarms()).willReturn(List.of());

        fcmScheduler.sendScheduledAlarms();

        verify(fcmMapper, never()).findActiveTokensByUserId(any());
        verifyNoInteractions(fcmSendService);
    }

    @Test
    @DisplayName("활성 FCM 토큰이 없으면 발송 없이 다음 주기에 재시도한다")
    void sendScheduledAlarms_noActiveTokens_doesNotMarkAsSent() {
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원");
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of());

        fcmScheduler.sendScheduledAlarms();

        verify(fcmMapper, never()).markAlarmAsSent(any());
        verifyNoInteractions(fcmSendService);
        verify(fcmMapper, never()).insertPushHistory(any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("1H 알람 발송 성공 시 알람 완료 처리 및 이력을 저장한다")
    void sendScheduledAlarms_1hAlarm_sendsCorrectBodyAndSavesHistory() {
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원");
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1", "token2"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(2);

        fcmScheduler.sendScheduledAlarms();

        verify(fcmSendService).sendMulticast(
                eq(List.of("token1", "token2")),
                eq("[히히] 예약 알림"),
                contains("1시간")
        );
        verify(fcmMapper).markAlarmAsSent(1L);
        verify(fcmMapper).insertPushHistory(eq(10L), eq(1L), anyString(), anyString(), eq(2), eq(0));
    }

    @Test
    @DisplayName("1D 알람 발송 시 '하루 전' 본문을 전송한다")
    void sendScheduledAlarms_1dAlarm_sendsCorrectBody() {
        PendingAlarmDto alarm = createAlarm(2L, "1D", 10L, "헤헤병원");
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(1);

        fcmScheduler.sendScheduledAlarms();

        verify(fcmSendService).sendMulticast(any(), anyString(), contains("하루 전"));
    }

    @Test
    @DisplayName("3D 알람 발송 시 '3일 전' 본문을 전송한다")
    void sendScheduledAlarms_3dAlarm_sendsCorrectBody() {
        PendingAlarmDto alarm = createAlarm(3L, "3D", 10L, "헤헤병원");
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(1);

        fcmScheduler.sendScheduledAlarms();

        verify(fcmSendService).sendMulticast(any(), anyString(), contains("3일 전"));
    }

    @Test
    @DisplayName("발송 일부 실패 시 성공/실패 건수를 정확히 이력에 저장한다")
    void sendScheduledAlarms_partialFailure_savesCorrectCounts() {
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원");
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1", "token2", "token3"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(2);  // 3개 중 2개 성공

        fcmScheduler.sendScheduledAlarms();

        verify(fcmMapper).insertPushHistory(eq(10L), eq(1L), anyString(), anyString(), eq(2), eq(1));
    }

    @Test
    @DisplayName("alarm_time이 30분 이상 지난 알람은 발송 포기 후 완료 처리한다")
    void sendScheduledAlarms_expiredAlarm_marksAsSentWithoutSending() {
        long expiredAlarmTime = System.currentTimeMillis() / 1000 - (31 * 60); // 31분 전
        PendingAlarmDto alarm = createAlarmWithTime(1L, "1H", 10L, "헤헤병원", expiredAlarmTime);
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));

        fcmScheduler.sendScheduledAlarms();

        verify(fcmMapper).markAlarmAsSent(1L);
        verify(fcmMapper, never()).findActiveTokensByUserId(any());
        verifyNoInteractions(fcmSendService);
    }

    @Test
    @DisplayName("alarm_time이 30분 이내인 알람은 정상 발송한다")
    void sendScheduledAlarms_notExpiredAlarm_sends() {
        long recentAlarmTime = System.currentTimeMillis() / 1000 - (10 * 60); // 10분 전
        PendingAlarmDto alarm = createAlarmWithTime(1L, "1H", 10L, "헤헤병원", recentAlarmTime);
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(1);

        fcmScheduler.sendScheduledAlarms();

        verify(fcmSendService).sendMulticast(any(), anyString(), anyString());
        verify(fcmMapper).markAlarmAsSent(1L);
    }

    // ---- 야간(22시~08시 KST) + night_agreed 필터링 테스트 ----

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("야간(23시) + night_agreed=false면 발송 없이 완료 처리한다")
    void sendScheduledAlarms_nightHours_nightNotAgreed_skipsSending() {
        setClock("2026-08-02T23:00:00");
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원", false);
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));

        fcmScheduler.sendScheduledAlarms();

        verify(fcmMapper).markAlarmAsSent(1L);
        verify(fcmMapper, never()).findActiveTokensByUserId(any());
        verifyNoInteractions(fcmSendService);
    }

    @Test
    @DisplayName("야간(23시)이어도 night_agreed=true면 정상 발송한다")
    void sendScheduledAlarms_nightHours_nightAgreed_sends() {
        setClock("2026-08-02T23:00:00");
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원", true);
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(1);

        fcmScheduler.sendScheduledAlarms();

        verify(fcmSendService).sendMulticast(any(), anyString(), anyString());
        verify(fcmMapper).markAlarmAsSent(1L);
    }

    @Test
    @DisplayName("주간(14시)이면 night_agreed=false여도 정상 발송한다")
    void sendScheduledAlarms_dayHours_nightNotAgreed_sends() {
        setClock("2026-08-02T14:00:00");
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원", false);
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(1);

        fcmScheduler.sendScheduledAlarms();

        verify(fcmSendService).sendMulticast(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("경계값 - 08시 정각은 야간이 아니므로 night_agreed=false여도 정상 발송한다")
    void sendScheduledAlarms_boundary_8am_isDayHours_sends() {
        setClock("2026-08-02T08:00:00");
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원", false);
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));
        given(fcmMapper.findActiveTokensByUserId(10L)).willReturn(List.of("token1"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(1);

        fcmScheduler.sendScheduledAlarms();

        verify(fcmSendService).sendMulticast(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("경계값 - 22시 정각은 야간이므로 night_agreed=false면 발송을 스킵한다")
    void sendScheduledAlarms_boundary_10pm_isNightHours_skips() {
        setClock("2026-08-02T22:00:00");
        PendingAlarmDto alarm = createAlarm(1L, "1H", 10L, "헤헤병원", false);
        given(fcmMapper.findPendingAlarms()).willReturn(List.of(alarm));

        fcmScheduler.sendScheduledAlarms();

        verify(fcmMapper).markAlarmAsSent(1L);
        verifyNoInteractions(fcmSendService);
    }

    private void setClock(String isoLocalDateTime) {
        Clock fixedClock = Clock.fixed(LocalDateTime.parse(isoLocalDateTime).atZone(KST).toInstant(), KST);
        ReflectionTestUtils.setField(fcmScheduler, "clock", fixedClock);
    }

    // ---- helper ----

    private PendingAlarmDto createAlarm(Long alarmId, String alarmType, Long userId, String hospitalName) {
        // 기본 alarmTime: 5분 전 (만료되지 않은 정상 케이스), night_agreed 기본값 true(주/야간 무관 발송)
        return createAlarm(alarmId, alarmType, userId, hospitalName, true);
    }

    private PendingAlarmDto createAlarm(Long alarmId, String alarmType, Long userId, String hospitalName, boolean nightAgreed) {
        return createAlarmWithTime(alarmId, alarmType, userId, hospitalName,
                System.currentTimeMillis() / 1000 - 300, nightAgreed);
    }

    private PendingAlarmDto createAlarmWithTime(Long alarmId, String alarmType, Long userId,
                                                String hospitalName, long alarmTime) {
        return createAlarmWithTime(alarmId, alarmType, userId, hospitalName, alarmTime, true);
    }

    private PendingAlarmDto createAlarmWithTime(Long alarmId, String alarmType, Long userId,
                                                String hospitalName, long alarmTime, boolean nightAgreed) {
        PendingAlarmDto alarm = new PendingAlarmDto();
        ReflectionTestUtils.setField(alarm, "alarmId", alarmId);
        ReflectionTestUtils.setField(alarm, "alarmType", alarmType);
        ReflectionTestUtils.setField(alarm, "alarmTime", alarmTime);
        ReflectionTestUtils.setField(alarm, "userId", userId);
        ReflectionTestUtils.setField(alarm, "hospitalName", hospitalName);
        ReflectionTestUtils.setField(alarm, "nightAgreed", nightAgreed);
        return alarm;
    }
}