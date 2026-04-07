package org.dev.hehe.dto.article;

import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.article.Article;

@Getter
@Builder
public class ArticleResponse {

    private Long articleId;
    private String title;
    private String subTitle;
    private String thumbnailUrl;
    private String content;

    public static ArticleResponse from(Article article) {
        return ArticleResponse.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .subTitle(article.getSubTitle())
                .thumbnailUrl(article.getThumbnailUrl())
                .content(article.getContent())
                .build();
    }
}