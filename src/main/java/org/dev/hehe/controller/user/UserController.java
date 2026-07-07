package org.dev.hehe.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.user.UserSummaryResponse;
import org.dev.hehe.dto.user.UserWithdrawRequest;
import org.dev.hehe.service.user.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 유저 컨트롤러
 * Swagger 명세는 UserApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApiSpecification {

    private final UserService userService;

    /** 마이페이지 요약 조회 */
    @Override
    @GetMapping("/summary")
    public ApiResult<UserSummaryResponse> getSummary(@LoginUser Long userId) {
        log.info("[GET] /api/v1/users/summary - userId={}", userId);
        return ApiResult.ok(userService.getSummary(userId));
    }

    /** 회원 탈퇴 */
    @Override
    @DeleteMapping
    public ApiResult<Void> deleteAccount(@LoginUser Long userId,
                                          @RequestBody(required = false) UserWithdrawRequest request) {
        log.info("[DELETE] /api/v1/users - userId={}", userId);
        userService.deleteAccount(userId, request);
        return ApiResult.ok(null);
    }
}
