package org.dev.hehe.mapper.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.dev.hehe.domain.article.Article;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ArticleMapper {

    /**
     * 활성 아티클 목록 조회 (content 제외 — 목록에서는 불필요한 대용량 컬럼 생략)
     */
    @Select("""
            SELECT id, article_id, title, sub_title, thumbnail_url,
                   priority, is_active, view_count, created_at, updated_at
            FROM tb_article
            WHERE is_active = true
            ORDER BY priority DESC
            """)
    List<Article> findAllArticle();

    /**
     * article_id로 활성 아티클 단건 조회
     */
    @Select("""
            SELECT id, article_id, title, sub_title, thumbnail_url, content,
                   priority, is_active, view_count, created_at, updated_at
            FROM tb_article
            WHERE article_id = #{articleId}
              AND is_active = true
            """)
    Optional<Article> findByArticleId(Long articleId);
}