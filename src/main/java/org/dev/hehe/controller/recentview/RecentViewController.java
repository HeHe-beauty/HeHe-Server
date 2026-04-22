package org.dev.hehe.controller.recentview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.recentview.RecentViewResponse;
import org.dev.hehe.service.recentview.RecentViewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 최근 본 병원 컨트롤러
 * Swagger 명세는 RecentViewApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recent-views")
@RequiredArgsConstructor
public class RecentViewController implements RecentViewApiSpecification {

    private final RecentViewService recentViewService;

    /** 최근 본 병원 목록 조회 */
    @Override
    @GetMapping
    public ApiResult<List<RecentViewResponse>> getRecentViews(@LoginUser Long userId) {
        log.info("[GET] /api/v1/recent-views - userId={}", userId);
        return ApiResult.ok(recentViewService.getRecentViews(userId));
    }

    /** 최근 본 병원 기록 */
    @Override
    @PostMapping("/{hospitalId}")
    public ApiResult<Void> recordRecentView(@PathVariable Long hospitalId, @LoginUser Long userId) {
        log.info("[POST] /api/v1/recent-views/{} - userId={}", hospitalId, userId);
        recentViewService.recordRecentView(userId, hospitalId);
        return ApiResult.ok(null);
    }
}