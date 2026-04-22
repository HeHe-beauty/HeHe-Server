package org.dev.hehe.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.common.ServerTimeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 공통 컨트롤러
 * Swagger 명세는 CommonApi 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/common")
public class CommonController implements CommonApiSpecification {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @GetMapping("/time")
    public ApiResult<ServerTimeResponse> getServerTime() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[GET] /api/v1/common/time - 서버 시각 조회 요청");

        return ApiResult.ok(
                ServerTimeResponse.builder()
                        .timestamp(System.currentTimeMillis())
                        .datetime(now.format(FORMATTER))
                        .build()
        );
    }
}