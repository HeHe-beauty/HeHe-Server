package org.dev.hehe.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.dev.hehe.dto.auth.AuthLoginRequest;
import org.dev.hehe.dto.auth.AuthLoginResponse;
import org.dev.hehe.dto.auth.AuthSignupRequest;
import org.dev.hehe.dto.auth.TokenRefreshRequest;
import org.dev.hehe.dto.auth.TokenRefreshResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Auth API Swagger 명세 인터페이스
 */
@Tag(name = "02. Auth", description = "소셜 로그인 / 회원가입 / 로그아웃 / 토큰 재발급")
public interface AuthApiSpecification {

    @Operation(summary = "소셜 로그인",
            description = "카카오 또는 네이버 provider access token으로 로그인합니다. " +
                    "미가입 유저는 exists=false만 반환하며(자동 가입되지 않음), FE는 이 경우 회원가입 절차(동의 화면)로 안내해야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (exists=false면 미가입)",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "가입된 유저", value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "exists": true,
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "user": { "userId": 1, "nickname": "홍길동" }
                                              }
                                            }
                                            """),
                                    @ExampleObject(name = "미가입 유저", value = """
                                            {
                                              "success": true,
                                              "data": { "exists": false }
                                            }
                                            """)
                            })
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (C002)"),
            @ApiResponse(responseCode = "502", description = "소셜 유저 정보 조회 실패 (AU004)")
    })
    org.dev.hehe.common.response.ApiResult<AuthLoginResponse> login(
            @Valid @RequestBody AuthLoginRequest request);

    @Operation(summary = "회원가입",
            description = "로그인 시도(`POST /login`)에서 exists=false를 받은 뒤, 동의 화면을 거쳐 호출합니다. " +
                    "provider/accessToken으로 소셜 유저 정보를 다시 조회해 신규 유저를 등록하고 즉시 로그인 처리합니다. " +
                    "이미 가입된 유저가 재호출해도 에러 없이 로그인 처리됩니다(idempotent). isOverAge=false면 400.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 및 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 14세 이상 동의 누락 (C002)"),
            @ApiResponse(responseCode = "502", description = "소셜 유저 정보 조회 실패 (AU004)")
    })
    org.dev.hehe.common.response.ApiResult<AuthLoginResponse> signup(
            @Valid @RequestBody AuthSignupRequest request);

    @Operation(summary = "로그아웃",
            description = "Redis에서 Refresh Token을 삭제합니다. Authorization 헤더에 Bearer 토큰 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음 (AU003)")
    })
    org.dev.hehe.common.response.ApiResult<Void> logout(Long userId);

    @Operation(summary = "Access Token 재발급",
            description = "Refresh Token으로 새 Access Token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰 (AU001/AU002/AU003)")
    })
    org.dev.hehe.common.response.ApiResult<TokenRefreshResponse> refresh(
            @Valid @RequestBody TokenRefreshRequest request);
}