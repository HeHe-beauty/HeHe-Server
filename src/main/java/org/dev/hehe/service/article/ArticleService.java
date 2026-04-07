package org.dev.hehe.service.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.article.ArticleListResponse;
import org.dev.hehe.dto.article.ArticleResponse;
import org.dev.hehe.mapper.article.ArticleMapper;
import org.springframework.stereotype.Service;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;

import java.util.List;

/**
 * 추천 아티클 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;

    /**
     * 활성 아티클 목록을 우선순위 순으로 조회하여 목록용 응답 DTO로 변환
     * content(본문 HTML)는 용량이 크므로 목록 응답에서 제외
     *
     * @return 활성 아티클 목록 (priority DESC), content 미포함
     */
    public List<ArticleListResponse> getActiveArticles() {
        List<ArticleListResponse> articles = articleMapper.findAllArticle()
                .stream()
                .map(ArticleListResponse::from)
                .toList();
        log.debug("활성 아티클 조회 완료 - count={}", articles.size());
        return articles;
    }

    /**
     * article_id로 활성 아티클 단건 조회
     * 존재하지 않거나 비활성 아티클이면 ARTICLE_NOT_FOUND 예외 발생
     *
     * @param articleId 조회할 아티클 ID
     * @return 아티클 응답 DTO
     * @throws CommonException ARTICLE_NOT_FOUND
     */
    public ArticleResponse getArticleById(Long articleId) {
        log.debug("아티클 단건 조회 - articleId={}", articleId);
        return articleMapper.findByArticleId(articleId)
                .map(ArticleResponse::from)
                .orElseThrow(() -> {
                    log.warn("아티클을 찾을 수 없음 - articleId={}", articleId);
                    return new CommonException(ErrorCode.ARTICLE_NOT_FOUND);
                });
    }
}