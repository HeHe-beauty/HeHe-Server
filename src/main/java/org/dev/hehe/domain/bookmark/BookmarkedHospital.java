package org.dev.hehe.domain.bookmark;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 찜한 병원 정보 도메인 (tb_bookmark JOIN tb_hospital 결과)
 */
@Getter
@NoArgsConstructor
public class BookmarkedHospital {

    /** 병원 ID */
    private Long hospitalId;

    /** 병원명 */
    private String name;

    /** 도로명 주소 */
    private String address;

    /** 찜한 시각 (tb_bookmark.created_at) */
    private LocalDateTime bookmarkedAt;
}