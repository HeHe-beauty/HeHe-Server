package org.dev.hehe.dto.heart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 하트 응답 DTO
 * - POST /api/v1/hearts : 하트 추가 후 누적 수 반환
 * - GET  /api/v1/hearts : 현재 누적 수 반환
 */
@Getter
@Builder
@Schema(description = "하트 응답")
public class HeartResponse {

    @Schema(description = "누적 하트 수", example = "42")
    private Long total;

    public static HeartResponse of(Long total) {
        return HeartResponse.builder().total(total).build();
    }
}
