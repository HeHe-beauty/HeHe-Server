package org.dev.hehe.controller.bookmark;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.bookmark.BookmarkResponse;

import java.util.List;

/**
 * Bookmark API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 BookmarkController
 */
@Tag(name = "Bookmark", description = "찜(북마크) API")
public interface BookmarkApiSpecification {

    @Operation(
            summary = "찜한 병원 목록 조회",
            description = "로그인한 유저의 찜한 병원 목록을 최신 찜 순으로 반환합니다.",
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
                                          "address": "서울 강남구 역삼동 123-4",
                                          "tags": ["여성원장", "주차가능"],
                                          "bookmarkedAt": "2026-04-22T10:30:00"
                                        }
                                      ]
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 없음 또는 만료)",
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
    ApiResult<List<BookmarkResponse>> getBookmarks(@LoginUser Long userId);

    @Operation(
            summary = "병원 찜 추가",
            description = "hospitalId에 해당하는 병원을 찜합니다. 이미 찜한 병원이면 409를 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "찜 추가 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 찜한 병원",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "B001",
                                      "message": "이미 찜한 병원입니다."
                                    }
                                    """))
            )
    })
    ApiResult<Void> addBookmark(@LoginUser Long userId,
                                  @io.swagger.v3.oas.annotations.Parameter(description = "병원 ID") Long hospitalId);

    @Operation(
            summary = "병원 찜 삭제",
            description = "hospitalId에 해당하는 병원의 찜을 해제합니다. 찜하지 않은 병원이면 404를 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "찜 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "찜하지 않은 병원",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "B002",
                                      "message": "찜한 병원을 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<Void> removeBookmark(@LoginUser Long userId,
                                     @io.swagger.v3.oas.annotations.Parameter(description = "병원 ID") Long hospitalId);
}