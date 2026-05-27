package org.dev.hehe.domain.bookmark;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 병원별 찜 수 집계 도메인 (tb_bookmark GROUP BY hospital_id 결과)
 *
 * <p>병원 목록 조회 시 N+1 방지를 위한 배치 COUNT 쿼리 결과를 매핑한다.</p>
 */
@Getter
@NoArgsConstructor
public class HospitalBookmarkCount {

    /** 병원 ID */
    private Long hospitalId;

    /** 해당 병원을 찜한 사용자 수 */
    private int bookmarkCount;
}