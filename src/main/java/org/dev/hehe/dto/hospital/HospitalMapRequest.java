package org.dev.hehe.dto.hospital;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 지도 클러스터 조회 요청 파라미터
 *
 * <p>GET /api/v1/hospitals/map 의 쿼리 파라미터를 바인딩한다.</p>
 */
@Getter
@Setter
@Schema(description = "지도 클러스터 조회 요청")
public class HospitalMapRequest {

    @NotNull
    @Schema(description = "뷰포트 남서 위도", example = "37.47", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double swLat;

    @NotNull
    @Schema(description = "뷰포트 남서 경도", example = "126.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double swLng;

    @NotNull
    @Schema(description = "뷰포트 북동 위도", example = "37.54", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double neLat;

    @NotNull
    @Schema(description = "뷰포트 북동 경도", example = "127.07", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double neLng;

    @NotNull
    @Schema(description = "현재 지도 줌 레벨 (1~21)", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer zoomLevel;

    // TODO 장비를 다중으로 선택시 수정 필요
    @Schema(description = "장비 필터 ID (없으면 전체 병원 조회)", example = "1")
    private Long equipId;
}