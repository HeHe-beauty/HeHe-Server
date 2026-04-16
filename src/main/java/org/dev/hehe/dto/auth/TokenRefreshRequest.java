package org.dev.hehe.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Access Token 재발급 요청 DTO
 */
@Schema(description = "Access Token 재발급 요청")
public record TokenRefreshRequest(

        @Schema(description = "앱 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {}