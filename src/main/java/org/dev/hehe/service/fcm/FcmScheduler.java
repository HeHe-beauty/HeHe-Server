package org.dev.hehe.service.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.fcm.PendingAlarmDto;
import org.dev.hehe.mapper.fcm.FcmMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * FCM 스케줄 알림 발송 스케줄러
 * 1분마다 실행하여 발송 시각이 도달한 알람을 조회하고 FCM 푸시를 전송한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmScheduler {

    private final FcmMapper fcmMapper;
    private final FcmSendService fcmSendService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String PUSH_TITLE = "[헤헤] 예약 알림";

    /** 알람 발송 유효 기간 (초) — 이 시간을 초과하면 발송 포기 후 완료 처리 */
    private static final long ALARM_EXPIRY_SECONDS = 30 * 60L;

    /**
     * 미발송 스케줄 알람 조회 후 FCM 푸시 발송
     *
     * <p>fixedDelay: 이전 실행 종료 후 1분 뒤 재실행 (중복 실행 방지)</p>
     * <p>야간(22시~08시)에는 night_agreed=true인 유저에게만 발송한다.</p>
     */
    @Scheduled(fixedDelay = 60_000)
    public void sendScheduledAlarms() {
        List<PendingAlarmDto> pendingAlarms = fcmMapper.findPendingAlarms();
        if (pendingAlarms.isEmpty()) {
            return;
        }

        log.info("FCM 스케줄러 실행: 발송 대상 알람 {}건", pendingAlarms.size());
        boolean nightTime = isNightTime();

        for (PendingAlarmDto alarm : pendingAlarms) {
            // 만료된 알람은 발송 포기 후 완료 처리
            if (isExpired(alarm.getAlarmTime())) {
                log.warn("만료된 알람 완료 처리: alarmId={}, alarmTime={}", alarm.getAlarmId(), alarm.getAlarmTime());
                fcmMapper.markAlarmAsSent(alarm.getAlarmId());
                continue;
            }

            // 야간 시간대에는 야간 동의 유저만 발송
            if (nightTime && !alarm.isNightAgreed()) {
                log.debug("야간 알림 동의 미완료 — 건너뜀: userId={}", alarm.getUserId());
                continue;
            }

            // 활성 FCM 토큰 조회
            List<String> tokens = fcmMapper.findActiveTokensByUserId(alarm.getUserId());
            if (tokens.isEmpty()) {
                log.debug("활성 FCM 토큰 없음 — 다음 주기에 재시도: userId={}", alarm.getUserId());
                continue;
            }

            // 메시지 구성 및 발송
            String body = buildBody(alarm.getAlarmType(), alarm.getHospitalName());
            int successCount = fcmSendService.sendMulticast(tokens, PUSH_TITLE, body);
            int failCount = tokens.size() - successCount;

            // 발송 완료 처리
            fcmMapper.markAlarmAsSent(alarm.getAlarmId());
            fcmMapper.insertPushHistory(alarm.getUserId(), alarm.getAlarmId(), PUSH_TITLE, body, successCount, failCount);

            log.info("알람 발송 완료: alarmId={}, userId={}, success={}, fail={}",
                    alarm.getAlarmId(), alarm.getUserId(), successCount, failCount);
        }
    }

    /**
     * 알람 발송 유효 기간 초과 여부
     * alarm_time 기준으로 ALARM_EXPIRY_SECONDS(30분) 이상 지난 경우 만료로 판단
     *
     * @param alarmTime 알람 발송 예정 시각 (Unix timestamp)
     * @return 만료 여부
     */
    private boolean isExpired(Long alarmTime) {
        long now = System.currentTimeMillis() / 1000;
        return now - alarmTime > ALARM_EXPIRY_SECONDS;
    }

    /**
     * 현재 KST 기준 야간 시간대 여부
     * 야간: 22시 이상 또는 08시 미만
     */
    private boolean isNightTime() {
        int hour = ZonedDateTime.now(KST).getHour();
        return hour >= 22 || hour < 8;
    }

    /**
     * alarmType에 따른 알림 본문 생성
     *
     * @param alarmType    알림 유형 (1H, 1D, 3D, CUSTOM)
     * @param hospitalName 병원명
     * @return 알림 본문 문자열
     */
    private String buildBody(String alarmType, String hospitalName) {
        return switch (alarmType) {
            case "1H" -> hospitalName + " 방문 1시간 전이에요.";
            case "1D" -> hospitalName + " 방문 하루 전이에요.";
            case "3D" -> hospitalName + " 방문 3일 전이에요.";
            default   -> hospitalName + " 방문 예정이 다가왔어요.";
        };
    }
}
