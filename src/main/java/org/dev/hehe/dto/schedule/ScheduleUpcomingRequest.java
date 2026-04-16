package org.dev.hehe.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 예정 일정 N건 조회 요청 파라미터
 *
 * <p>GET /api/v1/schedules/upcoming 의 쿼리 파라미터를 바인딩한다.</p>
 * <p>현재 시각 이후의 일정을 visit_time 오름차순으로 limit 개 반환한다.</p>
 * <p>userId는 JWT에서 자동 추출 (@LoginUser)</p>
 */
@Getter
@Setter
@Schema(description = "예정 일정 N건 조회 요청")
public class ScheduleUpcomingRequest {

    @NotNull
    @Min(1)
    @Schema(description = "조회할 일정 수 (1 이상)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limit;
}