package org.dev.hehe.mapper.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dev.hehe.domain.user.User;

import java.util.Optional;

/**
 * tb_user MyBatis 매퍼
 */
@Mapper
public interface UserMapper {

    /**
     * 소셜 제공자 + 소셜 ID로 유저 조회
     *
     * @param provider  소셜 제공자 (KAKAO / NAVER)
     * @param socialId  소셜 고유 ID
     * @return 유저 (없으면 Optional.empty)
     */
    @Select("SELECT * FROM tb_user WHERE provider = #{provider} AND social_id = #{socialId}")
    Optional<User> findByProviderAndSocialId(@Param("provider") String provider,
                                              @Param("socialId") String socialId);

    /**
     * 비즈니스 유저 ID로 유저 조회
     *
     * @param userId 비즈니스 유저 ID
     * @return 유저 (없으면 Optional.empty)
     */
    @Select("SELECT * FROM tb_user WHERE user_id = #{userId}")
    Optional<User> findByUserId(@Param("userId") Long userId);

    /**
     * 신규 유저 등록
     * - user_id: 애플리케이션에서 생성한 비즈니스 ID
     * - status 기본값: ACTIVE
     *
     * @param userId   비즈니스 유저 ID
     * @param socialId 소셜 고유 ID
     * @param provider 소셜 제공자 (KAKAO / NAVER)
     * @param nickname 닉네임
     */
    @Insert("INSERT INTO tb_user (user_id, social_id, provider, nickname, status) " +
            "VALUES (#{userId}, #{socialId}, #{provider}, #{nickname}, 'ACTIVE')")
    @Options(useGeneratedKeys = true, keyProperty = "generatedId")
    void insertUser(@Param("userId") Long userId,
                    @Param("socialId") String socialId,
                    @Param("provider") String provider,
                    @Param("nickname") String nickname);

    /**
     * 닉네임 업데이트 (로그인 시 최신 소셜 닉네임 반영)
     *
     * @param userId   비즈니스 유저 ID
     * @param nickname 최신 닉네임
     */
    @Update("UPDATE tb_user SET nickname = #{nickname} WHERE user_id = #{userId}")
    void updateNickname(@Param("userId") Long userId, @Param("nickname") String nickname);
}