package org.dev.hehe.controller.bookmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.bookmark.BookmarkResponse;
import org.dev.hehe.service.bookmark.BookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 찜(북마크) 컨트롤러
 * Swagger 명세는 BookmarkApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController implements BookmarkApiSpecification {

    private final BookmarkService bookmarkService;

    /** 찜한 병원 목록 조회 */
    @Override
    @GetMapping
    public ApiResult<List<BookmarkResponse>> getBookmarks(@LoginUser Long userId) {
        log.info("[GET] /api/v1/bookmarks - userId={}", userId);
        List<BookmarkResponse> response = bookmarkService.getBookmarks(userId);
        log.info("찜 목록 조회 완료 - userId={}, count={}", userId, response.size());
        return ApiResult.ok(response);
    }

    /** 병원 찜 추가 */
    @Override
    @PostMapping("/{hospitalId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<Void> addBookmark(@LoginUser Long userId, @PathVariable Long hospitalId) {
        log.info("[POST] /api/v1/bookmarks/{} - userId={}", hospitalId, userId);
        bookmarkService.addBookmark(userId, hospitalId);
        return ApiResult.ok(null);
    }

    /** 병원 찜 삭제 */
    @Override
    @DeleteMapping("/{hospitalId}")
    public ApiResult<Void> removeBookmark(@LoginUser Long userId, @PathVariable Long hospitalId) {
        log.info("[DELETE] /api/v1/bookmarks/{} - userId={}", hospitalId, userId);
        bookmarkService.removeBookmark(userId, hospitalId);
        return ApiResult.ok(null);
    }
}