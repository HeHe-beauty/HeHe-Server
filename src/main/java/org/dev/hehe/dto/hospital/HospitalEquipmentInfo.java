package org.dev.hehe.dto.hospital;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 병원 상세 응답에 포함되는 보유 장비 정보
 * MyBatis가 tb_hospital_equipment JOIN tb_equipment 쿼리 결과를 직접 매핑
 */
@Getter
@Schema(description = "병원 보유 장비 정보")
public class HospitalEquipmentInfo {

    @Schema(description = "장비 모델명", example = "젠틀맥스프로")
    private String modelName;

    @Schema(description = "병원 보유 대수", example = "2")
    private int totalCount;
}