package org.dev.hehe.service.heart;
// test6

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.heart.HeartResponse;
import org.dev.hehe.mapper.heart.HeartMapper;
import org.springframework.stereotype.Service;

/**
 * 하트 서비스
 * - 인증 없이 익명으로 하트 추가 및 누적 수 조회
 * - 중복 차단 없음 (프론트 sessionStorage에서 UX 제한)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeartService {

    private final HeartMapper heartMapper;

    /**
     * 하트 1건 추가 후 누적 수 반환
     *
     * @return 추가 후 전체 누적 하트 수
     */
    public HeartResponse addHeart() {
        heartMapper.insert();
        Long total = heartMapper.countAll();
        log.info("하트 추가 완료 - total={}", total);
        return HeartResponse.of(total);
    }

    /**
     * 현재 누적 하트 수 조회
     *
     * @return 전체 누적 하트 수
     */
    public HeartResponse getTotal() {
        Long total = heartMapper.countAll();
        log.debug("하트 수 조회 - total={}", total);
        return HeartResponse.of(total);
    }
}
