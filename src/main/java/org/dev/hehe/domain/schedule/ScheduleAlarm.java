package org.dev.hehe.domain.schedule;

import lombok.Getter;

import java.sql.Timestamp;

/**
 * 캘린더 일정 알림 도메인 객체 (tb_schedule_alarm 매핑)
 * tb_schedule과 N:1 관계 (한 일정에 여러 알림 시점 설정 가능)
 */
@Getter
public class ScheduleAlarm {

    /** 내부 PK */
    private Long id;

    /** 연관 일정 ID (tb_schedule.schedule_id 참조) */
    private Long scheduleId;

    /**
     * 알림 유형
     * ENUM: '1H'(1시간 전), '3H'(3시간 전), '1D'(1일 전), 'CUSTOM'(사용자 지정)
     */
    private String alarmType;

    /** 알림 발송 예정 시각 (Unix timestamp, seconds) */
    private Long alarmTime;

    /** 발송 완료 여부 */
    private Boolean isSent;

    private Timestamp createdAt;
    private Timestamp updatedAt;
}