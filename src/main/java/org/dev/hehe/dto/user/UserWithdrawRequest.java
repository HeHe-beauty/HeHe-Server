package org.dev.hehe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 요청 DTO
 *
 * <p>provider/providerAccessToken은 선택 값이다. 값이 있으면 탈퇴 처리 중 소셜 unlink를 시도하고,
 * 없으면 unlink를 시도하지 않고 DB 탈퇴만 진행한다(best-effort).</p>
 */
@Getter
@NoArgsConstructor
@Schema(description = "회원 탈퇴 요청 (소셜 unlink용, 선택)")
public class UserWithdrawRequest {

    @Schema(description = "소셜 제공자 (KAKAO/NAVER), unlink를 시도하지 않으면 생략 가능",
            example = "KAKAO", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String provider;

    @Schema(description = "FE가 방금 재획득한 provider access token (unlink 호출용)",
            example = "provider-access-token", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String providerAccessToken;
}
