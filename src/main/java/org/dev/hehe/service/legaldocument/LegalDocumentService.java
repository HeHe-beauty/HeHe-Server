package org.dev.hehe.service.legaldocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.legaldocument.LegalDocumentResponse;
import org.dev.hehe.mapper.legaldocument.LegalDocumentMapper;
import org.springframework.stereotype.Service;

/**
 * 약관/정책 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LegalDocumentService {

    private final LegalDocumentMapper legalDocumentMapper;

    /**
     * 유형별 최신 버전 문서 조회
     *
     * @param type 문서 유형 (PRIVACY_POLICY / TERMS_OF_SERVICE / ACCOUNT_DELETION_GUIDE)
     * @return 최신 버전 문서 응답 DTO
     * @throws CommonException L001 (해당 유형의 문서 없음)
     */
    public LegalDocumentResponse getLatestByType(String type) {
        String typeUpper = type.toUpperCase();
        log.debug("약관/정책 최신 버전 조회 - type={}", typeUpper);
        return legalDocumentMapper.findLatestByType(typeUpper)
                .map(LegalDocumentResponse::from)
                .orElseThrow(() -> {
                    log.warn("약관/정책을 찾을 수 없음 - type={}", typeUpper);
                    return new CommonException(ErrorCode.LEGAL_DOCUMENT_NOT_FOUND);
                });
    }
}
