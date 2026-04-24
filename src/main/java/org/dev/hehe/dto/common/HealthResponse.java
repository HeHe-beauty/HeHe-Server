package org.dev.hehe.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 헬스체크 응답 DTO
 * - ALB 대상 그룹 헬스체크 전용
 */
@Getter
@Builder
@Schema(description = "헬스체크 응답")
public class HealthResponse {

    /** 서버 상태 — 정상 시 항상 "UP" */
    @Schema(description = "서버 상태", example = "UP")
    private final String status;
}