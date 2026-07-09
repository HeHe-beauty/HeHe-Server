package org.dev.hehe.controller.legaldocument;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.legaldocument.LegalDocumentResponse;
import org.dev.hehe.service.legaldocument.LegalDocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LegalDocumentController 단위 테스트
 * - @WebMvcTest: Controller 레이어만 로드 (Service는 Mock 처리)
 * - 인증 불필요 API라 JWT 토큰 없이 호출
 */
@WebMvcTest(LegalDocumentController.class)
@Import(SecurityConfig.class)
@DisplayName("LegalDocumentController 테스트")
class LegalDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private LegalDocumentService legalDocumentService;

    @Test
    @DisplayName("GET /api/v1/legal-documents/{type} - 인증 토큰 없이 조회 성공")
    void getLatest_success() throws Exception {
        // given
        LegalDocumentResponse response = LegalDocumentResponse.builder()
                .type("TERMS_OF_SERVICE")
                .version("v1.0.0")
                .content("## 서비스 이용약관\n본문")
                .build();

        given(legalDocumentService.getLatestByType("TERMS_OF_SERVICE")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/legal-documents/TERMS_OF_SERVICE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("TERMS_OF_SERVICE"))
                .andExpect(jsonPath("$.data.version").value("v1.0.0"));
    }

    @Test
    @DisplayName("GET /api/v1/legal-documents/{type} - 존재하지 않는 type이면 404")
    void getLatest_notFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.LEGAL_DOCUMENT_NOT_FOUND))
                .given(legalDocumentService).getLatestByType("UNKNOWN");

        // when & then
        mockMvc.perform(get("/api/v1/legal-documents/UNKNOWN"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("L001"));
    }
}
