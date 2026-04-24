package org.dev.hehe.mapper.pushtoken;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

/**
 * FCM 푸시 토큰 MyBatis Mapper
 */
@Mapper
public interface PushTokenMapper {

    /**
     * FCM 토큰 등록 또는 갱신 (upsert)
     *
     * <p>token이 이미 존재하면 userId·platform·notificationPermissionGranted·isActive를 갱신한다.
     * 기기가 다른 유저에게 넘어간 경우에도 token의 실제 소유자(기기 보유자)로 userId가 교체된다.</p>
     *
     * @param userId                        유저 ID
     * @param token                         FCM 디바이스 토큰
     * @param platform                      플랫폼 (ANDROID/IOS)
     * @param notificationPermissionGranted OS 알림 권한 허용 여부
     */
    @Insert("""
            INSERT INTO tb_push_token (user_id, token, platform, notification_permission_granted, is_active)
            VALUES (#{userId}, #{token}, #{platform}, #{notificationPermissionGranted}, 1)
            ON DUPLICATE KEY UPDATE
                user_id                         = VALUES(user_id),
                platform                        = VALUES(platform),
                notification_permission_granted = VALUES(notification_permission_granted),
                is_active                       = 1,
                updated_at                      = NOW()
            """)
    void upsertToken(@Param("userId") Long userId,
                     @Param("token") String token,
                     @Param("platform") String platform,
                     @Param("notificationPermissionGranted") boolean notificationPermissionGranted);

    /**
     * FCM 토큰 소프트 비활성화 (is_active = 0)
     *
     * <p>userId + token 을 함께 검증하여 본인 토큰만 비활성화한다.
     * 토큰이 존재하지 않거나 다른 유저의 토큰이면 0을 반환하며, 예외는 발생하지 않는다 (idempotent).</p>
     *
     * @param userId 유저 ID
     * @param token  비활성화할 FCM 토큰
     * @return 업데이트된 행 수 (0 또는 1)
     */
    @Update("UPDATE tb_push_token SET is_active = 0, updated_at = NOW() WHERE user_id = #{userId} AND token = #{token}")
    int deactivateToken(@Param("userId") Long userId, @Param("token") String token);
}
