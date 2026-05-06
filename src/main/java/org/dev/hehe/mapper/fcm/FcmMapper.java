package org.dev.hehe.mapper.fcm;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dev.hehe.dto.fcm.PendingAlarmDto;

import java.util.List;

/**
 * FCM 알림 발송 관련 MyBatis Mapper
 */
@Mapper
public interface FcmMapper {

    /**
     * 발송 대상 미발송 알람 목록 조회
     *
     * <p>alarm_time이 현재 시각 이하이고 아직 발송되지 않은(is_sent=false) 알람을 조회한다.
     * 일정 알림 마스터 토글(alarm_enabled)이 꺼져 있으면 제외된다.
     * 알림 동의 여부는 FE에서 관리하므로 서버에서 필터링하지 않는다.</p>
     *
     * @return 발송 대상 알람 목록
     */
    @Select("""
            SELECT sa.id          AS alarm_id,
                   sa.alarm_type,
                   sa.alarm_time,
                   s.user_id,
                   s.hospital_name
            FROM tb_schedule_alarm sa
            JOIN tb_schedule s ON s.schedule_id = sa.schedule_id
            WHERE sa.alarm_time <= UNIX_TIMESTAMP()
              AND sa.is_sent      = false
              AND s.alarm_enabled = true
            """)
    List<PendingAlarmDto> findPendingAlarms();

    /**
     * 유저의 활성 FCM 토큰 목록 조회
     *
     * <p>is_active=1 이고 OS 알림 권한이 허용된 토큰만 반환한다.</p>
     *
     * @param userId 유저 ID
     * @return FCM 토큰 문자열 목록
     */
    @Select("""
            SELECT token
            FROM tb_push_token
            WHERE user_id                         = #{userId}
              AND is_active                       = 1
              AND notification_permission_granted = 1
            """)
    List<String> findActiveTokensByUserId(@Param("userId") Long userId);

    /**
     * 알람 발송 완료 처리 (is_sent = true)
     *
     * @param alarmId 완료 처리할 tb_schedule_alarm.id
     */
    @Update("UPDATE tb_schedule_alarm SET is_sent = true, updated_at = NOW() WHERE id = #{alarmId}")
    void markAlarmAsSent(@Param("alarmId") Long alarmId);

    /**
     * 만료 또는 무효 FCM 토큰 비활성화
     *
     * <p>Firebase로부터 UNREGISTERED / INVALID_ARGUMENT 오류를 받은 토큰을 비활성화한다.</p>
     *
     * @param token 비활성화할 FCM 토큰
     */
    @Update("UPDATE tb_push_token SET is_active = 0, updated_at = NOW() WHERE token = #{token}")
    void deactivateExpiredToken(@Param("token") String token);

    /**
     * FCM 발송 이력 저장
     *
     * @param userId          발송 대상 유저 ID
     * @param scheduleAlarmId 트리거된 스케줄 알람 ID
     * @param pushTitle       알림 제목
     * @param pushContent     알림 본문
     * @param successCount    발송 성공 건수
     * @param failCount       발송 실패 건수
     */
    @Insert("""
            INSERT INTO tb_push_history
                (user_id, schedule_alarm_id, push_title, push_content, target_type, success_count, fail_count, sent_at)
            VALUES
                (#{userId}, #{scheduleAlarmId}, #{pushTitle}, #{pushContent}, 'INDIVIDUAL', #{successCount}, #{failCount}, NOW())
            """)
    void insertPushHistory(@Param("userId") Long userId,
                           @Param("scheduleAlarmId") Long scheduleAlarmId,
                           @Param("pushTitle") String pushTitle,
                           @Param("pushContent") String pushContent,
                           @Param("successCount") int successCount,
                           @Param("failCount") int failCount);
}