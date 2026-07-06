package org.dev.hehe.controller.heart;

import lombok.RequiredArgsConstructor;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.heart.HeartResponse;
import org.dev.hehe.service.heart.HeartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 하트 컨트롤러
 * - 인증 불필요, 익명 접근 허용
 */
@RestController
@RequestMapping("/api/v1/hearts")
@RequiredArgsConstructor
public class HeartController implements HeartApiSpecification {

    private final HeartService heartService;

    /** 하트 추가 후 누적 수 반환 */
    @PostMapping
    public ApiResult<HeartResponse> addHeart() {
        return ApiResult.ok(heartService.addHeart());
    }

    /** 현재 누적 하트 수 조회 */
    @GetMapping
    public ApiResult<HeartResponse> getTotal() {
        return ApiResult.ok(heartService.getTotal());
    }
}
