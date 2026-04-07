package org.dev.hehe.dto.hospital;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.hospital.HospitalDetail;

import java.util.List;

/**
 * 병원 상세 조회 응답 DTO
 */
@Getter
@Builder
@Schema(description = "병원 상세 응답")
public class HospitalDetailResponse {

    @Schema(description = "병원 ID", example = "101")
    private Long hospitalId;

    @Schema(description = "병원명", example = "강남 제모 클리닉")
    private String name;

    @Schema(description = "도로명 주소", example = "서울 강남구 역삼동 123-4")
    private String address;

    @Schema(description = "위도", example = "37.512")
    private Double lat;

    @Schema(description = "경도", example = "127.059")
    private Double lng;

    @Schema(description = "문의 전화번호", example = "02-1234-5678")
    private String contactNumber;

    @Schema(description = "외부 예약 링크 (없으면 미노출)", example = "https://example.com")
    private String contactUrl;

    @Schema(description = "태그 목록", example = "[\"여성원장\", \"주차가능\"]")
    private List<String> tags;

    @Schema(description = "보유 장비 목록")
    private List<HospitalEquipmentInfo> equipments;

    /**
     * HospitalDetail 도메인 + 태그 + 장비 목록으로 응답 DTO 생성
     *
     * @param detail     병원 상세 도메인
     * @param tags       태그 목록
     * @param equipments 장비 목록
     * @return 응답 DTO
     */
    public static HospitalDetailResponse of(HospitalDetail detail,
                                            List<String> tags,
                                            List<HospitalEquipmentInfo> equipments) {
        return HospitalDetailResponse.builder()
                .hospitalId(detail.getHospitalId())
                .name(detail.getName())
                .address(detail.getAddress())
                .lat(detail.getLat())
                .lng(detail.getLng())
                .contactNumber(detail.getContactNumber())
                .contactUrl(detail.getContactUrl())
                .tags(tags)
                .equipments(equipments)
                .build();
    }
}