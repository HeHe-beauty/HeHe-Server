package org.dev.hehe.dto.hospital;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 지도 클러스터 단일 항목
 * MyBatis가 GROUP BY 집계 쿼리 결과를 직접 매핑
 *
 * - count : 해당 좌표 범위 내 병원 수
 * - lat   : ROUND(ST_Y(location), precision) 반올림 위도
 * - lng   : ROUND(ST_X(location), precision) 반올림 경도
 */
@Getter
@Schema(description = "클러스터 단일 항목")
public class HospitalClusterItem {

    @Schema(description = "클러스터 내 병원 수", example = "23")
    private int count;

    @Schema(description = "클러스터 중심 위도 (반올림된 좌표)", example = "37.50")
    private double lat;

    @Schema(description = "클러스터 중심 경도 (반올림된 좌표)", example = "127.04")
    private double lng;
}