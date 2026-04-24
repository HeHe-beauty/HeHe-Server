package org.dev.hehe.controller.contact;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.contact.ContactHistoryResponse;
import org.dev.hehe.dto.contact.ContactSaveRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 문의 내역 API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 ContactController
 */
@Tag(name = "Contact", description = "문의 내역 API")
public interface ContactApiSpecification {

    @Operation(
            summary = "문의 내역 목록 조회",
            description = "로그인한 유저의 문의 내역을 최신 문의 순으로 반환합니다. 삭제된 항목은 포함되지 않습니다. address, tags, isBookmarked 필드가 포함됩니다.",
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
                                          "id": 1,
                                          "hospitalId": 101,
                                          "hospitalName": "강남 제모 클리닉",
                                          "address": "서울 강남구 역삼동 123-4",
                                          "contactType": "CALL",
                                          "tags": ["여성원장", "주차가능"],
                                          "isBookmarked": true,
                                          "createdAt": "2026-04-22T10:30:00"
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
    ApiResult<List<ContactHistoryResponse>> getContactHistories(@LoginUser Long userId);

    @Operation(
            summary = "문의 내역 저장",
            description = "로그인한 유저의 병원 문의 내역을 저장합니다. 문의 유형은 CALL, CHAT, VISIT 중 하나여야 합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "저장 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": null
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 값 누락 또는 유효하지 않은 문의 유형)",
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
    ApiResult<Void> saveContact(@LoginUser Long userId, @RequestBody ContactSaveRequest request);

    @Operation(
            summary = "문의 내역 삭제",
            description = "로그인한 유저의 문의 내역을 소프트 삭제합니다. 본인 문의 내역이 아니거나 이미 삭제된 경우 CO001을 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": null
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "문의 내역 없음 또는 다른 유저의 내역",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "CO001",
                                      "message": "문의 내역을 찾을 수 없습니다."
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
    ApiResult<Void> deleteContact(@PathVariable Long contactId, @LoginUser Long userId);
}