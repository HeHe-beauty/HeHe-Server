package org.dev.hehe.controller.legaldocument;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.legaldocument.LegalDocumentResponse;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 약관/정책 API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 LegalDocumentController
 */
@Tag(name = "09. LegalDocument", description = "개인정보처리방침 / 서비스이용약관 / 계정삭제안내")
public interface LegalDocumentApiSpecification {

    @Operation(
            summary = "약관/정책 최신 버전 조회",
            description = "type별 가장 최근에 등록된 버전의 문서를 반환합니다. 로그인 불필요(회원가입 동의 화면에서도 호출). " +
                    "type: PRIVACY_POLICY(개인정보처리방침) / TERMS_OF_SERVICE(서비스이용약관) / ACCOUNT_DELETION_GUIDE(계정삭제안내)"
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
                                        "type": "TERMS_OF_SERVICE",
                                        "version": "v1.0.0",
                                        "content": "## 서비스 이용약관\\n본문 내용...",
                                        "createdAt": "2026-07-09 12:00:00"
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유형의 문서 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "L001",
                                      "message": "해당 유형의 약관/정책을 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<LegalDocumentResponse> getLatest(
            @Parameter(description = "문서 유형", example = "TERMS_OF_SERVICE") @PathVariable String type);
}
