package org.dev.hehe.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Access Token 재발급 응답 DTO
 */
@Schema(description = "Access Token 재발급 응답")
public record TokenRefreshResponse(

        @Schema(description = "새로 발급된 앱 Access Token (1시간 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {}