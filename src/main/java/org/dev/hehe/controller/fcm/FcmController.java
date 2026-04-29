package org.dev.hehe.controller.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.fcm.FcmTestResponse;
import org.dev.hehe.service.fcm.FcmService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FCM 컨트롤러
 * Swagger 명세는 FcmApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmController implements FcmApiSpecification {

    private final FcmService fcmService;

    /** FCM 테스트 푸시 즉시 발송 */
    @Override
    @PostMapping("/test")
    public ApiResult<FcmTestResponse> sendTestPush(@LoginUser Long userId) {
        log.info("[POST] /api/v1/fcm/test - FCM 테스트 발송 요청: userId={}", userId);
        FcmTestResponse response = fcmService.sendTestPush(userId);
        return ApiResult.ok(response);
    }
}