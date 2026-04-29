package org.dev.hehe.controller.fcm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.fcm.FcmTestResponse;

/**
 * FCM API Swagger 명세 인터페이스
 */
@Tag(name = "FCM", description = "FCM 푸시 알림 API")
@SecurityRequirement(name = "bearerAuth")
public interface FcmApiSpecification {

    @Operation(summary = "FCM 테스트 발송", description = "현재 로그인한 유저의 활성 FCM 토큰으로 테스트 푸시를 즉시 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발송 성공 (토큰 없으면 successCount=0)"),
            @ApiResponse(responseCode = "401", description = "AU003 - 인증 필요")
    })
    ApiResult<FcmTestResponse> sendTestPush(@LoginUser Long userId);
}