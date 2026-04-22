package org.dev.hehe.controller.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.common.ServerTimeResponse;

/**
 * Common API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 CommonController
 */
@Tag(name = "Common", description = "공통 API")
public interface CommonApiSpecification {

    @Operation(summary = "서버 현재 시각 조회", description = "서버 기준 현재 시각을 Unix timestamp(ms)와 datetime 문자열로 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "timestamp": 1741564812345,
                                        "datetime": "2026-03-10 10:00:12"
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "C001",
                                      "message": "서버 내부 오류가 발생했습니다."
                                    }
                                    """))
            )
    })
    ApiResult<ServerTimeResponse> getServerTime();
}