package org.dev.hehe.dto.equipment;

import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.equipment.Equipment;

/**
 * 메인 화면 기기 응답 DTO
 */
@Getter
@Builder
public class EquipmentResponse {

    private Long equipId;
    private String modelName;
    private Integer displayOrder;

    public static EquipmentResponse from(Equipment equipment) {
        return EquipmentResponse.builder()
                .equipId(equipment.getEquipId())
                .modelName(equipment.getModelName())
                .displayOrder(equipment.getDisplayOrder())
                .build();
    }
}