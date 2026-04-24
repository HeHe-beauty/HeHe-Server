package org.dev.hehe.mapper.schedule;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dev.hehe.domain.schedule.Schedule;
import org.dev.hehe.domain.schedule.ScheduleAlarm;
import org.dev.hehe.dto.schedule.ScheduleAlarmInsertDto;
import org.dev.hehe.dto.schedule.ScheduleDateCountDto;

import java.util.List;
import java.util.Optional;

/**
 * 캘린더 일정 MyBatis Mapper
 */
@Mapper
public interface ScheduleMapper {

    /**
     * schedule_id로 일정 단건 조회
     *
     * @param scheduleId 조회할 일정 ID
     * @return 일정 도메인 객체 (없으면 Optional.empty())
     */
    @Select("""
            SELECT schedule_id, user_id, hospital_name, procedure_name,
                   visit_time, alarm_enabled, created_at, updated_at
            FROM tb_schedule
            WHERE schedule_id = #{scheduleId}
            """)
    Optional<Schedule> findScheduleById(@Param("scheduleId") Long scheduleId);

    /**
     * 특정 유저의 지정 기간 내 일정 목록 조회 (visit_time ASC)
     *
     * @param userId    조회할 유저 ID
     * @param startTime 시작 시각 (Unix timestamp, 포함)
     * @param endTime   종료 시각 (Unix timestamp, 미포함)
     * @return 일정 목록
     */
    @Select("""
            SELECT schedule_id, user_id, hospital_name, procedure_name,
                   visit_time, alarm_enabled, created_at, updated_at
            FROM tb_schedule
            WHERE user_id   = #{userId}
              AND visit_time >= #{startTime}
              AND visit_time  < #{endTime}
            ORDER BY visit_time ASC
            """)
    List<Schedule> findSchedulesByUserIdAndPeriod(@Param("userId") Long userId,
                                                  @Param("startTime") long startTime,
                                                  @Param("endTime") long endTime);

    /**
     * 특정 유저의 예정 일정을 visit_time 오름차순으로 N건 조회
     *
     * <p>현재 시각(nowTime) 이후의 일정만 조회하며, 가장 가까운 순서대로 limit 개를 반환한다.</p>
     *
     * @param userId  조회할 유저 ID
     * @param nowTime 현재 시각 (Unix timestamp, 포함)
     * @param limit   조회할 최대 건수
     * @return 예정 일정 목록 (visit_time ASC)
     */
    @Select("""
            SELECT schedule_id, user_id, hospital_name, procedure_name,
                   visit_time, alarm_enabled, created_at, updated_at
            FROM tb_schedule
            WHERE user_id   = #{userId}
              AND visit_time >= #{nowTime}
            ORDER BY visit_time ASC
            LIMIT #{limit}
            """)
    List<Schedule> findUpcomingSchedulesByUserId(@Param("userId") Long userId,
                                                 @Param("nowTime") long nowTime,
                                                 @Param("limit") int limit);

    /**
     * 복수 schedule_id에 해당하는 알림 목록 일괄 조회 (alarm_time ASC)
     * N+1 방지를 위해 IN 절로 한 번에 조회
     *
     * @param scheduleIds 조회할 schedule_id 목록
     * @return 알림 목록
     */
    @Select("<script>" +
            "SELECT id, schedule_id, alarm_type, alarm_time, is_sent, created_at, updated_at " +
            "FROM tb_schedule_alarm " +
            "WHERE schedule_id IN " +
            "<foreach item='id' collection='scheduleIds' open='(' separator=',' close=')'>#{id}</foreach> " +
            "ORDER BY alarm_time ASC" +
            "</script>")
    List<ScheduleAlarm> findAlarmsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    /**
     * 일정 단건 INSERT
     *
     * @param scheduleId   생성할 일정 ID
     * @param userId       유저 ID
     * @param hospitalName 병원명
     * @param procedureName 시술명 (nullable)
     * @param visitTime    방문 예정 시각 (Unix timestamp)
     * @param alarmEnabled 알림 마스터 토글
     */
    @Insert("INSERT INTO tb_schedule (schedule_id, user_id, hospital_name, procedure_name, visit_time, alarm_enabled) " +
            "VALUES (#{scheduleId}, #{userId}, #{hospitalName}, #{procedureName}, #{visitTime}, #{alarmEnabled})")
    void insertSchedule(@Param("scheduleId") long scheduleId,
                        @Param("userId") long userId,
                        @Param("hospitalName") String hospitalName,
                        @Param("procedureName") String procedureName,
                        @Param("visitTime") long visitTime,
                        @Param("alarmEnabled") boolean alarmEnabled);

    /**
     * 일정 부분 수정 (PATCH)
     *
     * <p>null 이 아닌 파라미터만 UPDATE 한다. updated_at 은 항상 현재 시각으로 갱신.</p>
     *
     * @param scheduleId    수정할 일정 ID
     * @param hospitalName  변경할 병원명 (null 이면 기존값 유지)
     * @param procedureName 변경할 시술명 (null 이면 기존값 유지)
     * @param visitTime     변경할 방문 시각 (null 이면 기존값 유지)
     */
    @Update("<script>" +
            "UPDATE tb_schedule " +
            "<set>" +
            "  <if test='hospitalName != null'>hospital_name = #{hospitalName},</if>" +
            "  <if test='procedureName != null'>procedure_name = #{procedureName},</if>" +
            "  <if test='visitTime != null'>visit_time = #{visitTime},</if>" +
            "  updated_at = NOW()" +
            "</set>" +
            " WHERE schedule_id = #{scheduleId}" +
            "</script>")
    void updateSchedule(@Param("scheduleId") long scheduleId,
                        @Param("hospitalName") String hospitalName,
                        @Param("procedureName") String procedureName,
                        @Param("visitTime") Long visitTime);

    /**
     * 알림 다건 일괄 INSERT (알림 등록 API에서 호출 예정)
     *
     * @param alarms 삽입할 알림 목록
     */
    @Insert("<script>" +
            "INSERT INTO tb_schedule_alarm (schedule_id, alarm_type, alarm_time, is_sent) VALUES " +
            "<foreach item='a' collection='alarms' separator=','>" +
            "(#{a.scheduleId}, #{a.alarmType}, #{a.alarmTime}, false)" +
            "</foreach>" +
            "</script>")
    void insertScheduleAlarms(@Param("alarms") List<ScheduleAlarmInsertDto> alarms);

    /**
     * 알림 단건 INSERT — 동일 (schedule_id, alarm_type) 이 이미 존재하면 0 반환
     *
     * <p>WHERE NOT EXISTS 로 중복 삽입을 방지하며, affected rows 로 결과를 판별한다.</p>
     *
     * @param scheduleId 일정 ID
     * @param alarmType  알림 유형 (1H, 1D, 3D)
     * @param alarmTime  알림 발송 예정 시각 (Unix timestamp)
     * @return 삽입된 행 수 (1: 성공, 0: 이미 존재)
     */
    @Insert("""
            INSERT INTO tb_schedule_alarm (schedule_id, alarm_type, alarm_time, is_sent)
            SELECT #{scheduleId}, #{alarmType}, #{alarmTime}, false
            WHERE NOT EXISTS (
                SELECT 1 FROM tb_schedule_alarm
                WHERE schedule_id = #{scheduleId} AND alarm_type = #{alarmType}
            )
            """)
    int insertScheduleAlarmIfNotExists(@Param("scheduleId") long scheduleId,
                                       @Param("alarmType") String alarmType,
                                       @Param("alarmTime") long alarmTime);

    /**
     * 알림 단건 DELETE
     *
     * @param scheduleId 일정 ID
     * @param alarmType  삭제할 알림 유형
     * @return 삭제된 행 수 (1: 성공, 0: 존재하지 않음)
     */
    @Delete("""
            DELETE FROM tb_schedule_alarm
            WHERE schedule_id = #{scheduleId} AND alarm_type = #{alarmType}
            """)
    int deleteScheduleAlarm(@Param("scheduleId") long scheduleId,
                            @Param("alarmType") String alarmType);

    /**
     * 특정 일정의 알림 전체 DELETE
     *
     * <p>일정 삭제 전 선행 호출하여 연관 알림을 모두 제거한다.</p>
     *
     * @param scheduleId 일정 ID
     */
    @Delete("DELETE FROM tb_schedule_alarm WHERE schedule_id = #{scheduleId}")
    void deleteAllAlarmsByScheduleId(@Param("scheduleId") long scheduleId);

    /**
     * 일정 단건 DELETE
     *
     * @param scheduleId 삭제할 일정 ID
     * @return 삭제된 행 수 (1: 성공, 0: 존재하지 않음)
     */
    @Delete("DELETE FROM tb_schedule WHERE schedule_id = #{scheduleId}")
    int deleteSchedule(@Param("scheduleId") long scheduleId);

    /**
     * 특정 유저의 전체 일정을 날짜별로 그룹화하여 예약 건수 조회
     *
     * <p>visit_time(Unix timestamp)을 날짜(yyyy-MM-dd)로 변환 후 GROUP BY</p>
     * <p>예약이 0건인 날짜는 결과에 포함되지 않음</p>
     *
     * @param userId 조회할 유저 ID
     * @return 날짜별 예약 건수 목록 (date ASC)
     */
    @Select("""
            SELECT DATE_FORMAT(FROM_UNIXTIME(visit_time), '%Y-%m-%d') AS date,
                   COUNT(*) AS count
            FROM tb_schedule
            WHERE user_id = #{userId}
            GROUP BY DATE_FORMAT(FROM_UNIXTIME(visit_time), '%Y-%m-%d')
            ORDER BY date ASC
            """)
    List<ScheduleDateCountDto> findScheduleCountGroupByDate(@Param("userId") Long userId);

    /**
     * 유저의 예정 예약 수 조회 (현재 시각 이후)
     *
     * @param userId  유저 ID
     * @param nowTime 현재 시각 (Unix timestamp, 포함)
     * @return 예정 예약 수
     */
    @Select("SELECT COUNT(*) FROM tb_schedule WHERE user_id = #{userId} AND visit_time >= #{nowTime}")
    int countUpcomingSchedules(@Param("userId") Long userId, @Param("nowTime") long nowTime);
}