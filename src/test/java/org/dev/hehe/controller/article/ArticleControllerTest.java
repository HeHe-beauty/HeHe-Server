package org.dev.hehe.controller.article;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.article.ArticleListResponse;
import org.dev.hehe.dto.article.ArticleResponse;
import org.dev.hehe.service.article.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ArticleController 단위 테스트
 * - @WebMvcTest: Controller 레이어만 로드 (Service는 Mock 처리)
 * - MockMvc를 통해 HTTP 요청/응답 검증
 */
@WebMvcTest(ArticleController.class)
@DisplayName("ArticleController 테스트")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Service 계층을 Mock으로 대체하여 Controller 로직만 검증
    @MockitoBean
    private ArticleService articleService;

    @Test
    @DisplayName("GET /api/v1/articles - 아티클 목록 정상 반환 (content 미포함)")
    void getArticles_success() throws Exception {
        // given: 서비스가 아티클 목록을 반환하도록 설정 (ArticleListResponse — content 없음)
        List<ArticleListResponse> mockArticles = List.of(
                ArticleListResponse.builder()
                        .articleId(1L)
                        .title("레이저 제모 완벽 가이드")
                        .subTitle("시작 전 알아야 할 것들")
                        .thumbnailUrl("https://example.com/thumb.jpg")
                        .build()
        );
        given(articleService.getActiveArticles()).willReturn(mockArticles);

        // when & then: 응답 구조 및 값 검증, content 필드가 응답에 없어야 함
        mockMvc.perform(get("/api/v1/articles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].articleId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("레이저 제모 완벽 가이드"))
                .andExpect(jsonPath("$.data[0].subTitle").value("시작 전 알아야 할 것들"))
                .andExpect(jsonPath("$.data[0].content").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/articles - 활성 아티클 없을 때 빈 배열 반환")
    void getArticles_emptyList() throws Exception {
        // given: 서비스가 빈 목록을 반환하는 상황
        given(articleService.getActiveArticles()).willReturn(List.of());

        // when & then: success=true 이면서 빈 배열인지 검증
        mockMvc.perform(get("/api/v1/articles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // =============================================
    // GET /api/v1/articles/{articleId} 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/articles/{articleId} - 단건 조회 성공")
    void getArticle_success() throws Exception {
        // given
        ArticleResponse mockArticle = ArticleResponse.builder()
                .articleId(1L)
                .title("레이저 제모 완벽 가이드")
                .subTitle("시작 전 알아야 할 것들")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .content("https://example.com/article/1")
                .build();
        given(articleService.getArticleById(1L)).willReturn(mockArticle);

        // when & then
        mockMvc.perform(get("/api/v1/articles/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.articleId").value(1))
                .andExpect(jsonPath("$.data.title").value("레이저 제모 완벽 가이드"));
    }

    @Test
    @DisplayName("GET /api/v1/articles/{articleId} - 존재하지 않는 ID 요청 시 404 반환")
    void getArticle_notFound() throws Exception {
        // given: 서비스에서 CommonException(ARTICLE_NOT_FOUND) 발생
        willThrow(new CommonException(ErrorCode.ARTICLE_NOT_FOUND))
                .given(articleService).getArticleById(999L);

        // when & then: GlobalExceptionHandler가 404로 변환하고 errorCode 포함 응답 검증
        mockMvc.perform(get("/api/v1/articles/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("A001"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ARTICLE_NOT_FOUND.getMessage()));
    }
}