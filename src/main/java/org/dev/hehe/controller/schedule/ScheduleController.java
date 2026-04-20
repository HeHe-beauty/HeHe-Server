package org.dev.hehe.controller.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.dev.hehe.common.response.ApiResponse;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.schedule.ScheduleCreateRequest;
import org.dev.hehe.dto.schedule.ScheduleCreateResponse;
import org.dev.hehe.dto.schedule.ScheduleDailyRequest;
import org.dev.hehe.dto.schedule.ScheduleResponse;
import org.dev.hehe.dto.schedule.ScheduleUpdateRequest;
import org.dev.hehe.dto.schedule.ScheduleUpcomingRequest;
import org.dev.hehe.service.schedule.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 캘린더 일정 컨트롤러
 * Swagger 명세는 ScheduleApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController implements ScheduleApiSpecification {

    private final ScheduleService scheduleService;

    /** 전체 일정 요약 조회 (날짜별 예약 건수, 캘린더 점 표시용) */
    @Override
    @GetMapping("/summary")
    public ApiResponse<Map<String, Integer>> getScheduleSummary(@LoginUser Long userId) {
        log.info("[GET] /api/v1/schedules/summary - 전체 일정 요약 조회 요청 - userId={}", userId);
        Map<String, Integer> summary = scheduleService.getScheduleSummary(userId);
        log.info("전체 일정 요약 조회 완료 - userId={}, dateCount={}", userId, summary.size());
        return ApiResponse.ok(summary);
    }

    /** 일정 단건 조회 (알림 목록 포함) */
    @Override
    @GetMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> getSchedule(@LoginUser Long userId,
                                                      @PathVariable Long scheduleId) {
        log.info("[GET] /api/v1/schedules/{} - 일정 단건 조회 요청 - userId={}", scheduleId, userId);
        ScheduleResponse response = scheduleService.getScheduleById(scheduleId);
        log.info("일정 단건 조회 완료 - scheduleId={}", scheduleId);
        return ApiResponse.ok(response);
    }

    /** 날짜별 일정 목록 조회 (JWT에서 userId 자동 추출) */
    @Override
    @GetMapping("/daily")
    public ApiResponse<List<ScheduleResponse>> getSchedulesByDate(@LoginUser Long userId,
                                                                   @Valid ScheduleDailyRequest request) {
        log.info("[GET] /api/v1/schedules/daily - userId={}, date={}", userId, request.getDate());
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDate(userId, request.getDate());
        log.info("날짜별 일정 조회 완료 - userId={}, date={}, count={}", userId, request.getDate(), schedules.size());
        return ApiResponse.ok(schedules);
    }

    /** 예정 일정 N건 조회 (현재 시각 이후, visit_time ASC, JWT에서 userId 자동 추출) */
    @Override
    @GetMapping("/upcoming")
    public ApiResponse<List<ScheduleResponse>> getUpcomingSchedules(@LoginUser Long userId,
                                                                     @Valid ScheduleUpcomingRequest request) {
        log.info("[GET] /api/v1/schedules/upcoming - userId={}, limit={}", userId, request.getLimit());
        List<ScheduleResponse> schedules = scheduleService.getUpcomingSchedules(userId, request.getLimit());
        log.info("예정 일정 조회 완료 - userId={}, limit={}, count={}", userId, request.getLimit(), schedules.size());
        return ApiResponse.ok(schedules);
    }

    /** 일정 생성 (JWT에서 userId 자동 추출) */
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScheduleCreateResponse> createSchedule(@LoginUser Long userId,
                                                               @RequestBody ScheduleCreateRequest request) {
        log.info("[POST] /api/v1/schedules - userId={}, hospitalName={}", userId, request.getHospitalName());
        ScheduleCreateResponse response = scheduleService.createSchedule(userId, request);
        log.info("일정 생성 완료 - userId={}, scheduleId={}", userId, response.getScheduleId());
        return ApiResponse.ok(response);
    }

    /** 일정 삭제 (연관 알림 포함) */
    @Override
    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> deleteSchedule(@LoginUser Long userId,
                                             @PathVariable Long scheduleId) {
        log.info("[DELETE] /api/v1/schedules/{} - 일정 삭제 요청 - userId={}", scheduleId, userId);
        scheduleService.deleteSchedule(scheduleId);
        log.info("일정 삭제 완료 - scheduleId={}", scheduleId);
        return ApiResponse.ok(null);
    }

    /** 일정 부분 수정 (병원명·시술명·방문시각) */
    @Override
    @PatchMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> updateSchedule(@LoginUser Long userId,
                                                         @PathVariable Long scheduleId,
                                                         @RequestBody ScheduleUpdateRequest request) {
        log.info("[PATCH] /api/v1/schedules/{} - userId={}, hospitalName={}, visitTime={}",
                scheduleId, userId, request.getHospitalName(), request.getVisitTime());
        ScheduleResponse response = scheduleService.updateSchedule(scheduleId, request);
        log.info("일정 수정 완료 - scheduleId={}", scheduleId);
        return ApiResponse.ok(response);
    }
}