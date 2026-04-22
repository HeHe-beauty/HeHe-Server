package org.dev.hehe.controller.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.schedule.ScheduleCreateRequest;
import org.dev.hehe.dto.schedule.ScheduleCreateResponse;
import org.dev.hehe.dto.schedule.ScheduleDailyRequest;
import org.dev.hehe.dto.schedule.ScheduleResponse;
import org.dev.hehe.dto.schedule.ScheduleUpdateRequest;
import org.dev.hehe.dto.schedule.ScheduleUpcomingRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Schedule API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 ScheduleController
 */
@Tag(name = "Schedule", description = "캘린더 일정 API")
public interface ScheduleApiSpecification {

    @Operation(
            summary = "전체 일정 요약 조회 (캘린더 점 표시용)",
            description = """
                    유저의 전체 일정을 날짜별 예약 건수 Map으로 반환합니다.
                    예약이 없는 날짜는 응답에 포함되지 않습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "2026-04-15": 2,
                                        "2026-04-20": 1,
                                        "2026-05-03": 3
                                      }
                                    }
                                    """))
            )
    })
    ApiResult<Map<String, Integer>> getScheduleSummary(@LoginUser Long userId);

    @Operation(
            summary = "일정 단건 조회",
            description = "scheduleId로 일정 상세와 연결된 알림 목록을 함께 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "scheduleId": 1741680000000,
                                        "hospitalName": "강남 제모 클리닉",
                                        "procedureName": "겨드랑이 레이저 제모",
                                        "visitTime": 1741680000,
                                        "alarmEnabled": true,
                                        "alarms": [
                                          { "alarmType": "1H", "alarmTime": 1741676400, "isSent": false },
                                          { "alarmType": "1D", "alarmTime": 1741593600, "isSent": false }
                                        ]
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "일정 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "S001",
                                      "message": "일정을 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<ScheduleResponse> getSchedule(
            @LoginUser Long userId,
            @Parameter(description = "조회할 일정 ID", required = true) @PathVariable Long scheduleId
    );

    @Operation(
            summary = "날짜별 일정 목록 조회",
            description = """
                    특정 날짜의 일정 목록을 반환합니다. (해당 날짜 00:00:00 ~ 23:59:59)
                    visit_time은 Unix timestamp(seconds)로 반환되며, 시간대 전환은 클라이언트에서 처리합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "scheduleId": 1001,
                                          "hospitalName": "강남 제모 클리닉",
                                          "procedureName": "겨드랑이 레이저 제모",
                                          "visitTime": 1744077600,
                                          "alarmEnabled": true,
                                          "alarms": [
                                            { "alarmType": "1H", "alarmTime": 1744074000, "isSent": false }
                                          ]
                                        }
                                      ]
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "필수 파라미터 누락 또는 날짜 형식 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "errorCode": "C002", "message": "잘못된 요청 값입니다." }
                                    """))
            )
    })
    ApiResult<List<ScheduleResponse>> getSchedulesByDate(@LoginUser Long userId,
                                                            @ParameterObject @Valid ScheduleDailyRequest request);

    @Operation(
            summary = "예정 일정 N건 조회",
            description = """
                    현재 시각 이후의 예정 일정을 visit_time 오름차순(가까운 순)으로 N건 반환합니다.
                    visit_time은 Unix timestamp(seconds)로 반환되며, 시간대 전환은 클라이언트에서 처리합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "scheduleId": 1001,
                                          "hospitalName": "강남 제모 클리닉",
                                          "procedureName": "겨드랑이 레이저 제모",
                                          "visitTime": 1741680000,
                                          "alarmEnabled": true,
                                          "alarms": [
                                            { "alarmType": "1H", "alarmTime": 1741676400, "isSent": false }
                                          ]
                                        },
                                        {
                                          "scheduleId": 1002,
                                          "hospitalName": "홍대 스킨케어",
                                          "visitTime": 1741766400,
                                          "alarmEnabled": false,
                                          "alarms": []
                                        }
                                      ]
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "필수 파라미터 누락 또는 limit < 1",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "C002",
                                      "message": "잘못된 요청 값입니다."
                                    }
                                    """))
            )
    })
    ApiResult<List<ScheduleResponse>> getUpcomingSchedules(
            @LoginUser Long userId,
            @ParameterObject @Valid ScheduleUpcomingRequest request
    );

    @Operation(
            summary = "일정 생성",
            description = "방문 일정을 생성합니다. alarm_enabled 는 항상 true 로 저장됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "일정 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "scheduleId": 1741680000000
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "필수값 누락 또는 잘못된 alarmType",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "C002",
                                      "message": "잘못된 요청 값입니다."
                                    }
                                    """))
            )
    })
    ApiResult<ScheduleCreateResponse> createSchedule(@LoginUser Long userId,
                                                        @RequestBody ScheduleCreateRequest request);

    @Operation(
            summary = "일정 수정",
            description = """
                    방문 일정의 병원명·시술명·방문시각을 부분 수정합니다. (PATCH)

                    - 전달된 필드만 수정하며, null 인 필드는 기존값을 유지합니다.
                    - hospitalName, procedureName, visitTime 중 최소 하나 이상 전달해야 합니다.
                    - 수정 완료 후 최신 일정 상세(알림 목록 포함)를 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "scheduleId": 1741680000000,
                                        "hospitalName": "수정된 클리닉",
                                        "procedureName": "전신 레이저 제모",
                                        "visitTime": 1741766400,
                                        "alarmEnabled": true,
                                        "alarms": [
                                          { "alarmType": "1H", "alarmTime": 1741762800, "isSent": false }
                                        ]
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "수정 필드 없음 또는 hospitalName 공백",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "C002",
                                      "message": "잘못된 요청 값입니다."
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "일정 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "S001",
                                      "message": "일정을 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<ScheduleResponse> updateSchedule(
            @LoginUser Long userId,
            @Parameter(description = "수정할 일정 ID", required = true) @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateRequest request
    );

    @Operation(
            summary = "일정 삭제",
            description = """
                    방문 일정을 삭제합니다. 연관된 알림(tb_schedule_alarm)도 함께 삭제됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "일정 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "S001",
                                      "message": "일정을 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<Void> deleteSchedule(
            @LoginUser Long userId,
            @Parameter(description = "삭제할 일정 ID", required = true) @PathVariable Long scheduleId
    );
}