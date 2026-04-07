package org.dev.hehe.controller.equipment;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.equipment.EquipmentResponse;
import org.dev.hehe.service.equipment.EquipmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * EquipmentController 단위 테스트
 * - @WebMvcTest: Controller 레이어만 로드 (Service는 Mock 처리)
 */
@WebMvcTest(EquipmentController.class)
@DisplayName("EquipmentController 테스트")
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentService equipmentService;

    @Test
    @DisplayName("GET /api/v1/equipments - 메인 노출 기기 목록 정상 반환")
    void getMainEquipments_success() throws Exception {
        // given
        List<EquipmentResponse> mockEquipments = List.of(
                EquipmentResponse.builder().equipId(1L).modelName("젠틀맥스프로플러스").displayOrder(1).build(),
                EquipmentResponse.builder().equipId(2L).modelName("아포지플러스").displayOrder(2).build(),
                EquipmentResponse.builder().equipId(3L).modelName("클라리티2").displayOrder(3).build()
        );
        given(equipmentService.getMainDisplayEquipments()).willReturn(mockEquipments);

        // when & then
        mockMvc.perform(get("/api/v1/equipments/main"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].equipId").value(1))
                .andExpect(jsonPath("$.data[0].modelName").value("젠틀맥스프로플러스"))
                .andExpect(jsonPath("$.data[0].displayOrder").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/equipments - 메인 노출 기기 없을 때 빈 배열 반환")
    void getMainEquipments_emptyList() throws Exception {
        // given
        given(equipmentService.getMainDisplayEquipments()).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/equipments/main"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // =============================================
    // GET /api/v1/equipments/{equipId} 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/equipments/{equipId} - 단건 조회 성공")
    void getEquipmentById_success() throws Exception {
        // given
        EquipmentResponse mockEquipment =
                EquipmentResponse.builder().equipId(1L).modelName("젠틀맥스프로플러스").displayOrder(1).build();
        given(equipmentService.getEquipmentById(1L)).willReturn(mockEquipment);

        // when & then
        mockMvc.perform(get("/api/v1/equipments/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.equipId").value(1))
                .andExpect(jsonPath("$.data.modelName").value("젠틀맥스프로플러스"));
    }

    @Test
    @DisplayName("GET /api/v1/equipments/{equipId} - 존재하지 않는 ID 요청 시 404 반환 (H002)")
    void getEquipmentById_notFound() throws Exception {
        // given: 서비스에서 CommonException(EQUIPMENT_NOT_FOUND) 발생
        willThrow(new CommonException(ErrorCode.EQUIPMENT_NOT_FOUND))
                .given(equipmentService).getEquipmentById(999L);

        // when & then: GlobalExceptionHandler → 404, errorCode=H002
        mockMvc.perform(get("/api/v1/equipments/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("H002"))
                .andExpect(jsonPath("$.message").value(ErrorCode.EQUIPMENT_NOT_FOUND.getMessage()));
    }
}