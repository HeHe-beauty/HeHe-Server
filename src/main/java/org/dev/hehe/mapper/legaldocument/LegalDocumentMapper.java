package org.dev.hehe.mapper.legaldocument;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dev.hehe.domain.legaldocument.LegalDocument;

import java.util.Optional;

/**
 * 약관/정책 MyBatis 매퍼
 */
@Mapper
public interface LegalDocumentMapper {

    /**
     * 유형별 최신 버전 문서 조회
     *
     * <p>같은 type의 row 중 가장 최근에 등록된(id DESC) 한 건을 반환한다.</p>
     *
     * @param type 문서 유형 (PRIVACY_POLICY / TERMS_OF_SERVICE / ACCOUNT_DELETION_GUIDE)
     * @return 최신 버전 문서 (없으면 Optional.empty)
     */
    @Select("""
            SELECT id, type, version, content, created_at, updated_at
            FROM tb_legal_document
            WHERE type = #{type}
            ORDER BY id DESC
            LIMIT 1
            """)
    Optional<LegalDocument> findLatestByType(@Param("type") String type);
}
