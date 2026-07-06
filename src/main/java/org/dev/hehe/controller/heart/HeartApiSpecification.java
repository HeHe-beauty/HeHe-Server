package org.dev.hehe.controller.heart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.heart.HeartResponse;

/**
 * Heart API Swagger 명세 인터페이스
 * - 인증 불필요, 익명 하트 기능
 */
@Tag(name = "Heart", description = "하트 API")
public interface HeartApiSpecification {

    @Operation(summary = "하트 추가", description = "익명으로 하트를 1개 추가하고 누적 수를 반환합니다. 중복 차단 없음 (프론트 UX 제한).")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "추가 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": { "total": 42 }
                                    }
                                    """))
            )
    })
    ApiResult<HeartResponse> addHeart();

    @Operation(summary = "하트 수 조회", description = "현재 누적 하트 수를 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": { "total": 42 }
                                    }
                                    """))
            )
    })
    ApiResult<HeartResponse> getTotal();
}
