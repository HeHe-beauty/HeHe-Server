package org.dev.hehe.controller.pushtoken;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.pushtoken.PushTokenDeactivateRequest;
import org.dev.hehe.dto.pushtoken.PushTokenRegisterRequest;
import org.dev.hehe.service.pushtoken.PushTokenService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FCM 푸시 토큰 컨트롤러
 * Swagger 명세는 PushTokenApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/push-tokens")
@RequiredArgsConstructor
public class PushTokenController implements PushTokenApiSpecification {

    private final PushTokenService pushTokenService;

    /** FCM 토큰 등록/갱신 */
    @Override
    @PostMapping
    public ApiResult<Void> registerToken(@LoginUser Long userId,
                                         @RequestBody @Valid PushTokenRegisterRequest request) {
        pushTokenService.registerToken(userId, request);
        return ApiResult.ok(null);
    }

    /** FCM 토큰 비활성화 */
    @Override
    @DeleteMapping
    public ApiResult<Void> deactivateToken(@LoginUser Long userId,
                                           @RequestBody @Valid PushTokenDeactivateRequest request) {
        pushTokenService.deactivateToken(userId, request);
        return ApiResult.ok(null);
    }
}
