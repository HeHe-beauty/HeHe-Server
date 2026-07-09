package org.dev.hehe.service.legaldocument;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.legaldocument.LegalDocument;
import org.dev.hehe.dto.legaldocument.LegalDocumentResponse;
import org.dev.hehe.mapper.legaldocument.LegalDocumentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * LegalDocumentService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LegalDocumentService 테스트")
class LegalDocumentServiceTest {

    @Mock
    private LegalDocumentMapper legalDocumentMapper;

    @InjectMocks
    private LegalDocumentService legalDocumentService;

    @Test
    @DisplayName("최신 버전 조회 성공 - 소문자 type도 대문자로 변환해서 조회")
    void getLatestByType_success() {
        // given
        LegalDocument document = new LegalDocument();
        ReflectionTestUtils.setField(document, "type", "TERMS_OF_SERVICE");
        ReflectionTestUtils.setField(document, "version", "v1.0.0");
        ReflectionTestUtils.setField(document, "content", "## 서비스 이용약관\n본문");

        given(legalDocumentMapper.findLatestByType("TERMS_OF_SERVICE")).willReturn(Optional.of(document));

        // when
        LegalDocumentResponse result = legalDocumentService.getLatestByType("terms_of_service");

        // then
        assertThat(result.getType()).isEqualTo("TERMS_OF_SERVICE");
        assertThat(result.getVersion()).isEqualTo("v1.0.0");
        assertThat(result.getContent()).isEqualTo("## 서비스 이용약관\n본문");
        verify(legalDocumentMapper).findLatestByType("TERMS_OF_SERVICE");
    }

    @Test
    @DisplayName("존재하지 않는 type 조회 시 L001 예외")
    void getLatestByType_notFound() {
        // given
        given(legalDocumentMapper.findLatestByType("UNKNOWN")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> legalDocumentService.getLatestByType("unknown"))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LEGAL_DOCUMENT_NOT_FOUND));
    }
}
