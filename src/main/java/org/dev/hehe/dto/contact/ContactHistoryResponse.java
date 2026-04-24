package org.dev.hehe.dto.contact;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.contact.ContactHistory;

import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "도로명 주소", example = "서울 강남구 역삼동 123-4")
    private String address;

    @Schema(description = "태그 목록", example = "[\"여성원장\", \"주차가능\"]")
    private List<String> tags;

    @Schema(description = "문의 유형 (CALL/CHAT/VISIT)", example = "CALL")
    private String contactType;

    @Schema(description = "문의 시각", example = "2026-04-22T10:30:00")
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "찜 여부 (비로그인 시 미노출)", example = "true")
    private Boolean isBookmarked;

    /**
     * ContactHistory 도메인 + 태그 목록 + 찜 여부로 응답 DTO 생성
     *
     * @param contact      문의 내역 도메인
     * @param tags         병원 태그 목록
     * @param isBookmarked 찜 여부
     * @return 응답 DTO
     */
    public static ContactHistoryResponse of(ContactHistory contact, List<String> tags, Boolean isBookmarked) {
        return ContactHistoryResponse.builder()
                .id(contact.getId())
                .hospitalId(contact.getHospitalId())
                .hospitalName(contact.getHospitalName())
                .address(contact.getAddress())
                .tags(tags)
                .contactType(contact.getContactType())
                .createdAt(contact.getCreatedAt())
                .isBookmarked(isBookmarked)
                .build();
    }
}