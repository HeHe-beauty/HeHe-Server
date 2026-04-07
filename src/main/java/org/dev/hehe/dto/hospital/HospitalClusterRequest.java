package org.dev.hehe.dto.hospital;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 클러스터 내 병원 목록 조회 요청 파라미터
 *
 * <p>GET /api/v1/hospitals 의 쿼리 파라미터를 바인딩한다.</p>
 * <p>lat, lng, precision 은 지도 클러스터 API 응답값을 그대로 전달해야 한다.</p>
 */
@Getter
@Setter
@Schema(description = "클러스터 내 병원 목록 조회 요청")
public class HospitalClusterRequest {

    @NotNull
    @Schema(description = "클러스터 반올림 위도 (map API 응답값 그대로)", example = "37.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double lat;

    @NotNull
    @Schema(description = "클러스터 반올림 경도 (map API 응답값 그대로)", example = "127.04", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double lng;

    @NotNull
    @Schema(description = "좌표 반올림 자릿수 (map API 응답값 그대로, 1~4)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer precision;

    @Schema(description = "장비 필터 ID (없으면 전체 병원 조회)", example = "1")
    private Long equipId;
}