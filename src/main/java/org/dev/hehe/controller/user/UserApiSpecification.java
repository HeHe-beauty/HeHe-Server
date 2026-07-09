package org.dev.hehe.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.user.UserAgreementsRequest;
import org.dev.hehe.dto.user.UserSummaryResponse;
import org.dev.hehe.dto.user.UserWithdrawRequest;
import org.springframework.web.bind.annotation.RequestBody;

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

    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 유저를 소프트 삭제(status=LEAVE) 처리하고 로그인 토큰을 무효화합니다. " +
                    "찜·일정 등 연관 데이터는 일정 기간 보관 후 배치로 완전 삭제됩니다. 이미 탈퇴한 유저도 200을 반환합니다(idempotent). " +
                    "요청 body에 provider/providerAccessToken을 함께 보내면 소셜 unlink를 시도합니다(카카오/네이버 둘 다 지원). " +
                    "body를 생략하면 unlink 없이 탈퇴만 진행합니다. unlink 실패해도 탈퇴는 그대로 처리됩니다(best-effort).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "탈퇴 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true }
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "U001",
                                      "message": "유저를 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<Void> deleteAccount(@LoginUser Long userId, @RequestBody(required = false) UserWithdrawRequest request);

    @Operation(
            summary = "알림 동의 변경",
            description = "푸시/야간/마케팅 수신 동의를 변경합니다. 부분 수정이며 null인 필드는 기존값을 유지합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "변경 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true }
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
    ApiResult<Void> updateAgreements(@LoginUser Long userId, @Valid @RequestBody UserAgreementsRequest request);
}