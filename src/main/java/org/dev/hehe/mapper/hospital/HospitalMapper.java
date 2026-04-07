package org.dev.hehe.mapper.hospital;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dev.hehe.domain.hospital.HospitalDetail;
import org.dev.hehe.domain.hospital.HospitalSummary;
import org.dev.hehe.domain.hospital.HospitalTag;
import org.dev.hehe.dto.hospital.HospitalClusterItem;
import org.dev.hehe.dto.hospital.HospitalEquipmentInfo;

import java.util.List;
import java.util.Optional;

/**
 * 병원 지도 MyBatis Mapper
 */
@Mapper
public interface HospitalMapper {

    /**
     * 뷰포트 내 클러스터 목록 조회
     *
     * <p>ROUND(lat, precision), ROUND(lng, precision) 기준으로 GROUP BY 집계한다.</p>
     * <p>equipId 가 존재하면 해당 장비를 보유한 병원(total_count > 0)만 필터링한다.</p>
     *
     * @param viewport  MBRContains 에 사용할 WKT Polygon 문자열 (서비스에서 생성)
     * @param precision 좌표 반올림 자릿수 (줌 레벨로 산출)
     * @param equipId   장비 필터 (null 이면 전체 병원)
     * @return 클러스터 목록 (count, lat, lng)
     */
    @Select("<script>" +
            "SELECT ROUND(ST_X(h.location), #{precision}) AS lat, " +
            "       ROUND(ST_Y(h.location), #{precision}) AS lng, " +
            "       COUNT(*) AS count " +
            "FROM tb_hospital h " +
            "<if test='equipId != null'>" +
            "INNER JOIN tb_hospital_equipment he " +
            "        ON he.hospital_id = h.hospital_id " +
            "       AND he.equip_id    = #{equipId} " +
            "       AND he.total_count > 0 " +
            "</if>" +
            "WHERE MBRContains(ST_GeomFromText(#{viewport}, 4326), h.location) " +
            "GROUP BY ROUND(ST_X(h.location), #{precision}), " +
            "         ROUND(ST_Y(h.location), #{precision}) " +
            "ORDER BY count DESC" +
            "</script>")
    List<HospitalClusterItem> findClusters(@Param("viewport") String viewport,
                                           @Param("precision") int precision,
                                           @Param("equipId") Long equipId);

    /**
     * 클러스터 내 병원 기본 정보 목록 조회
     *
     * <p>반올림 좌표(lat, lng, precision)로 동일 클러스터에 속한 병원을 조회한다.</p>
     * <p>equipId 가 존재하면 해당 장비를 보유한 병원만 필터링한다.</p>
     * <p>태그는 N+1 방지를 위해 별도 쿼리(findTagsByHospitalIds)로 일괄 조회한다.</p>
     *
     * @param lat       반올림된 위도 (API 1 응답값 그대로)
     * @param lng       반올림된 경도 (API 1 응답값 그대로)
     * @param precision 좌표 반올림 자릿수 (API 1 응답값 그대로)
     * @param equipId   장비 필터 (null 이면 전체)
     * @return 병원 기본 정보 목록
     */
    @Select("<script>" +
            "SELECT h.hospital_id, h.name, h.address " +
            "FROM tb_hospital h " +
            "<if test='equipId != null'>" +
            "INNER JOIN tb_hospital_equipment he " +
            "        ON he.hospital_id = h.hospital_id " +
            "       AND he.equip_id    = #{equipId} " +
            "       AND he.total_count > 0 " +
            "</if>" +
            "WHERE ROUND(ST_X(h.location), #{precision}) = #{lat} " +
            "  AND ROUND(ST_Y(h.location), #{precision}) = #{lng}" +
            "</script>")
    List<HospitalSummary> findHospitalsByCluster(@Param("lat") double lat,
                                                  @Param("lng") double lng,
                                                  @Param("precision") int precision,
                                                  @Param("equipId") Long equipId);

    /**
     * 복수 병원의 태그 일괄 조회 (N+1 방지)
     *
     * @param hospitalIds 조회할 병원 ID 목록
     * @return 태그 목록 (hospitalId, tagName)
     */
    @Select("<script>" +
            "SELECT hospital_id, tag_name " +
            "FROM tb_hospital_tag " +
            "WHERE hospital_id IN " +
            "<foreach item='id' collection='hospitalIds' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<HospitalTag> findTagsByHospitalIds(@Param("hospitalIds") List<Long> hospitalIds);

    /**
     * 병원 상세 단건 조회
     *
     * <p>location POINT 에서 ST_X(위도), ST_Y(경도)를 분리하여 반환한다.</p>
     * <p>SRID 4326은 POINT(lat lng) 축 순서이므로 ST_X = 위도, ST_Y = 경도다.</p>
     *
     * @param hospitalId 조회할 병원 ID
     * @return 병원 상세 도메인 (없으면 Optional.empty())
     */
    @Select("""
            SELECT hospital_id,
                   name,
                   address,
                   ST_X(location) AS lat,
                   ST_Y(location) AS lng,
                   contact_number,
                   contact_url
            FROM tb_hospital
            WHERE hospital_id = #{hospitalId}
            """)
    Optional<HospitalDetail> findHospitalById(@Param("hospitalId") Long hospitalId);

    /**
     * 병원 단건 태그 조회 (상세 API 전용)
     *
     * @param hospitalId 조회할 병원 ID
     * @return 태그명 목록
     */
    @Select("SELECT tag_name FROM tb_hospital_tag WHERE hospital_id = #{hospitalId}")
    List<String> findTagNamesByHospitalId(@Param("hospitalId") Long hospitalId);

    /**
     * 병원 보유 장비 목록 조회 (상세 API 전용)
     *
     * <p>total_count = 0 인 장비는 제외한다.</p>
     *
     * @param hospitalId 조회할 병원 ID
     * @return 장비 정보 목록 (modelName, totalCount)
     */
    @Select("""
            SELECT e.model_name,
                   he.total_count
            FROM tb_hospital_equipment he
            JOIN tb_equipment e ON e.equip_id = he.equip_id
            WHERE he.hospital_id = #{hospitalId}
              AND he.total_count > 0
            ORDER BY e.display_order ASC
            """)
    List<HospitalEquipmentInfo> findEquipmentsByHospitalId(@Param("hospitalId") Long hospitalId);
}