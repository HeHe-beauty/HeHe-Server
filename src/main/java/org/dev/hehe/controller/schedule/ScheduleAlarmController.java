package org.dev.hehe.controller.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.schedule.ScheduleAlarmRequest;
import org.dev.hehe.dto.schedule.ScheduleAlarmResponse;
import org.dev.hehe.service.schedule.ScheduleAlarmService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 캘린더 일정 알림 등록/삭제 컨트롤러
 * Swagger 명세는 ScheduleAlarmApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedules/{scheduleId}/alarms")
@RequiredArgsConstructor
public class ScheduleAlarmController implements ScheduleAlarmApiSpecification {

    private final ScheduleAlarmService scheduleAlarmService;

    /** 알림 등록 */
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<ScheduleAlarmResponse> addAlarm(@LoginUser Long userId,
                                                        @PathVariable Long scheduleId,
                                                        @RequestBody ScheduleAlarmRequest request) {
        log.info("[POST] /api/v1/schedules/{}/alarms - userId={}, alarmType={}", scheduleId, userId, request.getAlarmType());
        ScheduleAlarmResponse response = scheduleAlarmService.addAlarm(scheduleId, request.getAlarmType());
        log.info("알림 등록 완료 - scheduleId={}, alarmType={}", scheduleId, request.getAlarmType());
        return ApiResult.ok(response);
    }

    /** 알림 삭제 */
    @Override
    @DeleteMapping("/{alarmType}")
    public ApiResult<Void> removeAlarm(@LoginUser Long userId,
                                          @PathVariable Long scheduleId,
                                          @PathVariable String alarmType) {
        log.info("[DELETE] /api/v1/schedules/{}/alarms/{} - userId={}, 알림 삭제 요청", scheduleId, alarmType, userId);
        scheduleAlarmService.removeAlarm(scheduleId, alarmType);
        log.info("알림 삭제 완료 - scheduleId={}, alarmType={}", scheduleId, alarmType);
        return ApiResult.ok(null);
    }
}