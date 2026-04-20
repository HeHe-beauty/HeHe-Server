package org.dev.hehe.dto.schedule;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 날짜별 일정 건수 Mapper 결과 매핑 DTO
 * - ScheduleMapper.findScheduleCountGroupByDate() 결과를 담는 내부 DTO
 */
@Getter
@NoArgsConstructor
public class ScheduleDateCountDto {

    /** 날짜 (yyyy-MM-dd) */
    private String date;

    /** 해당 날짜의 일정 건수 */
    private int count;
}