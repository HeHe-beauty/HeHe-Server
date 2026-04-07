package org.dev.hehe.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 일괄 INSERT용 DTO
 * ScheduleMapper.insertScheduleAlarms() 의 foreach 파라미터로 사용
 */
@Getter
@AllArgsConstructor
public class ScheduleAlarmInsertDto {

    /** 연관 일정 ID */
    private Long scheduleId;

    /** 알림 유형: 1H, 3H, 1D, CUSTOM */
    private String alarmType;

    /** 알림 발송 예정 시각 (Unix timestamp, seconds) */
    private Long alarmTime;
}