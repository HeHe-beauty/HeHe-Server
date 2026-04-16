package org.dev.hehe.dto.schedule;

import lombok.Getter;

/**
 * 캘린더 일정 생성 요청 DTO
 *
 * - alarmEnabled 는 서버에서 항상 true(활성) 로 저장
 * - userId는 JWT에서 자동 추출 (@LoginUser)
 */
@Getter
public class ScheduleCreateRequest {

    /** 병원명 (필수) */
    private String hospitalName;

    /** 시술명 (선택) */
    private String procedureName;

    /** 방문 예정 시각 (Unix timestamp, seconds, 필수) */
    private Long visitTime;
}