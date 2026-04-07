package org.dev.hehe.dto.schedule;

import lombok.Getter;

/**
 * 캘린더 일정 수정 요청 DTO (PATCH)
 *
 * - 모든 필드는 선택적이며, null 인 필드는 기존값을 유지
 * - hospitalName, procedureName, visitTime 중 최소 하나 이상 전달 필요
 */
@Getter
public class ScheduleUpdateRequest {

    /**
     * 병원명 (선택, null 이면 기존값 유지)
     * 전달 시 공백 불가
     */
    private String hospitalName;

    /**
     * 시술명 (선택, null 이면 기존값 유지)
     * 빈 문자열로 전달 시 NULL 로 초기화됨
     */
    private String procedureName;

    /**
     * 방문 예정 시각 (Unix timestamp, seconds)
     * 선택, null 이면 기존값 유지
     */
    private Long visitTime;
}