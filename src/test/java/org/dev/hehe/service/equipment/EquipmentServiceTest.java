package org.dev.hehe.service.equipment;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.equipment.Equipment;
import org.dev.hehe.dto.equipment.EquipmentResponse;
import org.dev.hehe.mapper.equipment.EquipmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

/**
 * EquipmentService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentService 테스트")
class EquipmentServiceTest {

    @Mock
    private EquipmentMapper equipmentMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment createEquipment(Long equipId, String modelName, int displayOrder) {
        Equipment equipment = new Equipment();
        ReflectionTestUtils.setField(equipment, "equipId", equipId);
        ReflectionTestUtils.setField(equipment, "modelName", modelName);
        ReflectionTestUtils.setField(equipment, "isMainDisplay", true);
        ReflectionTestUtils.setField(equipment, "displayOrder", displayOrder);
        return equipment;
    }

    @Test
    @DisplayName("메인 노출 기기 목록 조회 및 DTO 변환 검증")
    void getMainDisplayEquipments_success() {
        // given
        List<Equipment> mockEquipments = List.of(
                createEquipment(1L, "젠틀맥스프로플러스", 1),
                createEquipment(2L, "아포지플러스", 2),
                createEquipment(3L, "클라리티2", 3)
        );
        given(equipmentMapper.findMainDisplayEquipments()).willReturn(mockEquipments);

        // when
        List<EquipmentResponse> result = equipmentService.getMainDisplayEquipments();

        // then: 개수 및 DTO 필드 매핑 검증
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getEquipId()).isEqualTo(1L);
        assertThat(result.get(0).getModelName()).isEqualTo("젠틀맥스프로플러스");
        assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(result.get(2).getModelName()).isEqualTo("클라리티2");

        verify(equipmentMapper).findMainDisplayEquipments();
    }

    @Test
    @DisplayName("메인 노출 기기가 없을 때 빈 리스트 반환")
    void getMainDisplayEquipments_emptyResult() {
        // given
        given(equipmentMapper.findMainDisplayEquipments()).willReturn(List.of());

        // when
        List<EquipmentResponse> result = equipmentService.getMainDisplayEquipments();

        // then
        assertThat(result).isEmpty();
        verify(equipmentMapper).findMainDisplayEquipments();
    }

    @Test
    @DisplayName("DB 조회 중 RuntimeException 발생 시 예외 전파")
    void getMainDisplayEquipments_mapperThrowsException() {
        // given
        willThrow(new RuntimeException("DB connection error"))
                .given(equipmentMapper).findMainDisplayEquipments();

        // when & then: GlobalExceptionHandler가 최종 처리
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> equipmentService.getMainDisplayEquipments())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB connection error");

        verify(equipmentMapper).findMainDisplayEquipments();
    }

    // =============================================
    // getEquipmentById 테스트
    // =============================================

    @Test
    @DisplayName("기기 단건 조회 성공")
    void getEquipmentById_success() {
        // given
        Equipment equipment = createEquipment(1L, "젠틀맥스프로플러스", 1);
        given(equipmentMapper.findByEquipId(1L)).willReturn(Optional.of(equipment));

        // when
        EquipmentResponse result = equipmentService.getEquipmentById(1L);

        // then
        assertThat(result.getEquipId()).isEqualTo(1L);
        assertThat(result.getModelName()).isEqualTo("젠틀맥스프로플러스");
        assertThat(result.getDisplayOrder()).isEqualTo(1);
        verify(equipmentMapper).findByEquipId(1L);
    }

    @Test
    @DisplayName("존재하지 않는 equipId 조회 시 CommonException(EQUIPMENT_NOT_FOUND) 발생")
    void getEquipmentById_notFound() {
        // given
        given(equipmentMapper.findByEquipId(999L)).willReturn(Optional.empty());

        // when & then: H002 에러코드 검증
        assertThatThrownBy(() -> equipmentService.getEquipmentById(999L))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> {
                    CommonException ce = (CommonException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.EQUIPMENT_NOT_FOUND);
                    assertThat(ce.getErrorCode().getCode()).isEqualTo("H002");
                    assertThat(ce.getMessage()).isEqualTo(ErrorCode.EQUIPMENT_NOT_FOUND.getMessage());
                });

        verify(equipmentMapper).findByEquipId(999L);
    }
}