package org.dev.hehe.dto.schedule;

import lombok.Builder;
import lombok.Getter;

/**
 * 캘린더 일정 생성 응답 DTO
 */
@Getter
@Builder
public class ScheduleCreateResponse {

    /** 생성된 일정 ID */
    private Long scheduleId;
}