package org.dev.hehe.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 회원가입 요청 DTO
 *
 * <p>provider/accessToken으로 소셜 유저 정보를 다시 조회한 뒤(로그인 시도와 별개 호출) 동의값과
 * 함께 신규 유저를 등록한다. 이미 가입된 유저면 idempotent하게 로그인 처리한다.</p>
 */
@Schema(description = "회원가입 요청")
public record AuthSignupRequest(

        @Schema(description = "소셜 제공자", example = "kakao", allowableValues = {"kakao", "naver"})
        @NotBlank(message = "provider는 필수입니다.")
        @Pattern(regexp = "^(kakao|naver)$", message = "provider는 kakao 또는 naver만 가능합니다.")
        String provider,

        @Schema(description = "소셜 제공자로부터 발급받은 access token", example = "provider_access_token_value")
        @NotBlank(message = "accessToken은 필수입니다.")
        String accessToken,

        @Schema(description = "일반 푸시 동의", example = "true")
        @NotNull(message = "pushAgreed는 필수입니다.")
        Boolean pushAgreed,

        @Schema(description = "야간 푸시 동의", example = "false")
        @NotNull(message = "nightAgreed는 필수입니다.")
        Boolean nightAgreed,

        @Schema(description = "마케팅 수신 동의", example = "false")
        @NotNull(message = "mktAgreed는 필수입니다.")
        Boolean mktAgreed,

        @Schema(description = "14세 이상 동의 (필수, false면 가입 불가)", example = "true")
        @AssertTrue(message = "14세 이상 동의가 필요합니다.")
        boolean isOverAge,

        @Schema(description = "가입 시점 약관 버전", example = "v1.0.0")
        @NotBlank(message = "termsVersion은 필수입니다.")
        String termsVersion
) {}
