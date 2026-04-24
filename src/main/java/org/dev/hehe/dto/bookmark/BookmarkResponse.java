package org.dev.hehe.dto.bookmark;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.bookmark.BookmarkedHospital;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 찜한 병원 목록 단일 항목 응답 DTO
 */
@Getter
@Builder
@Schema(description = "찜한 병원 목록 단일 항목")
public class BookmarkResponse {

    @Schema(description = "병원 ID", example = "101")
    private Long hospitalId;

    @Schema(description = "병원명", example = "강남 제모 클리닉")
    private String name;

    @Schema(description = "도로명 주소", example = "서울 강남구 역삼동 123-4")
    private String address;

    @Schema(description = "태그 목록", example = "[\"여성원장\", \"주차가능\"]")
    private List<String> tags;

    @Schema(description = "찜한 시각", example = "2026-04-22T10:30:00")
    private LocalDateTime bookmarkedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "찜 여부 (찜 목록에서는 항상 true)", example = "true")
    private Boolean isBookmarked;

    /**
     * BookmarkedHospital 도메인 + 태그 목록으로 응답 DTO 생성
     *
     * <p>찜 목록에서는 isBookmarked 가 항상 true다.</p>
     *
     * @param hospital 찜한 병원 도메인
     * @param tags     해당 병원 태그 목록
     * @return 응답 DTO
     */
    public static BookmarkResponse of(BookmarkedHospital hospital, List<String> tags) {
        return BookmarkResponse.builder()
                .hospitalId(hospital.getHospitalId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .tags(tags)
                .bookmarkedAt(hospital.getBookmarkedAt())
                .isBookmarked(true)
                .build();
    }
}