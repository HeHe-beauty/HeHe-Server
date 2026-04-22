package org.dev.hehe.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.contact.ContactHistory;

import java.time.LocalDateTime;

/**
 * 문의 내역 목록 단일 항목 응답 DTO
 */
@Getter
@Builder
@Schema(description = "문의 내역 단일 항목")
public class ContactHistoryResponse {

    @Schema(description = "문의 내역 ID", example = "1")
    private Long id;

    @Schema(description = "병원 ID", example = "101")
    private Long hospitalId;

    @Schema(description = "병원명", example = "강남 제모 클리닉")
    private String hospitalName;

    @Schema(description = "문의 유형 (CALL/CHAT/VISIT)", example = "CALL")
    private String contactType;

    @Schema(description = "문의 시각", example = "2026-04-22T10:30:00")
    private LocalDateTime createdAt;

    /**
     * ContactHistory 도메인으로부터 응답 DTO 생성
     *
     * @param contact 문의 내역 도메인
     * @return 응답 DTO
     */
    public static ContactHistoryResponse from(ContactHistory contact) {
        return ContactHistoryResponse.builder()
                .id(contact.getId())
                .hospitalId(contact.getHospitalId())
                .hospitalName(contact.getHospitalName())
                .contactType(contact.getContactType())
                .createdAt(contact.getCreatedAt())
                .build();
    }
}