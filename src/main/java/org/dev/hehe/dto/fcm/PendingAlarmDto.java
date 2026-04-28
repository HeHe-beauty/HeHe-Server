package org.dev.hehe.dto.fcm;

import lombok.Getter;

/**
 * FCM 발송 대상 알람 조회 결과 DTO
 * tb_schedule_alarm + tb_schedule + tb_user JOIN 결과를 담는다.
 */
@Getter
public class PendingAlarmDto {

    /** tb_schedule_alarm.id — is_sent 업데이트 및 push_history 연결에 사용 */
    private Long alarmId;

    /**
     * 알림 유형 (1H / 1D / 3D / CUSTOM)
     * 알림 본문 문구 분기에 사용
     */
    private String alarmType;

    /** 발송 대상 유저 ID */
    private Long userId;

    /** 병원명 — 알림 본문에 포함 */
    private String hospitalName;

    /** 알림 발송 예정 시각 (Unix timestamp) — 만료 여부 판단에 사용 */
    private Long alarmTime;

    /** 야간 푸시 동의 여부 — 22시~08시 발송 여부 판단에 사용 */
    private boolean nightAgreed;
}