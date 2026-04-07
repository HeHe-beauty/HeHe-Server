package org.dev.hehe.dto.schedule;

import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.schedule.ScheduleAlarm;

/**
 * 캘린더 일정 알림 응답 DTO
 * ScheduleResponse 내부에 리스트로 포함됨
 */
@Getter
@Builder
public class ScheduleAlarmResponse {

    /**
     * 알림 유형
     * '1H'(1시간 전), '3H'(3시간 전), '1D'(1일 전), 'CUSTOM'(사용자 지정)
     */
    private String alarmType;

    /** 알림 발송 예정 시각 (Unix timestamp, seconds) */
    private Long alarmTime;

    /** 발송 완료 여부 */
    private Boolean isSent;

    /**
     * ScheduleAlarm 도메인 객체를 ScheduleAlarmResponse DTO로 변환
     *
     * @param alarm 도메인 객체
     * @return 알림 응답 DTO
     */
    public static ScheduleAlarmResponse from(ScheduleAlarm alarm) {
        return ScheduleAlarmResponse.builder()
                .alarmType(alarm.getAlarmType())
                .alarmTime(alarm.getAlarmTime())
                .isSent(alarm.getIsSent())
                .build();
    }
}