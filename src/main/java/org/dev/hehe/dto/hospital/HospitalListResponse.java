package org.dev.hehe.dto.hospital;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.hospital.HospitalSummary;

import java.util.List;

/**
 * 클러스터 클릭 시 하단 시트에 표시할 병원 목록 단일 항목 응답 DTO
 */
@Getter
@Builder
@Schema(description = "병원 목록 단일 항목")
public class HospitalListResponse {

    @Schema(description = "병원 ID", example = "101")
    private Long hospitalId;

    @Schema(description = "병원명", example = "강남 제모 클리닉")
    private String name;

    @Schema(description = "도로명 주소", example = "서울 강남구 역삼동 123-4")
    private String address;

    @Schema(description = "태그 목록", example = "[\"여성원장\", \"주차가능\"]")
    private List<String> tags;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "찜 여부 (비로그인 시 미노출)", example = "true")
    private Boolean isBookmarked;

    /**
     * HospitalSummary 도메인 + 태그 목록 + 찜 여부로 응답 DTO 생성
     *
     * @param summary      병원 기본 정보 도메인
     * @param tags         해당 병원 태그 목록
     * @param isBookmarked 찜 여부 (비로그인 시 null)
     * @return 응답 DTO
     */
    public static HospitalListResponse of(HospitalSummary summary, List<String> tags, Boolean isBookmarked) {
        return HospitalListResponse.builder()
                .hospitalId(summary.getHospitalId())
                .name(summary.getName())
                .address(summary.getAddress())
                .tags(tags)
                .isBookmarked(isBookmarked)
                .build();
    }
}