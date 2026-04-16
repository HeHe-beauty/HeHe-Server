package org.dev.hehe.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 소셜 로그인 요청 DTO
 */
@Schema(description = "소셜 로그인 요청")
public record AuthLoginRequest(

        @Schema(description = "소셜 제공자", example = "kakao", allowableValues = {"kakao", "naver"})
        @NotBlank(message = "provider는 필수입니다.")
        @Pattern(regexp = "^(kakao|naver)$", message = "provider는 kakao 또는 naver만 가능합니다.")
        String provider,

        @Schema(description = "소셜 제공자로부터 발급받은 access token", example = "provider_access_token_value")
        @NotBlank(message = "accessToken은 필수입니다.")
        String accessToken
) {}