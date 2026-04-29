package org.dev.hehe.dto.fcm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FCM 테스트 발송 응답 DTO
 */
@Getter
@AllArgsConstructor
@Schema(description = "FCM 테스트 발송 결과")
public class FcmTestResponse {

    @Schema(description = "발송 성공 건수", example = "1")
    private final int successCount;

    @Schema(description = "발송 실패 건수", example = "0")
    private final int failCount;
}