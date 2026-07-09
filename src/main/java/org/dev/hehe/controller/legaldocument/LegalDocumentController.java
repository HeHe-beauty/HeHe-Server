package org.dev.hehe.controller.legaldocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.legaldocument.LegalDocumentResponse;
import org.dev.hehe.service.legaldocument.LegalDocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 약관/정책 컨트롤러
 * Swagger 명세는 LegalDocumentApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentController implements LegalDocumentApiSpecification {

    private final LegalDocumentService legalDocumentService;

    @Override
    @GetMapping("/{type}")
    public ApiResult<LegalDocumentResponse> getLatest(@PathVariable String type) {
        log.info("[GET] /api/v1/legal-documents/{} - 최신 버전 조회 요청", type);
        return ApiResult.ok(legalDocumentService.getLatestByType(type));
    }
}
