package org.dev.hehe.dto.pushtoken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 푸시 토큰 등록/갱신 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "FCM 푸시 토큰 등록 요청")
public class PushTokenRegisterRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    @Schema(description = "FCM 디바이스 토큰", example = "dGhpcyBpcyBhIHRlc3QgdG9rZW4", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotNull(message = "플랫폼은 필수입니다.")
    @Pattern(regexp = "ANDROID|IOS", message = "플랫폼은 ANDROID 또는 IOS여야 합니다.")
    @Schema(description = "플랫폼 (ANDROID/IOS)", example = "ANDROID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String platform;

    @NotNull(message = "알림 권한 여부는 필수입니다.")
    @Schema(description = "OS 알림 권한 허용 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean notificationPermissionGranted;
}
