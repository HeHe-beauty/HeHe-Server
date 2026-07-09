package org.dev.hehe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 동의 변경 요청 DTO (PATCH)
 *
 * <p>모든 필드는 선택적이며, null인 필드는 기존값을 유지한다.</p>
 */
@Getter
@NoArgsConstructor
@Schema(description = "알림 동의 변경 요청")
public class UserAgreementsRequest {

    @Schema(description = "일반 푸시 동의 (선택, null이면 기존값 유지)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean pushAgreed;

    @Schema(description = "야간 푸시 동의 (선택, null이면 기존값 유지)", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean nightAgreed;

    @Schema(description = "마케팅 수신 동의 (선택, null이면 기존값 유지)", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean mktAgreed;
}
