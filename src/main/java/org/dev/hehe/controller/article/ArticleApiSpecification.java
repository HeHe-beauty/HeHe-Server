package org.dev.hehe.controller.article;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.article.ArticleListResponse;
import org.dev.hehe.dto.article.ArticleResponse;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Article API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 ArticleController
 */
@Tag(name = "Article", description = "추천 아티클 API")
public interface ArticleApiSpecification {

    @Operation(
            summary = "아티클 목록 조회",
            description = "활성 아티클을 우선순위 순으로 반환합니다. 본문(content)은 목록에서 제외합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "articleId": 1,
                                          "title": "레이저 제모 완벽 가이드",
                                          "subTitle": "시작 전 알아야 할 것들",
                                          "thumbnailUrl": "https://example.com/thumb.jpg"
                                        },
                                        {
                                          "articleId": 2,
                                          "title": "제모 후 관리 방법",
                                          "subTitle": "피부 진정 루틴 소개",
                                          "thumbnailUrl": "https://example.com/thumb2.jpg"
                                        }
                                      ]
                                    }
                                    """))
            )
    })
    ApiResult<List<ArticleListResponse>> getArticles();


    @Operation(summary = "아티클 단건 조회", description = "articleId로 특정 아티클을 조회합니다. 없으면 404를 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "articleId": 1,
                                        "title": "레이저 제모 완벽 가이드",
                                        "subTitle": "시작 전 알아야 할 것들",
                                        "thumbnailUrl": "https://example.com/thumb.jpg",
                                        "content": "<p>레이저 제모에 대한 본문 HTML 내용...</p>"
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "아티클 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "A001",
                                      "message": "아티클을 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<ArticleResponse> getArticle(@Parameter(description = "조회할 아티클 ID") @PathVariable Long articleId);
}