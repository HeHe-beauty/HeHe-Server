package org.dev.hehe.domain.hospital;

import lombok.Getter;

/**
 * 병원 목록 조회 결과 도메인 (tb_hospital 기본 정보 매핑)
 * MyBatis가 클러스터 내 병원 목록 쿼리 결과를 리플렉션으로 주입
 */
@Getter
public class HospitalSummary {

    /** 병원 ID */
    private Long hospitalId;

    /** 병원 명칭 */
    private String name;

    /** 도로명 주소 */
    private String address;
}