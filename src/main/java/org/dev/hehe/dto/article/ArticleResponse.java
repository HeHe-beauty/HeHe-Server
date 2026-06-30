package org.dev.hehe.dto.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.dev.hehe.domain.article.Article;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Builder
@Schema(description = "아티클 단건 응답")
public class ArticleResponse {

    @Schema(description = "아티클 ID", example = "1")
    private Long articleId;

    @Schema(description = "아티클 제목", example = "레이저 제모 완벽 가이드")
    private String title;

    @Schema(description = "리스트 요약 문구", example = "시작 전 알아야 할 것들")
    private String subTitle;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumb.jpg")
    private String thumbnailUrl;

    @Schema(description = "아티클 본문 (Markdown 형식)")
    private String content;

    @Schema(description = "아티클 태그 목록", example = "[\"제모\", \"피부관리\"]")
    private List<String> tags;

    @Schema(description = "생성 일시", example = "2025-01-01 12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;

    @Schema(description = "수정 일시", example = "2025-06-01 09:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp updatedAt;

    /**
     * Article 도메인과 태그 목록을 받아 응답 DTO 생성
     *
     * @param article 아티클 도메인 객체
     * @param tags    아티클 태그명 목록
     * @return ArticleResponse
     */
    public static ArticleResponse of(Article article, List<String> tags) {
        return ArticleResponse.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .subTitle(article.getSubTitle())
                .thumbnailUrl(article.getThumbnailUrl())
                .content(article.getContent())
                .tags(tags)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
