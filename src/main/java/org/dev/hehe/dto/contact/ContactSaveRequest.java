package org.dev.hehe.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문의 내역 저장 요청 DTO
 *
 * <p>contactType은 CALL·CHAT·VISIT 중 하나여야 한다.</p>
 */
@Getter
@NoArgsConstructor
@Schema(description = "문의 내역 저장 요청")
public class ContactSaveRequest {

    @NotNull(message = "병원 ID는 필수입니다.")
    @Schema(description = "병원 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long hospitalId;

    @NotNull(message = "문의 유형은 필수입니다.")
    @Pattern(regexp = "CALL|CHAT|VISIT", message = "문의 유형은 CALL, CHAT, VISIT 중 하나여야 합니다.")
    @Schema(description = "문의 유형 (CALL/CHAT/VISIT)", example = "CALL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactType;
}