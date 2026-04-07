package org.dev.hehe.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.schedule.Schedule;
import org.dev.hehe.dto.schedule.ScheduleAlarmResponse;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 캘린더 일정 알림 등록/삭제 서비스
 *
 * <p>지원 alarmType: 1H(1시간 전), 1D(1일 전), 3D(3일 전)</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleAlarmService {

    private static final Set<String> SUPPORTED_ALARM_TYPES = Set.of("1H", "1D", "3D");

    private final ScheduleMapper scheduleMapper;

    /**
     * 알림 등록
     *
     * <p>처리 순서:</p>
     * <ol>
     *   <li>alarmType 유효성 검증</li>
     *   <li>scheduleId로 일정 조회 (없으면 SCHEDULE_NOT_FOUND)</li>
     *   <li>alarmTime 계산 후 INSERT — 이미 존재하면 ALARM_ALREADY_EXISTS</li>
     * </ol>
     *
     * @param scheduleId 대상 일정 ID
     * @param alarmType  등록할 알림 유형 (1H, 1D, 3D)
     * @return 등록된 알림 응답 DTO
     * @throws CommonException INVALID_INPUT       — 지원하지 않는 alarmType
     * @throws CommonException SCHEDULE_NOT_FOUND  — 존재하지 않는 scheduleId
     * @throws CommonException ALARM_ALREADY_EXISTS — 이미 등록된 alarmType
     */
    @Transactional
    public ScheduleAlarmResponse addAlarm(Long scheduleId, String alarmType) {
        validateAlarmType(alarmType);
        Schedule schedule = findScheduleOrThrow(scheduleId);

        long alarmTime = resolveAlarmTime(alarmType, schedule.getVisitTime());

        log.debug("알림 등록 시도 - scheduleId={}, alarmType={}", scheduleId, alarmType);
        int affected = scheduleMapper.insertScheduleAlarmIfNotExists(scheduleId, alarmType, alarmTime);
        if (affected == 0) {
            log.warn("알림 중복 등록 시도 - scheduleId={}, alarmType={}", scheduleId, alarmType);
            throw new CommonException(ErrorCode.ALARM_ALREADY_EXISTS);
        }

        log.info("알림 등록 완료 - scheduleId={}, alarmType={}", scheduleId, alarmType);
        return ScheduleAlarmResponse.builder()
                .alarmType(alarmType)
                .alarmTime(alarmTime)
                .isSent(false)
                .build();
    }

    /**
     * 알림 삭제
     *
     * <p>처리 순서:</p>
     * <ol>
     *   <li>alarmType 유효성 검증</li>
     *   <li>scheduleId로 일정 존재 확인 (없으면 SCHEDULE_NOT_FOUND)</li>
     *   <li>DELETE — 존재하지 않으면 ALARM_NOT_FOUND</li>
     * </ol>
     *
     * @param scheduleId 대상 일정 ID
     * @param alarmType  삭제할 알림 유형 (1H, 1D, 3D)
     * @throws CommonException INVALID_INPUT      — 지원하지 않는 alarmType
     * @throws CommonException SCHEDULE_NOT_FOUND — 존재하지 않는 scheduleId
     * @throws CommonException ALARM_NOT_FOUND    — 등록되지 않은 alarmType
     */
    @Transactional
    public void removeAlarm(Long scheduleId, String alarmType) {
        validateAlarmType(alarmType);
        findScheduleOrThrow(scheduleId);

        log.debug("알림 삭제 시도 - scheduleId={}, alarmType={}", scheduleId, alarmType);
        int affected = scheduleMapper.deleteScheduleAlarm(scheduleId, alarmType);
        if (affected == 0) {
            log.warn("존재하지 않는 알림 삭제 시도 - scheduleId={}, alarmType={}", scheduleId, alarmType);
            throw new CommonException(ErrorCode.ALARM_NOT_FOUND);
        }

        log.info("알림 삭제 완료 - scheduleId={}, alarmType={}", scheduleId, alarmType);
    }

    /**
     * alarmType 유효성 검증 — 지원하지 않는 유형이면 INVALID_INPUT 예외
     *
     * @param alarmType 검증할 알림 유형
     * @throws CommonException INVALID_INPUT
     */
    private void validateAlarmType(String alarmType) {
        if (alarmType == null || !SUPPORTED_ALARM_TYPES.contains(alarmType)) {
            log.warn("지원하지 않는 alarmType - alarmType={}", alarmType);
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * scheduleId로 일정 조회 — 존재하지 않으면 SCHEDULE_NOT_FOUND 예외
     *
     * @param scheduleId 조회할 일정 ID
     * @return 일정 도메인 객체
     * @throws CommonException SCHEDULE_NOT_FOUND
     */
    private Schedule findScheduleOrThrow(Long scheduleId) {
        return scheduleMapper.findScheduleById(scheduleId)
                .orElseThrow(() -> {
                    log.warn("일정을 찾을 수 없음 - scheduleId={}", scheduleId);
                    return new CommonException(ErrorCode.SCHEDULE_NOT_FOUND);
                });
    }

    /**
     * alarmType과 visitTime으로 알림 발송 시각 계산
     *
     * <ul>
     *   <li>1H: visitTime - 1시간 (3,600초)</li>
     *   <li>1D: visitTime - 1일 (86,400초)</li>
     *   <li>3D: visitTime - 3일 (259,200초)</li>
     * </ul>
     *
     * @param alarmType 알림 유형
     * @param visitTime 방문 예정 시각 (Unix timestamp)
     * @return 알림 발송 예정 시각 (Unix timestamp)
     */
    long resolveAlarmTime(String alarmType, long visitTime) {
        return switch (alarmType) {
            case "1H" -> visitTime - 3_600;
            case "1D" -> visitTime - 86_400;
            case "3D" -> visitTime - 259_200;
            default   -> throw new CommonException(ErrorCode.INVALID_INPUT); // validateAlarmType에서 이미 방어
        };
    }
}