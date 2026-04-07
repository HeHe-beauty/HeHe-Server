package org.dev.hehe.dto.schedule;

import lombok.Getter;

/**
 * 알림 등록/삭제 요청 DTO
 *
 * - 알림 등록: POST /api/v1/schedules/{scheduleId}/alarms
 * - 알림 삭제: DELETE /api/v1/schedules/{scheduleId}/alarms/{alarmType}
 *
 * 지원 alarmType: 1H(1시간 전), 1D(1일 전), 3D(3일 전)
 */
@Getter
public class ScheduleAlarmRequest {

    /** 알림 유형 (1H, 1D, 3D) */
    private String alarmType;
}