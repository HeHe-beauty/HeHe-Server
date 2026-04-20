package org.dev.hehe.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 캘린더 일정 생성 요청 DTO
 *
 * - alarmEnabled 는 서버에서 항상 true(활성) 로 저장
 * - userId는 JWT에서 자동 추출 (@LoginUser)
 */
@Getter
@Schema(description = "일정 추가 요청")
public class ScheduleCreateRequest {

    /** 병원명 (필수) */
    @Schema(description = "병원명", example = "ㅇㅇㅇ의원", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hospitalName;

    /** 시술명 (선택) */
    @Schema(description = "시술명", example = "레이저 제모", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String procedureName;

    /** 방문 예정 시각 (Unix timestamp, seconds, 필수) */
    @Schema(type = "string", description = "방문 시각 (millisecond)", example = "1777024800", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long visitTime;
}