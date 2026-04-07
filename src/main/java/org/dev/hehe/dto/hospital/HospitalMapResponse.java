package org.dev.hehe.dto.hospital;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 지도 클러스터 API 응답 DTO
 *
 * - precision : 줌 레벨에서 산출된 좌표 반올림 자릿수.
 *               클라이언트가 목록 API 호출 시 그대로 전달해야 함
 * - items     : 뷰포트 내 클러스터 목록
 */
@Getter
@Builder
@Schema(description = "지도 클러스터 조회 응답")
public class HospitalMapResponse {

    /**
     * 좌표 반올림 자릿수 (줌 레벨로 산출)
     * 목록 API 호출 시 그대로 전달 필요
     */
    @Schema(description = "좌표 반올림 자릿수 — 클러스터 클릭 시 목록 API에 그대로 전달", example = "2")
    private int precision;

    @Schema(description = "클러스터 목록")
    private List<HospitalClusterItem> items;
}