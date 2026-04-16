package org.dev.hehe.controller.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResponse;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.schedule.ScheduleAlarmRequest;
import org.dev.hehe.dto.schedule.ScheduleAlarmResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Schedule 알림 API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 ScheduleAlarmController
 */
@Tag(name = "Schedule", description = "캘린더 일정 API")
public interface ScheduleAlarmApiSpecification {

    @Operation(
            summary = "알림 등록",
            description = """
                    일정에 알림을 등록합니다.
                    지원 alarmType: 1H(1시간 전), 1D(1일 전), 3D(3일 전)
                    이미 등록된 alarmType이면 409를 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "알림 등록 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "alarmType": "1H",
                                        "alarmTime": 1741676400,
                                        "isSent": false
                                      }
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 등록된 알림 유형",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "S002",
                                      "message": "이미 등록된 알림 유형입니다."
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "지원하지 않는 alarmType",
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
    ApiResponse<ScheduleAlarmResponse> addAlarm(
            @LoginUser Long userId,
            @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId,
            @RequestBody ScheduleAlarmRequest request
    );

    @Operation(
            summary = "알림 삭제",
            description = """
                    일정에서 알림을 삭제합니다.
                    지원 alarmType: 1H, 1D, 3D
                    등록되지 않은 alarmType이면 404를 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "알림 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "일정 없음 또는 알림 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "S003",
                                      "message": "등록된 알림을 찾을 수 없습니다."
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "지원하지 않는 alarmType",
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
    ApiResponse<Void> removeAlarm(
            @LoginUser Long userId,
            @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId,
            @Parameter(description = "삭제할 알림 유형 (1H, 1D, 3D)", required = true) @PathVariable String alarmType
    );
}