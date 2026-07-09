package org.dev.hehe.domain.legaldocument;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * tb_legal_document 테이블 매핑 도메인 객체
 * (개인정보처리방침 / 서비스이용약관 / 계정삭제안내)
 */
@Getter
public class LegalDocument {

    private Long id;
    private String type;
    private String version;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
