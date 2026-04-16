package org.dev.hehe.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소셜 로그인 응답 DTO
 */
@Schema(description = "소셜 로그인 응답")
public record AuthLoginResponse(

        @Schema(description = "앱 Access Token (1시간 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "앱 Refresh Token (14일 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "유저 정보")
        UserInfo user
) {

        @Schema(description = "유저 기본 정보")
        public record UserInfo(

                @Schema(description = "유저 ID", example = "1")
                Long userId,

                @Schema(description = "닉네임", example = "홍길동")
                String nickname
        ) {}
}