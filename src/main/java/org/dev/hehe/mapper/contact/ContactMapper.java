package org.dev.hehe.mapper.contact;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dev.hehe.domain.contact.ContactHistory;

import java.util.List;

/**
 * 문의 내역 MyBatis Mapper
 */
@Mapper
public interface ContactMapper {

    /**
     * 유저의 문의 내역 목록 조회 (소프트 삭제 제외)
     *
     * <p>tb_contact_history와 tb_hospital을 JOIN하여 병원명을 함께 반환한다.</p>
     * <p>is_deleted = 0 인 항목만 조회하며, 최신 문의 순으로 정렬한다.</p>
     *
     * @param userId 조회할 유저 ID
     * @return 문의 내역 목록 (최신 문의 순)
     */
    @Select("""
            SELECT c.id,
                   c.hospital_id,
                   h.name AS hospital_name,
                   c.contact_type,
                   c.created_at
            FROM tb_contact_history c
            JOIN tb_hospital h ON c.hospital_id = h.hospital_id
            WHERE c.user_id = #{userId}
              AND c.is_deleted = 0
            ORDER BY c.created_at DESC
            """)
    List<ContactHistory> findContactHistories(@Param("userId") Long userId);

    /**
     * 문의 내역 저장
     *
     * @param userId      유저 ID
     * @param hospitalId  병원 ID
     * @param contactType 문의 유형 (CALL/CHAT/VISIT)
     */
    @Insert("INSERT INTO tb_contact_history (user_id, hospital_id, contact_type) VALUES (#{userId}, #{hospitalId}, #{contactType})")
    void insertContact(@Param("userId") Long userId,
                       @Param("hospitalId") Long hospitalId,
                       @Param("contactType") String contactType);

    /**
     * 유저 소유의 삭제되지 않은 문의 내역 존재 여부 확인
     *
     * @param contactId 문의 내역 ID
     * @param userId    유저 ID
     * @return 존재하면 true
     */
    @Select("SELECT COUNT(*) > 0 FROM tb_contact_history WHERE id = #{contactId} AND user_id = #{userId} AND is_deleted = 0")
    boolean existsContact(@Param("contactId") Long contactId, @Param("userId") Long userId);

    /**
     * 문의 내역 소프트 삭제 (is_deleted = 1)
     *
     * @param contactId 문의 내역 ID
     * @param userId    유저 ID
     */
    @Update("UPDATE tb_contact_history SET is_deleted = 1 WHERE id = #{contactId} AND user_id = #{userId}")
    void softDeleteContact(@Param("contactId") Long contactId, @Param("userId") Long userId);

    /**
     * 유저의 문의 수 조회 (소프트 삭제 제외)
     *
     * @param userId 유저 ID
     * @return 문의 수
     */
    @Select("SELECT COUNT(*) FROM tb_contact_history WHERE user_id = #{userId} AND is_deleted = 0")
    int countContacts(@Param("userId") Long userId);
}