package org.dev.hehe.controller.common;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.common.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ALB 헬스체크 전용 컨트롤러
 * - 경로: GET /health
 * - ALB 대상 그룹이 이 엔드포인트로 헬스체크 수행 (HTTP 200 = healthy)
 * - Swagger 미노출 (@Hidden) — 비즈니스 API 아님
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResult<HealthResponse> health() {
        log.debug("[GET] /health - ALB 헬스체크");
        return ApiResult.ok(HealthResponse.builder().status("UP").build());
    }
}