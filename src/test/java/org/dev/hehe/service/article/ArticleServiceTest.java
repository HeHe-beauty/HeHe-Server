package org.dev.hehe.service.article;

import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.domain.article.Article;
import org.dev.hehe.dto.article.ArticleListResponse;
import org.dev.hehe.dto.article.ArticleResponse;
import org.dev.hehe.mapper.article.ArticleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

/**
 * ArticleService 단위 테스트
 * - @ExtendWith(MockitoExtension): Spring Context 없이 순수 Mockito로 테스트
 * - Mapper를 Mock 처리하여 Service 비즈니스 로직만 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleService 테스트")
@Slf4j
class ArticleServiceTest {

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private ArticleService articleService;

    @Test
    @DisplayName("활성 아티클 목록 조회 및 ArticleListResponse DTO 변환 검증 (content 미포함)")
    void getActiveArticles_success() {
        // given: Article 도메인 객체 생성
        // MyBatis는 리플렉션으로 필드를 주입하므로 테스트에서도 ReflectionTestUtils 사용
        // content는 목록 쿼리에서 SELECT하지 않으므로 설정하지 않음
        Article article = new Article();
        ReflectionTestUtils.setField(article, "articleId", 1L);
        ReflectionTestUtils.setField(article, "title", "레이저 제모 완벽 가이드");
        ReflectionTestUtils.setField(article, "subTitle", "시작 전 알아야 할 것들");
        ReflectionTestUtils.setField(article, "thumbnailUrl", "https://example.com/thumb.jpg");

        given(articleMapper.findAllArticle()).willReturn(List.of(article));

        // when
        List<ArticleListResponse> result = articleService.getActiveArticles();

        // then: 크기 및 DTO 필드 매핑 검증 (content 필드 없음)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getArticleId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("레이저 제모 완벽 가이드");
        assertThat(result.get(0).getSubTitle()).isEqualTo("시작 전 알아야 할 것들");
        assertThat(result.get(0).getThumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");

        // verify: articleMapper.findAllArticle() 가 정확히 1회 호출됐는지 확인
        verify(articleMapper).findAllArticle();
    }

    @Test
    @DisplayName("활성 아티클이 없을 때 빈 리스트 반환")
    void getActiveArticles_emptyResult() {
        // given: mapper가 빈 목록을 반환하는 상황
        given(articleMapper.findAllArticle()).willReturn(List.of());

        // when
        List<ArticleListResponse> result = articleService.getActiveArticles();

        // then
        assertThat(result).isEmpty();
        verify(articleMapper).findAllArticle();
    }

    @Test
    @DisplayName("DB 조회 중 RuntimeException 발생 시 예외 전파")
    void getActiveArticles_mapperThrowsRuntimeException() {
        // given: mapper에서 DB 연결 오류 등 RuntimeException 발생 상황
        willThrow(new RuntimeException("DB connection error"))
                .given(articleMapper).findAllArticle();
        // when & then: Service에서 별도 처리 없이 예외가 그대로 전파되는지 검증
        // → GlobalExceptionHandler가 최종적으로 처리

        log.info("[getActiveArticles_mapperThrowsRuntimeException] Runtime Exception Error 발생");

        assertThatThrownBy(() -> articleService.getActiveArticles())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB connection error");

        verify(articleMapper).findAllArticle();
    }

    // =============================================
    // getArticleById 테스트
    // =============================================

    @Test
    @DisplayName("아티클 단건 조회 성공")
    void getArticleById_success() {
        // given
        Article article = new Article();
        ReflectionTestUtils.setField(article, "articleId", 1L);
        ReflectionTestUtils.setField(article, "title", "레이저 제모 완벽 가이드");
        ReflectionTestUtils.setField(article, "subTitle", "시작 전 알아야 할 것들");
        ReflectionTestUtils.setField(article, "thumbnailUrl", "https://example.com/thumb.jpg");
        ReflectionTestUtils.setField(article, "content", "<p>레이저 제모 본문 HTML</p>");

        given(articleMapper.findByArticleId(1L)).willReturn(Optional.of(article));

        // when
        ArticleResponse result = articleService.getArticleById(1L);

        // then: DTO 변환 정상 확인
        assertThat(result.getArticleId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("레이저 제모 완벽 가이드");
        verify(articleMapper).findByArticleId(1L);
    }

    @Test
    @DisplayName("존재하지 않는 articleId 조회 시 CommonException(ARTICLE_NOT_FOUND) 발생")
    void getArticleById_notFound() {
        // given: mapper가 빈 Optional 반환 → 아티클이 없는 상황
        given(articleMapper.findByArticleId(999L)).willReturn(Optional.empty());

        // when & then: CommonException 발생 여부 및 ErrorCode 검증
        assertThatThrownBy(() -> articleService.getArticleById(999L))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> {
                    CommonException ce = (CommonException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ARTICLE_NOT_FOUND);
                    assertThat(ce.getErrorCode().getCode()).isEqualTo("A001");
                    assertThat(ce.getMessage()).isEqualTo(ErrorCode.ARTICLE_NOT_FOUND.getMessage());
                });

        verify(articleMapper).findByArticleId(999L);
    }
}