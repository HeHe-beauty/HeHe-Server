package org.dev.hehe.domain.hospital;

import lombok.Getter;

/**
 * 병원 상세 조회 결과 도메인 (tb_hospital 전체 + 좌표 분리 매핑)
 * MyBatis가 ST_Y(location) AS lat, ST_X(location) AS lng 를 리플렉션으로 주입
 */
@Getter
public class HospitalDetail {

    /** 병원 ID */
    private Long hospitalId;

    /** 병원 명칭 */
    private String name;

    /** 도로명 주소 */
    private String address;

    /** 위도 (ST_Y(location)) */
    private Double lat;

    /** 경도 (ST_X(location)) */
    private Double lng;

    /** 문의 전화번호 */
    private String contactNumber;

    /** 외부 예약 링크 (NULL 허용) */
    private String contactUrl;
}