package org.dev.hehe.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.user.UserSummaryResponse;

/**
 * 유저 API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 UserController
 */
@Tag(name = "User", description = "유저 API")
public interface UserApiSpecification {

    @Operation(
            summary = "마이페이지 요약 조회",
            description = "로그인한 유저의 찜 수, 문의 수, 예약 수를 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "bookmarkCount": 5,
                                        "contactCount": 3,
                                        "scheduleCount": 2
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "AU003",
                                      "message": "인증 정보가 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<UserSummaryResponse> getSummary(@LoginUser Long userId);
}