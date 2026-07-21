package org.dev.hehe.mapper.user;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dev.hehe.domain.user.User;

import java.util.List;
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
     * 신규 유저 등록 (회원가입 API 전용)
     * - user_id: 애플리케이션에서 생성한 비즈니스 ID
     * - status 기본값: ACTIVE
     *
     * @param userId       비즈니스 유저 ID
     * @param socialId     소셜 고유 ID
     * @param provider     소셜 제공자 (KAKAO / NAVER)
     * @param nickname     닉네임
     * @param email        소셜 계정 이메일 (제공 동의 없거나 미제공 시 null)
     * @param pushAgreed   일반 푸시 동의
     * @param nightAgreed  야간 푸시 동의
     * @param mktAgreed    마케팅 수신 동의
     * @param isOverAge    14세 이상 동의 (가입 필수)
     * @param termsVersion 가입 시점 약관 버전
     */
    @Insert("""
            INSERT INTO tb_user (user_id, social_id, provider, nickname, email, status,
                                  push_agreed, night_agreed, mkt_agreed, is_over_age, terms_version)
            VALUES (#{userId}, #{socialId}, #{provider}, #{nickname}, #{email}, 'ACTIVE',
                    #{pushAgreed}, #{nightAgreed}, #{mktAgreed}, #{isOverAge}, #{termsVersion})
            """)
    void insertUser(@Param("userId") Long userId,
                    @Param("socialId") String socialId,
                    @Param("provider") String provider,
                    @Param("nickname") String nickname,
                    @Param("email") String email,
                    @Param("pushAgreed") boolean pushAgreed,
                    @Param("nightAgreed") boolean nightAgreed,
                    @Param("mktAgreed") boolean mktAgreed,
                    @Param("isOverAge") boolean isOverAge,
                    @Param("termsVersion") String termsVersion);

    /**
     * 닉네임/이메일 업데이트 (로그인 시 최신 소셜 정보 반영)
     *
     * @param userId   비즈니스 유저 ID
     * @param nickname 최신 닉네임
     * @param email    최신 이메일 (제공 동의 없거나 미제공 시 null)
     */
    @Update("UPDATE tb_user SET nickname = #{nickname}, email = #{email} WHERE user_id = #{userId}")
    void updateProfile(@Param("userId") Long userId, @Param("nickname") String nickname, @Param("email") String email);

    /**
     * 알림 동의 여부 부분 수정 (null인 필드는 기존값 유지)
     *
     * @param userId      비즈니스 유저 ID
     * @param pushAgreed  일반 푸시 동의 (null이면 기존값 유지)
     * @param nightAgreed 야간 푸시 동의 (null이면 기존값 유지)
     * @param mktAgreed   마케팅 수신 동의 (null이면 기존값 유지)
     */
    @Update("<script>" +
            "UPDATE tb_user " +
            "<set>" +
            "  <if test='pushAgreed != null'>push_agreed = #{pushAgreed},</if>" +
            "  <if test='nightAgreed != null'>night_agreed = #{nightAgreed},</if>" +
            "  <if test='mktAgreed != null'>mkt_agreed = #{mktAgreed},</if>" +
            "  updated_at = NOW()" +
            "</set>" +
            " WHERE user_id = #{userId}" +
            "</script>")
    void updateAgreements(@Param("userId") Long userId,
                          @Param("pushAgreed") Boolean pushAgreed,
                          @Param("nightAgreed") Boolean nightAgreed,
                          @Param("mktAgreed") Boolean mktAgreed);

    /**
     * 회원 탈퇴 처리 (소프트 삭제)
     *
     * <p>status를 LEAVE로 전환하고, social_id/email을 tombstone 처리하여 UNIQUE 제약을 비운다.
     * 이후 같은 소셜 계정으로 재가입하면 로그인 로직 변경 없이 신규 유저(새 user_id)로 INSERT된다.</p>
     *
     * @param userId       탈퇴할 비즈니스 유저 ID
     * @param unlinkStatus 소셜 unlink 결과 (SUCCESS/FAILED/NOT_SUPPORTED, 미시도 시 null)
     */
    @Update("""
            UPDATE tb_user
            SET status = 'LEAVE',
                social_id = CONCAT('LEFT_', user_id, '_', social_id),
                email = CASE WHEN email IS NOT NULL THEN CONCAT('left_', user_id, '_', email) ELSE email END,
                unlink_status = #{unlinkStatus},
                updated_at = NOW()
            WHERE user_id = #{userId}
            """)
    void withdrawUser(@Param("userId") Long userId, @Param("unlinkStatus") String unlinkStatus);

    /**
     * 하드 삭제 배치 대상 유저 ID 조회
     *
     * <p>ponytail: 보관 기간(30일)은 정책 값이 아직 안 바뀌어 SQL에 하드코딩. 정책이 바뀌면 파라미터로 분리.</p>
     *
     * @return status=LEAVE 이고 탈퇴(updated_at) 후 30일 지난 유저 ID 목록
     */
    @Select("SELECT user_id FROM tb_user WHERE status = 'LEAVE' AND updated_at < NOW() - INTERVAL 30 DAY")
    List<Long> findLeaveUserIdsForPurge();

    /**
     * 유저 물리 삭제 (하드 삭제 배치 전용)
     *
     * @param userId 삭제할 비즈니스 유저 ID
     */
    @Delete("DELETE FROM tb_user WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}