package org.dev.hehe.mapper.recentview;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dev.hehe.domain.recentview.RecentView;

import java.util.List;

/**
 * 최근 본 병원 MyBatis Mapper
 */
@Mapper
public interface RecentViewMapper {

    /**
     * 유저의 최근 본 병원 목록 조회 (최신 순 10건)
     *
     * <p>tb_recent_view와 tb_hospital을 JOIN하여 병원 기본 정보와 조회 시각을 반환한다.</p>
     * <p>태그는 N+1 방지를 위해 서비스에서 HospitalMapper로 별도 조회한다.</p>
     *
     * @param userId 조회할 유저 ID
     * @return 최근 본 병원 목록 (최신 조회 순, 최대 10건)
     */
    @Select("""
            SELECT r.id,
                   r.hospital_id,
                   h.name,
                   h.address,
                   r.viewed_at
            FROM tb_recent_view r
            JOIN tb_hospital h ON r.hospital_id = h.hospital_id
            WHERE r.user_id = #{userId}
            ORDER BY r.viewed_at DESC
            LIMIT 10
            """)
    List<RecentView> findRecentViews(@Param("userId") Long userId);

    /**
     * 최근 본 병원 기록 (upsert)
     *
     * <p>동일한 (user_id, hospital_id) 조합이 이미 존재하면 viewed_at을 현재 시각으로 갱신한다.</p>
     *
     * @param userId     유저 ID
     * @param hospitalId 병원 ID
     */
    @Insert("INSERT INTO tb_recent_view (user_id, hospital_id) VALUES (#{userId}, #{hospitalId}) " +
            "ON DUPLICATE KEY UPDATE viewed_at = NOW()")
    void upsertRecentView(@Param("userId") Long userId, @Param("hospitalId") Long hospitalId);
}