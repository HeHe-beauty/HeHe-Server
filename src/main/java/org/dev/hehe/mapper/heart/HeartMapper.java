package org.dev.hehe.mapper.heart;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 하트 MyBatis Mapper
 * - 인증 없이 익명으로 INSERT, 집계는 COUNT(*) 단순 조회
 */
@Mapper
public interface HeartMapper {

    /** tb_heart에 하트 1건 추가 */
    @Insert("INSERT INTO tb_heart (created_at) VALUES (NOW())")
    void insert();

    /** 전체 누적 하트 수 조회 */
    @Select("SELECT COUNT(*) FROM tb_heart")
    Long countAll();
}
