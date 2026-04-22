package org.dev.hehe.domain.recentview;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 최근 본 병원 도메인
 *
 * <p>tb_recent_view와 tb_hospital을 JOIN한 결과를 매핑한다.</p>
 */
@Getter
@NoArgsConstructor
public class RecentView {

    /** 최근 본 내역 ID */
    private Long id;

    /** 병원 ID */
    private Long hospitalId;

    /** 병원명 (tb_hospital.name) */
    private String name;

    /** 병원 주소 (tb_hospital.address) */
    private String address;

    /** 최근 조회 시각 */
    private LocalDateTime viewedAt;
}