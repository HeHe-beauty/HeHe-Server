package org.dev.hehe.controller.bookmark;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.bookmark.BookmarkResponse;
import org.dev.hehe.service.bookmark.BookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BookmarkController 단위 테스트
 * - @WebMvcTest: Controller 레이어만 로드 (Service는 Mock 처리)
 * - @Import(SecurityConfig): JWT 필터 체인 적용
 * - JwtProvider를 Mock으로 등록하여 테스트 토큰("test-token") 인증 처리
 */
@WebMvcTest(BookmarkController.class)
@Import(SecurityConfig.class)
@DisplayName("BookmarkController 테스트")
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookmarkService bookmarkService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String TEST_TOKEN = "test-token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setup() {
        given(jwtProvider.getUserIdFromToken(TEST_TOKEN)).willReturn(TEST_USER_ID);
    }

    // =============================================
    // GET /api/v1/bookmarks 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/bookmarks - 찜 목록 조회 성공")
    void getBookmarks_success() throws Exception {
        // given
        BookmarkResponse r1 = createBookmarkResponse(101L, "강남 제모 클리닉", "서울 강남구 역삼동 1",
                List.of("여성원장", "주차가능"), LocalDateTime.of(2026, 4, 22, 10, 30));
        BookmarkResponse r2 = createBookmarkResponse(102L, "역삼 스킨케어", "서울 강남구 역삼동 2",
                List.of(), LocalDateTime.of(2026, 4, 20, 15, 0));

        given(bookmarkService.getBookmarks(TEST_USER_ID)).willReturn(List.of(r1, r2));

        // when & then
        mockMvc.perform(get("/api/v1/bookmarks")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].hospitalId").value(101))
                .andExpect(jsonPath("$.data[0].name").value("강남 제모 클리닉"))
                .andExpect(jsonPath("$.data[0].tags[0]").value("여성원장"))
                .andExpect(jsonPath("$.data[0].tags[1]").value("주차가능"))
                .andExpect(jsonPath("$.data[1].hospitalId").value(102))
                .andExpect(jsonPath("$.data[1].tags").isArray())
                .andExpect(jsonPath("$.data[1].tags").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/bookmarks - 찜한 병원 없음 (빈 배열 반환)")
    void getBookmarks_empty() throws Exception {
        // given
        given(bookmarkService.getBookmarks(TEST_USER_ID)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/bookmarks")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/bookmarks - 인증 토큰 없이 호출 시 4xx")
    void getBookmarks_unauthorized() throws Exception {
        // Spring Security STATELESS 설정에서 AuthenticationEntryPoint 미설정 시 403 반환
        mockMvc.perform(get("/api/v1/bookmarks"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    // =============================================
    // POST /api/v1/bookmarks/{hospitalId} 테스트
    // =============================================

    @Test
    @DisplayName("POST /api/v1/bookmarks/{hospitalId} - 찜 추가 성공 (201)")
    void addBookmark_success() throws Exception {
        // given
        willDoNothing().given(bookmarkService).addBookmark(TEST_USER_ID, 101L);

        // when & then
        mockMvc.perform(post("/api/v1/bookmarks/101")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/bookmarks/{hospitalId} - 이미 찜한 병원 409")
    void addBookmark_alreadyExists() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.BOOKMARK_ALREADY_EXISTS))
                .given(bookmarkService).addBookmark(TEST_USER_ID, 101L);

        // when & then
        mockMvc.perform(post("/api/v1/bookmarks/101")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("B001"));
    }

    // =============================================
    // DELETE /api/v1/bookmarks/{hospitalId} 테스트
    // =============================================

    @Test
    @DisplayName("DELETE /api/v1/bookmarks/{hospitalId} - 찜 삭제 성공 (200)")
    void removeBookmark_success() throws Exception {
        // given
        willDoNothing().given(bookmarkService).removeBookmark(TEST_USER_ID, 101L);

        // when & then
        mockMvc.perform(delete("/api/v1/bookmarks/101")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/bookmarks/{hospitalId} - 찜하지 않은 병원 404")
    void removeBookmark_notFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.BOOKMARK_NOT_FOUND))
                .given(bookmarkService).removeBookmark(TEST_USER_ID, 101L);

        // when & then
        mockMvc.perform(delete("/api/v1/bookmarks/101")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("B002"));
    }

    // =============================================
    // 헬퍼 메서드
    // =============================================

    private BookmarkResponse createBookmarkResponse(Long hospitalId, String name, String address,
                                                    List<String> tags, LocalDateTime bookmarkedAt) {
        BookmarkResponse response = BookmarkResponse.builder()
                .hospitalId(hospitalId)
                .name(name)
                .address(address)
                .tags(tags)
                .bookmarkedAt(bookmarkedAt)
                .build();
        return response;
    }
}