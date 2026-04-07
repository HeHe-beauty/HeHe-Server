package org.dev.hehe.dto.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.schedule.Schedule;

import java.util.List;

/**
 * 캘린더 일정 응답 DTO
 *
 * - procedureName: null이면 JSON 응답에서 자동 제외 (@JsonInclude(NON_NULL))
 * - visitTime: Unix timestamp (seconds), 시간대 전환은 FE에서 처리
 * - alarms: 해당 일정에 등록된 알림 목록 (없으면 빈 리스트)
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleResponse {

    /** 일정 ID */
    private Long scheduleId;

    /** 병원명 */
    private String hospitalName;

    /** 시술명 (없으면 null — 응답에서 제외) */
    private String procedureName;

    /** 방문 예정 시각 (Unix timestamp, seconds) */
    private Long visitTime;

    /** 푸시 알림 마스터 토글 */
    private Boolean alarmEnabled;

    /** 등록된 알림 목록 (tb_schedule_alarm 1:N) */
    private List<ScheduleAlarmResponse> alarms;

    /**
     * Schedule 도메인 객체와 알림 목록으로 ScheduleResponse DTO 조립
     *
     * @param schedule 일정 도메인 객체
     * @param alarms   해당 일정의 알림 목록 (없으면 빈 리스트 전달)
     * @return 응답 DTO
     */
    public static ScheduleResponse of(Schedule schedule, List<ScheduleAlarmResponse> alarms) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .hospitalName(schedule.getHospitalName())
                .procedureName(schedule.getProcedureName())
                .visitTime(schedule.getVisitTime())
                .alarmEnabled(schedule.getAlarmEnabled())
                .alarms(alarms)
                .build();
    }
}