package org.dev.hehe.dto.legaldocument;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.legaldocument.LegalDocument;

import java.time.LocalDateTime;

/**
 * 약관/정책 최신 버전 응답 DTO
 */
@Getter
@Builder
@Schema(description = "약관/정책 최신 버전 응답")
public class LegalDocumentResponse {

    @Schema(description = "문서 유형", example = "TERMS_OF_SERVICE")
    private String type;

    @Schema(description = "문서 버전", example = "v1.0.0")
    private String version;

    @Schema(description = "문서 본문 (MD/HTML)")
    private String content;

    @Schema(description = "등록 일시", example = "2026-07-09 12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static LegalDocumentResponse from(LegalDocument document) {
        return LegalDocumentResponse.builder()
                .type(document.getType())
                .version(document.getVersion())
                .content(document.getContent())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
