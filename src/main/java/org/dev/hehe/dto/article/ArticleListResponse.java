package org.dev.hehe.dto.article;

import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.article.Article;

/**
 * 아티클 목록 조회용 응답 DTO
 * - content(본문 HTML)는 용량이 크므로 목록 조회 시 제외
 * - 단건 상세 조회 시에는 ArticleResponse 사용
 */
@Getter
@Builder
public class ArticleListResponse {

    private Long articleId;
    private String title;
    private String subTitle;
    private String thumbnailUrl;

    /**
     * Article 도메인 객체를 목록용 응답 DTO로 변환
     *
     * @param article 도메인 객체
     * @return ArticleListResponse
     */
    public static ArticleListResponse from(Article article) {
        return ArticleListResponse.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .subTitle(article.getSubTitle())
                .thumbnailUrl(article.getThumbnailUrl())
                .build();
    }
}