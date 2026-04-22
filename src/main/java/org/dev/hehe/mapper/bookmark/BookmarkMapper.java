package org.dev.hehe.mapper.bookmark;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dev.hehe.domain.bookmark.BookmarkedHospital;

import java.util.List;

/**
 * 찜(북마크) MyBatis Mapper
 */
@Mapper
public interface BookmarkMapper {

    /**
     * 유저의 찜한 병원 목록 조회
     *
     * <p>tb_bookmark와 tb_hospital을 JOIN하여 병원 기본 정보와 찜한 시각을 반환한다.</p>
     * <p>태그는 N+1 방지를 위해 서비스에서 HospitalMapper로 별도 조회한다.</p>
     *
     * @param userId 조회할 유저 ID
     * @return 찜한 병원 목록 (찜한 최신 순)
     */
    @Select("""
            SELECT h.hospital_id,
                   h.name,
                   h.address,
                   b.created_at AS bookmarked_at
            FROM tb_bookmark b
            JOIN tb_hospital h ON b.hospital_id = h.hospital_id
            WHERE b.user_id = #{userId}
            ORDER BY b.created_at DESC
            """)
    List<BookmarkedHospital> findBookmarkedHospitals(@Param("userId") Long userId);

    /**
     * 찜 여부 확인
     *
     * @param userId     유저 ID
     * @param hospitalId 병원 ID
     * @return 찜 존재 여부
     */
    @Select("SELECT COUNT(*) > 0 FROM tb_bookmark WHERE user_id = #{userId} AND hospital_id = #{hospitalId}")
    boolean existsBookmark(@Param("userId") Long userId, @Param("hospitalId") Long hospitalId);

    /**
     * 병원 찜 추가
     *
     * @param userId     찜하는 유저 ID
     * @param hospitalId 찜할 병원 ID
     */
    @Insert("""
            INSERT INTO tb_bookmark (user_id, hospital_id)
            VALUES (#{userId}, #{hospitalId})
            """)
    void insertBookmark(@Param("userId") Long userId, @Param("hospitalId") Long hospitalId);

    /**
     * 병원 찜 삭제
     *
     * @param userId     유저 ID
     * @param hospitalId 병원 ID
     */
    @Delete("""
            DELETE FROM tb_bookmark
            WHERE user_id = #{userId} AND hospital_id = #{hospitalId}
            """)
    void deleteBookmark(@Param("userId") Long userId, @Param("hospitalId") Long hospitalId);

    /**
     * 유저의 찜 수 조회
     *
     * @param userId 유저 ID
     * @return 찜한 병원 수
     */
    @Select("SELECT COUNT(*) FROM tb_bookmark WHERE user_id = #{userId}")
    int countBookmarks(@Param("userId") Long userId);
}