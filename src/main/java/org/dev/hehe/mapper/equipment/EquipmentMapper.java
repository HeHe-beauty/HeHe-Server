package org.dev.hehe.mapper.equipment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.dev.hehe.domain.equipment.Equipment;

import java.util.List;
import java.util.Optional;

/**
 * 장비 MyBatis Mapper
 */
@Mapper
public interface EquipmentMapper {

    /**
     * 메인 화면 노출 기기 조회
     * - is_main_display = true 인 기기만 반환
     * - display_order ASC 정렬
     */
    @Select("""
            SELECT equip_id, model_name, is_main_display, display_order, created_at, updated_at
            FROM tb_equipment
            WHERE is_main_display = true
            ORDER BY display_order ASC
            """)
    List<Equipment> findMainDisplayEquipments();

    /**
     * equip_id로 기기 단건 조회
     */
    @Select("""
            SELECT equip_id, model_name, is_main_display, display_order, created_at, updated_at
            FROM tb_equipment
            WHERE equip_id = #{equipId}
            """)
    Optional<Equipment> findByEquipId(Long equipId);
}