package org.dev.hehe.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dev.hehe.common.response.ApiResponse;
import org.dev.hehe.dto.auth.AuthLoginRequest;
import org.dev.hehe.dto.auth.AuthLoginResponse;
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
 * - POST /api/v1/auth/login    소셜 로그인 / 자동 회원가입
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
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        AuthLoginResponse response = authService.login(request.provider(), request.accessToken());
        return ApiResponse.ok(response);
    }

    @PostMapping("/logout")
    @Override
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/token/refresh")
    @Override
    public ApiResponse<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request.refreshToken());
        return ApiResponse.ok(response);
    }
}