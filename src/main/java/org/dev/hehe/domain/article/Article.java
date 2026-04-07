package org.dev.hehe.domain.article;

import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class Article {

    private Long id;
    private Long articleId;
    private String title;
    private String subTitle;
    private String thumbnailUrl;
    private String content;
    private Integer priority;
    private Boolean isActive;
    private Integer viewCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}