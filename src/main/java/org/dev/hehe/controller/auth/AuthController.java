package org.dev.hehe.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.auth.AuthLoginRequest;
import org.dev.hehe.dto.auth.AuthLoginResponse;
import org.dev.hehe.dto.auth.AuthSignupRequest;
import org.dev.hehe.dto.auth.TokenRefreshRequest;
import org.dev.hehe.dto.auth.TokenRefreshResponse;
import org.dev.hehe.service.auth.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 컨트롤러
 * - POST /api/v1/auth/login    소셜 로그인 (미가입 시 exists=false)
 * - POST /api/v1/auth/signup   회원가입 (동의값 포함)
 * - POST /api/v1/auth/logout   로그아웃 (Redis RT 삭제)
 * - POST /api/v1/auth/token/refresh  Access Token 재발급
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiSpecification {

    private final AuthService authService;

    @PostMapping("/login")
    @Override
    public ApiResult<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        AuthLoginResponse response = authService.login(request.provider(), request.accessToken());
        return ApiResult.ok(response);
    }

    @PostMapping("/signup")
    @Override
    public ApiResult<AuthLoginResponse> signup(@Valid @RequestBody AuthSignupRequest request) {
        AuthLoginResponse response = authService.signup(
                request.provider(), request.accessToken(),
                request.pushAgreed(), request.nightAgreed(), request.mktAgreed(),
                request.isOverAge(), request.termsVersion());
        return ApiResult.ok(response);
    }

    @PostMapping("/logout")
    @Override
    public ApiResult<Void> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ApiResult.ok(null);
    }

    @PostMapping("/token/refresh")
    @Override
    public ApiResult<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request.refreshToken());
        return ApiResult.ok(response);
    }
}