package org.dev.hehe.controller.recentview;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.recentview.RecentViewResponse;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 최근 본 병원 API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 RecentViewController
 */
@Tag(name = "RecentView", description = "최근 본 병원 API")
public interface RecentViewApiSpecification {

    @Operation(
            summary = "최근 본 병원 목록 조회",
            description = "로그인한 유저의 최근 본 병원을 최신 조회 순으로 최대 10건 반환합니다.",
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
                                          "hospitalId": 101,
                                          "name": "강남 제모 클리닉",
                                          "address": "서울 강남구 테헤란로 123",
                                          "tags": ["젠틀맥스프로", "당일예약"],
                                          "viewedAt": "2026-04-22T10:30:00"
                                        }
                                      ]
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "AU003",
                                      "message": "인증 정보가 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<List<RecentViewResponse>> getRecentViews(@LoginUser Long userId);

    @Operation(
            summary = "최근 본 병원 기록",
            description = "병원 상세 진입 시 호출합니다. 이미 기록된 병원이면 조회 시각만 갱신됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "기록 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": null
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "AU003",
                                      "message": "인증 정보가 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<Void> recordRecentView(@PathVariable Long hospitalId, @LoginUser Long userId);
}