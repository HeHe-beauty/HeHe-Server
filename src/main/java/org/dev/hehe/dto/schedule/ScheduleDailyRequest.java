package org.dev.hehe.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 날짜별 일정 목록 조회 요청 파라미터
 *
 * <p>GET /api/v1/schedules/daily 의 쿼리 파라미터를 바인딩한다.</p>
 * <p>userId는 JWT에서 자동 추출 (@LoginUser)</p>
 */
@Getter
@Setter
@Schema(description = "날짜별 일정 목록 조회 요청")
public class ScheduleDailyRequest {

    @NotNull
    @Schema(description = "조회할 날짜 (yyyy-MM-dd)", example = "2026-04-08", requiredMode = Schema.RequiredMode.REQUIRED)
    private String date;
}