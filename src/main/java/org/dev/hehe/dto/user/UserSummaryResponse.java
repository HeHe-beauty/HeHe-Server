package org.dev.hehe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 마이페이지 요약 응답 DTO
 *
 * <p>찜 수, 문의 수, 예약 수를 반환한다.</p>
 */
@Getter
@Builder
@Schema(description = "마이페이지 요약 정보")
public class UserSummaryResponse {

    @Schema(description = "찜한 병원 수", example = "5")
    private int bookmarkCount;

    @Schema(description = "문의 수 (삭제 제외)", example = "3")
    private int contactCount;

    @Schema(description = "예약 수", example = "2")
    private int scheduleCount;
}