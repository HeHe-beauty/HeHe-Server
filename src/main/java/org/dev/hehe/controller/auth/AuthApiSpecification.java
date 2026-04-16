package org.dev.hehe.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.dev.hehe.dto.auth.AuthLoginRequest;
import org.dev.hehe.dto.auth.AuthLoginResponse;
import org.dev.hehe.dto.auth.TokenRefreshRequest;
import org.dev.hehe.dto.auth.TokenRefreshResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Auth API Swagger 명세 인터페이스
 */
@Tag(name = "02. Auth", description = "소셜 로그인 / 로그아웃 / 토큰 재발급")
public interface AuthApiSpecification {

    @Operation(summary = "소셜 로그인 / 회원가입",
            description = "카카오 또는 네이버 provider access token으로 로그인합니다. " +
                    "신규 유저는 자동으로 회원가입 처리됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (C002)"),
            @ApiResponse(responseCode = "502", description = "소셜 유저 정보 조회 실패 (AU004)")
    })
    org.dev.hehe.common.response.ApiResponse<AuthLoginResponse> login(
            @Valid @RequestBody AuthLoginRequest request);

    @Operation(summary = "로그아웃",
            description = "Redis에서 Refresh Token을 삭제합니다. Authorization 헤더에 Bearer 토큰 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음 (AU003)")
    })
    org.dev.hehe.common.response.ApiResponse<Void> logout(Long userId);

    @Operation(summary = "Access Token 재발급",
            description = "Refresh Token으로 새 Access Token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰 (AU001/AU002/AU003)")
    })
    org.dev.hehe.common.response.ApiResponse<TokenRefreshResponse> refresh(
            @Valid @RequestBody TokenRefreshRequest request);
}