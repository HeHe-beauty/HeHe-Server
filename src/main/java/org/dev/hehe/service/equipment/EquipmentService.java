package org.dev.hehe.service.equipment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.equipment.EquipmentResponse;
import org.dev.hehe.mapper.equipment.EquipmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 장비 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentMapper equipmentMapper;

    /**
     * 메인 화면 노출 기기 목록 조회
     * - is_main_display = true 인 기기만 반환 (최대 3개)
     *
     * @return 메인 노출 기기 목록 (display_order ASC)
     */
    public List<EquipmentResponse> getMainDisplayEquipments() {
        List<EquipmentResponse> equipments = equipmentMapper.findMainDisplayEquipments()
                .stream()
                .map(EquipmentResponse::from)
                .toList();
        log.debug("메인 노출 기기 조회 완료 - count={}", equipments.size());
        return equipments;
    }

    /**
     * equip_id로 기기 단건 조회
     * 존재하지 않으면 EQUIPMENT_NOT_FOUND 예외 발생
     *
     * @param equipId 조회할 기기 ID
     * @return 기기 응답 DTO
     * @throws CommonException EQUIPMENT_NOT_FOUND (H002)
     */
    public EquipmentResponse getEquipmentById(Long equipId) {
        log.debug("기기 단건 조회 - equipId={}", equipId);
        return equipmentMapper.findByEquipId(equipId)
                .map(EquipmentResponse::from)
                .orElseThrow(() -> {
                    log.warn("기기를 찾을 수 없음 - equipId={}", equipId);
                    return new CommonException(ErrorCode.EQUIPMENT_NOT_FOUND);
                });
    }
}