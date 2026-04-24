package org.dev.hehe.dto.pushtoken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 푸시 토큰 비활성화 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "FCM 푸시 토큰 비활성화 요청")
public class PushTokenDeactivateRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    @Schema(description = "비활성화할 FCM 디바이스 토큰", example = "dGhpcyBpcyBhIHRlc3QgdG9rZW4", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
}
