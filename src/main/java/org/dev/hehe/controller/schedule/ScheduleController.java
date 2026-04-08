package org.dev.hehe.controller.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.dev.hehe.common.response.ApiResponse;
import org.dev.hehe.dto.schedule.ScheduleCreateRequest;
import org.dev.hehe.dto.schedule.ScheduleCreateResponse;
import org.dev.hehe.dto.schedule.ScheduleDailyRequest;
import org.dev.hehe.dto.schedule.ScheduleResponse;
import org.dev.hehe.dto.schedule.ScheduleUpdateRequest;
import org.dev.hehe.service.schedule.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /** 일정 단건 조회 (알림 목록 포함) */
    @Override
    @GetMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> getSchedule(@PathVariable Long scheduleId) {
        log.info("[GET] /api/v1/schedules/{} - 일정 단건 조회 요청", scheduleId);
        ScheduleResponse response = scheduleService.getScheduleById(scheduleId);
        log.info("일정 단건 조회 완료 - scheduleId={}", scheduleId);
        return ApiResponse.ok(response);
    }

    /**
     * 날짜별 일정 목록 조회
     * TODO: Auth 구현 후 request.getUserId() 제거, JWT SecurityContext에서 userId 추출로 변경
     */
    @Override
    @GetMapping("/daily")
    public ApiResponse<List<ScheduleResponse>> getSchedulesByDate(@Valid ScheduleDailyRequest request) {
        log.info("[GET] /api/v1/schedules/daily - userId={}, date={}", request.getUserId(), request.getDate());
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDate(request.getUserId(), request.getDate());
        log.info("날짜별 일정 조회 완료 - userId={}, date={}, count={}", request.getUserId(), request.getDate(), schedules.size());
        return ApiResponse.ok(schedules);
    }

    /**
     * 홈 화면 7일 일정 조회
     * TODO: 사용하지 않는 API. 삭제 예정
     */
    @Deprecated
    @Override
    @GetMapping("/upcoming")
    public ApiResponse<List<ScheduleResponse>> getUpcomingSchedules(@RequestParam Long userId) {
        log.info("[GET] /api/v1/schedules/upcoming - userId={}", userId);
        List<ScheduleResponse> schedules = scheduleService.getUpcomingSchedules(userId);
        log.info("7일 일정 조회 완료 - userId={}, count={}", userId, schedules.size());
        return ApiResponse.ok(schedules);
    }

    /**
     * 일정 생성
     * TODO: Auth 구현 후 request.getUserId() 제거, JWT SecurityContext에서 userId 추출로 변경
     */
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScheduleCreateResponse> createSchedule(@RequestBody ScheduleCreateRequest request) {
        log.info("[POST] /api/v1/schedules - userId={}, hospitalName={}", request.getUserId(), request.getHospitalName());
        ScheduleCreateResponse response = scheduleService.createSchedule(request);
        log.info("일정 생성 완료 - userId={}, scheduleId={}", request.getUserId(), response.getScheduleId());
        return ApiResponse.ok(response);
    }

    /** 일정 삭제 (연관 알림 포함) */
    @Override
    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> deleteSchedule(@PathVariable Long scheduleId) {
        log.info("[DELETE] /api/v1/schedules/{} - 일정 삭제 요청", scheduleId);
        scheduleService.deleteSchedule(scheduleId);
        log.info("일정 삭제 완료 - scheduleId={}", scheduleId);
        return ApiResponse.ok(null);
    }

    /** 일정 부분 수정 (병원명·시술명·방문시각) */
    @Override
    @PatchMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> updateSchedule(@PathVariable Long scheduleId,
                                                        @RequestBody ScheduleUpdateRequest request) {
        log.info("[PATCH] /api/v1/schedules/{} - hospitalName={}, visitTime={}",
                scheduleId, request.getHospitalName(), request.getVisitTime());
        ScheduleResponse response = scheduleService.updateSchedule(scheduleId, request);
        log.info("일정 수정 완료 - scheduleId={}", scheduleId);
        return ApiResponse.ok(response);
    }
}