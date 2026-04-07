package org.dev.hehe.domain.schedule;

import lombok.Getter;

import java.sql.Timestamp;

/**
 * 캘린더 방문 일정 도메인 객체 (tb_schedule 매핑)
 *
 * - hospital_name, procedure_name: 현재는 직접 입력값 저장
 * - 추후 hospital_id / procedure_id FK 컬럼 추가 예정
 */
@Getter
public class Schedule {

    /** 내부 PK */
    private Long id;

    /** 비즈니스 일정 ID */
    private Long scheduleId;

    /** 일정 소유 유저 ID */
    private Long userId;

    /** 병원명 (직접 입력) */
    private String hospitalName;

    /** 시술명 (직접 입력, NULL 허용) */
    private String procedureName;

    /**
     * 방문 예정 시각 (Unix timestamp, seconds)
     * 시간대 전환은 FE에서 처리
     */
    private Long visitTime;

    /** 푸시 알림 마스터 토글 */
    private Boolean alarmEnabled;

    private Timestamp createdAt;
    private Timestamp updatedAt;
}