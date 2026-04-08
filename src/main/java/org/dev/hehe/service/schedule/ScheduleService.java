package org.dev.hehe.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.schedule.Schedule;
import org.dev.hehe.domain.schedule.ScheduleAlarm;
import org.dev.hehe.dto.schedule.ScheduleAlarmResponse;
import org.dev.hehe.dto.schedule.ScheduleCreateRequest;
import org.dev.hehe.dto.schedule.ScheduleCreateResponse;
import org.dev.hehe.dto.schedule.ScheduleResponse;
import org.dev.hehe.dto.schedule.ScheduleUpdateRequest;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 캘린더 일정 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;

    /**
     * scheduleId로 일정 단건 조회 (알림 목록 포함)
     *
     * @param scheduleId 조회할 일정 ID
     * @return 일정 상세 응답 DTO (알림 목록 포함)
     * @throws CommonException SCHEDULE_NOT_FOUND — 존재하지 않는 scheduleId
     */
    public ScheduleResponse getScheduleById(Long scheduleId) {
        log.debug("일정 단건 조회 - scheduleId={}", scheduleId);

        Schedule schedule = scheduleMapper.findScheduleById(scheduleId)
                .orElseThrow(() -> {
                    log.warn("일정을 찾을 수 없음 - scheduleId={}", scheduleId);
                    return new CommonException(ErrorCode.SCHEDULE_NOT_FOUND);
                });

        List<ScheduleAlarmResponse> alarms = scheduleMapper
                .findAlarmsByScheduleIds(List.of(scheduleId))
                .stream()
                .map(ScheduleAlarmResponse::from)
                .toList();

        log.debug("일정 단건 조회 완료 - scheduleId={}, alarmCount={}", scheduleId, alarms.size());
        return ScheduleResponse.of(schedule, alarms);
    }

    /**
     * 오늘부터 7일간 일정 목록 조회 (알림 정보 포함)
     *
     * <p>조회 범위: 오늘 00:00:00 (포함) ~ 오늘 + 7일 00:00:00 (미포함)</p>
     * <p>visit_time은 Unix timestamp(seconds) 그대로 반환. 시간대 전환은 FE에서 처리.</p>
     * <p>알림은 schedule_id IN (...) 단일 쿼리로 일괄 조회하여 N+1 방지.</p>
     *
     * <p>TODO: Auth 구현 후 userId를 JWT SecurityContext에서 추출하도록 변경</p>
     *
     * @param userId 조회할 유저 ID
     * @return 7일간 일정 목록 (visit_time ASC, 각 일정에 알림 목록 포함)
     */
    public List<ScheduleResponse> getUpcomingSchedules(Long userId) {
        // 조회 범위: 오늘 00:00:00 (포함) ~ 오늘 + 7일 00:00:00 (미포함)
        ZonedDateTime todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        long startTime = todayStart.toEpochSecond();
        long endTime   = todayStart.plusDays(7).toEpochSecond();

        log.debug("7일 일정 조회 - userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        List<Schedule> schedules = scheduleMapper.findSchedulesByUserIdAndPeriod(userId, startTime, endTime);

        if (schedules.isEmpty()) {
            log.debug("7일 일정 없음 - userId={}", userId);
            return List.of();
        }

        // 알림 일괄 조회 (N+1 방지)
        List<Long> scheduleIds = schedules.stream()
                .map(Schedule::getScheduleId)
                .toList();

        // IN 절 활용해서 일괄 조회
        Map<Long, List<ScheduleAlarmResponse>> alarmMap = scheduleMapper
                .findAlarmsByScheduleIds(scheduleIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ScheduleAlarm::getScheduleId,
                        Collectors.mapping(ScheduleAlarmResponse::from, Collectors.toList())
                ));

        List<ScheduleResponse> result = schedules.stream()
                .map(s -> ScheduleResponse.of(
                        s,
                        alarmMap.getOrDefault(s.getScheduleId(), List.of())
                ))
                .toList();

        log.debug("7일 일정 조회 완료 - userId={}, count={}", userId, result.size());
        return result;
    }

    /**
     * 특정 날짜의 일정 목록 조회 (알림 정보 포함)
     *
     * <p>조회 범위: 입력 날짜 00:00:00 (포함) ~ 다음 날 00:00:00 (미포함)</p>
     * <p>visit_time은 Unix timestamp(seconds) 그대로 반환. 시간대 전환은 FE에서 처리.</p>
     * <p>알림은 schedule_id IN (...) 단일 쿼리로 일괄 조회하여 N+1 방지.</p>
     *
     * <p>TODO: Auth 구현 후 userId를 JWT SecurityContext에서 추출하도록 변경</p>
     *
     * @param userId 조회할 유저 ID
     * @param date   조회할 날짜 문자열 (yyyy-MM-dd)
     * @return 해당 날짜의 일정 목록 (visit_time ASC, 각 일정에 알림 목록 포함)
     * @throws CommonException INVALID_INPUT — 날짜 형식이 잘못된 경우
     */
    public List<ScheduleResponse> getSchedulesByDate(Long userId, String date) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            log.warn("날짜 형식 오류 - date={}", date);
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }

        ZonedDateTime dayStart = localDate.atStartOfDay(ZoneId.systemDefault());
        long startTime = dayStart.toEpochSecond();
        long endTime   = dayStart.plusDays(1).toEpochSecond();

        log.debug("날짜별 일정 조회 - userId={}, date={}, startTime={}, endTime={}", userId, date, startTime, endTime);

        List<Schedule> schedules = scheduleMapper.findSchedulesByUserIdAndPeriod(userId, startTime, endTime);

        if (schedules.isEmpty()) {
            log.debug("해당 날짜 일정 없음 - userId={}, date={}", userId, date);
            return List.of();
        }

        List<Long> scheduleIds = schedules.stream()
                .map(Schedule::getScheduleId)
                .toList();

        Map<Long, List<ScheduleAlarmResponse>> alarmMap = scheduleMapper
                .findAlarmsByScheduleIds(scheduleIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ScheduleAlarm::getScheduleId,
                        Collectors.mapping(ScheduleAlarmResponse::from, Collectors.toList())
                ));

        List<ScheduleResponse> result = schedules.stream()
                .map(s -> ScheduleResponse.of(s, alarmMap.getOrDefault(s.getScheduleId(), List.of())))
                .toList();

        log.debug("날짜별 일정 조회 완료 - userId={}, date={}, count={}", userId, date, result.size());
        return result;
    }

    /**
     * 일정 생성
     *
     * <p>tb_schedule 에 일정을 저장한다. alarm_enabled 는 항상 true 로 저장.</p>
     * <p>알림 등록·제거는 별도 알림 API에서 처리.</p>
     *
     * <p>TODO: Auth 구현 후 userId 파라미터 제거, JWT에서 자동 추출</p>
     * <p>TODO: 운영 환경에서는 scheduleId 생성 전략을 Snowflake 등으로 교체</p>
     *
     * @param request 일정 생성 요청
     * @return 생성된 일정 ID
     * @throws CommonException INVALID_INPUT — 필수값 누락
     */
    @Transactional
    public ScheduleCreateResponse createSchedule(ScheduleCreateRequest request) {
        validateCreateRequest(request);

        // TODO: 운영 환경에서는 Snowflake 등 분산 ID 생성기로 교체
        long scheduleId = System.currentTimeMillis();

        log.debug("일정 생성 - userId={}, hospitalName={}, scheduleId={}",
                request.getUserId(), request.getHospitalName(), scheduleId);

        // alarmEnabled 는 항상 true 로 저장 (알림 등록/제거는 별도 API에서 처리)
        scheduleMapper.insertSchedule(
                scheduleId,
                request.getUserId(),
                request.getHospitalName(),
                request.getProcedureName(),
                request.getVisitTime(),
                true
        );

        log.info("일정 생성 완료 - userId={}, scheduleId={}", request.getUserId(), scheduleId);
        return ScheduleCreateResponse.builder().scheduleId(scheduleId).build();
    }

    /**
     * 일정 부분 수정 (PATCH)
     *
     * <p>hospitalName, procedureName, visitTime 중 최소 하나 이상 전달해야 한다.</p>
     * <p>null 인 필드는 기존값을 유지한다.</p>
     *
     * @param scheduleId 수정할 일정 ID
     * @param request    수정 요청 DTO
     * @return 수정 완료된 일정 상세 응답 (알림 목록 포함)
     * @throws CommonException SCHEDULE_NOT_FOUND — 존재하지 않는 scheduleId
     * @throws CommonException INVALID_INPUT — 수정 필드가 하나도 없거나 hospitalName 이 공백
     */
    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, ScheduleUpdateRequest request) {
        validateUpdateRequest(request);

        scheduleMapper.findScheduleById(scheduleId)
                .orElseThrow(() -> {
                    log.warn("수정 대상 일정 없음 - scheduleId={}", scheduleId);
                    return new CommonException(ErrorCode.SCHEDULE_NOT_FOUND);
                });

        log.debug("일정 수정 - scheduleId={}, hospitalName={}, procedureName={}, visitTime={}",
                scheduleId, request.getHospitalName(), request.getProcedureName(), request.getVisitTime());

        scheduleMapper.updateSchedule(
                scheduleId,
                request.getHospitalName(),
                request.getProcedureName(),
                request.getVisitTime()
        );

        log.info("일정 수정 완료 - scheduleId={}", scheduleId);
        return getScheduleById(scheduleId);
    }

    /**
     * 일정 수정 요청 유효성 검증
     *
     * @throws CommonException INVALID_INPUT — 수정 필드가 모두 null 이거나 hospitalName 이 공백
     */
    private void validateUpdateRequest(ScheduleUpdateRequest request) {
        if (request.getHospitalName() == null
                && request.getProcedureName() == null
                && request.getVisitTime() == null) {
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }
        if (request.getHospitalName() != null && request.getHospitalName().isBlank()) {
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 일정 삭제 (연관 알림 선삭제 후 일정 삭제)
     *
     * <p>tb_schedule_alarm 을 먼저 전량 삭제한 뒤 tb_schedule 을 삭제한다.</p>
     *
     * <p>TODO: FCM 알림 발송 구현 후 삭제 정책 재검토 필요.
     *    - is_sent=true 인 알림(이미 발송된 알림)이 있는 경우에도 단순 삭제할지,
     *      아니면 발송 이력을 별도 보존할지 결정 필요.
     *    - is_sent=false 인 미발송 알림은 삭제 전 FCM 취소 처리 여부도 검토 필요.</p>
     *
     * @param scheduleId 삭제할 일정 ID
     * @throws CommonException SCHEDULE_NOT_FOUND — 존재하지 않는 scheduleId
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        scheduleMapper.findScheduleById(scheduleId)
                .orElseThrow(() -> {
                    log.warn("삭제 대상 일정 없음 - scheduleId={}", scheduleId);
                    return new CommonException(ErrorCode.SCHEDULE_NOT_FOUND);
                });

        scheduleMapper.deleteAllAlarmsByScheduleId(scheduleId);
        scheduleMapper.deleteSchedule(scheduleId);

        log.info("일정 삭제 완료 - scheduleId={}", scheduleId);
    }

    /**
     * 일정 생성 요청 필수값 검증
     *
     * @throws CommonException INVALID_INPUT — 필수값 누락
     */
    private void validateCreateRequest(ScheduleCreateRequest request) {
        if (request.getUserId() == null) {
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }
        if (request.getHospitalName() == null || request.getHospitalName().isBlank()) {
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }
        if (request.getVisitTime() == null) {
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }
    }

}