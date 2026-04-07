package org.dev.hehe.controller.equipment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResponse;
import org.dev.hehe.dto.equipment.EquipmentResponse;
import org.dev.hehe.service.equipment.EquipmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 메인 화면 기기 컨트롤러
 * Swagger 명세는 EquipmentApi 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
public class EquipmentController implements EquipmentApiSpecification {

    private final EquipmentService equipmentService;

    @Override
    @GetMapping("/main")
    public ApiResponse<List<EquipmentResponse>> getMainEquipments() {
        log.info("[GET] /api/v1/equipments - 메인 노출 기기 목록 조회 요청");
        List<EquipmentResponse> equipments = equipmentService.getMainDisplayEquipments();
        log.info("메인 노출 기기 조회 완료 - count={}", equipments.size());
        return ApiResponse.ok(equipments);
    }

    @Override
    @GetMapping("/{equipId}")
    public ApiResponse<EquipmentResponse> getEquipmentById(@PathVariable Long equipId) {
        log.info("[GET] /api/v1/equipments/{} - 기기 단건 조회 요청", equipId);
        EquipmentResponse equipment = equipmentService.getEquipmentById(equipId);
        log.info("기기 단건 조회 완료 - equipId={}", equipId);
        return ApiResponse.ok(equipment);
    }
}