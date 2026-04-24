package org.dev.hehe.dto.recentview;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.recentview.RecentView;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 최근 본 병원 단일 항목 응답 DTO
 */
@Getter
@Builder
@Schema(description = "최근 본 병원 단일 항목")
public class RecentViewResponse {

    @Schema(description = "병원 ID", example = "101")
    private Long hospitalId;

    @Schema(description = "병원명", example = "강남 제모 클리닉")
    private String name;

    @Schema(description = "병원 주소", example = "서울 강남구 테헤란로 123")
    private String address;

    @Schema(description = "병원 태그 목록", example = "[\"젠틀맥스프로\", \"당일예약\"]")
    private List<String> tags;

    @Schema(description = "최근 조회 시각", example = "2026-04-22T10:30:00")
    private LocalDateTime viewedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "찜 여부 (비로그인 시 미노출)", example = "true")
    private Boolean isBookmarked;

    /**
     * RecentView 도메인과 태그 목록, 찜 여부로 응답 DTO 생성
     *
     * @param recentView   최근 본 병원 도메인
     * @param tags         병원 태그 목록
     * @param isBookmarked 찜 여부
     * @return 응답 DTO
     */
    public static RecentViewResponse of(RecentView recentView, List<String> tags, Boolean isBookmarked) {
        return RecentViewResponse.builder()
                .hospitalId(recentView.getHospitalId())
                .name(recentView.getName())
                .address(recentView.getAddress())
                .tags(tags)
                .viewedAt(recentView.getViewedAt())
                .isBookmarked(isBookmarked)
                .build();
    }
}