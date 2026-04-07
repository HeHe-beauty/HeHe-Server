package org.dev.hehe.controller.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResponse;
import org.dev.hehe.dto.article.ArticleListResponse;
import org.dev.hehe.dto.article.ArticleResponse;
import org.dev.hehe.service.article.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 추천 아티클 컨트롤러
 * Swagger 명세는 ArticleApi 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController implements ArticleApiSpecification {

    private final ArticleService articleService;

    @Override
    @GetMapping("")
    public ApiResponse<List<ArticleListResponse>> getArticles() {
        log.info("[GET] /api/v1/articles - 아티클 목록 조회 요청");
        List<ArticleListResponse> articles = articleService.getActiveArticles();
        log.info("아티클 목록 조회 완료 - count={}", articles.size());
        return ApiResponse.ok(articles);
    }

    @Override
    @GetMapping("/{articleId}")
    public ApiResponse<ArticleResponse> getArticle(@PathVariable Long articleId) {
        log.info("[GET] /api/v1/articles/{} - 아티클 단건 조회 요청", articleId);
        ArticleResponse article = articleService.getArticleById(articleId);
        log.info("아티클 단건 조회 완료 - articleId={}", articleId);
        return ApiResponse.ok(article);
    }
}