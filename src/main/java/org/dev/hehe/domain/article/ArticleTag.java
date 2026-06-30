package org.dev.hehe.domain.article;

import lombok.Getter;

/**
 * tb_article_tag MyBatis 매핑 도메인
 */
@Getter
public class ArticleTag {

    private Long articleId;
    private String tagName;
}
