package org.dev.hehe.controller.pushtoken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.pushtoken.PushTokenDeactivateRequest;
import org.dev.hehe.dto.pushtoken.PushTokenRegisterRequest;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * FCM 푸시 토큰 API Swagger 명세 인터페이스
 * 실제 구현은 PushTokenController
 */
@Tag(name = "PushToken", description = "FCM 푸시 토큰 API")
public interface PushTokenApiSpecification {

    @Operation(
            summary = "FCM 토큰 등록/갱신",
            description = """
                    FCM 디바이스 토큰을 등록하거나 갱신합니다.

                    - 앱 실행 시 또는 토큰 갱신 시 호출합니다.
                    - 동일 토큰이 이미 존재하면 유저 정보와 알림 권한 여부를 갱신합니다.
                    - JWT 인증이 필요합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "등록/갱신 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 값 누락 또는 유효하지 않은 플랫폼)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "errorCode": "C002", "message": "잘못된 요청 값입니다." }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "errorCode": "AU003", "message": "인증 정보가 없습니다." }
                                    """))
            )
    })
    ApiResult<Void> registerToken(@LoginUser Long userId, @RequestBody PushTokenRegisterRequest request);

    @Operation(
            summary = "FCM 토큰 비활성화",
            description = """
                    FCM 디바이스 토큰을 비활성화합니다.

                    - 로그아웃 시 호출하여 해당 기기로의 푸시 알림 수신을 중단합니다.
                    - 토큰이 존재하지 않거나 이미 비활성화된 경우에도 200을 반환합니다 (idempotent).
                    - JWT 인증이 필요합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "비활성화 성공 (토큰 미존재 시에도 동일)",
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
                                    { "success": false, "errorCode": "AU003", "message": "인증 정보가 없습니다." }
                                    """))
            )
    })
    ApiResult<Void> deactivateToken(@LoginUser Long userId, @RequestBody PushTokenDeactivateRequest request);
}
