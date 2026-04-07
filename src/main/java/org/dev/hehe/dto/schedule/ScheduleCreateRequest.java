package org.dev.hehe.dto.schedule;

import lombok.Getter;

/**
 * 캘린더 일정 생성 요청 DTO
 *
 * - alarmEnabled 는 서버에서 항상 true(활성) 로 저장
 *
 * TODO: Auth 구현 후 userId 필드 제거, JWT에서 자동 추출
 */
@Getter
public class ScheduleCreateRequest {

    /** 유저 ID (임시, Auth 구현 후 제거) */
    private Long userId;

    /** 병원명 (필수) */
    private String hospitalName;

    /** 시술명 (선택) */
    private String procedureName;

    /** 방문 예정 시각 (Unix timestamp, seconds, 필수) */
    private Long visitTime;
}