package org.dev.hehe.dto.kakao;

import lombok.Builder;
import lombok.Getter;

/**
 * 주소 → 좌표 변환 결과를 담는 DTO
 * <p>
 * Kakao Local API 응답에서 핵심 좌표 정보만 추출한 결과 객체입니다.
 */
@Getter
@Builder
public class GeoCoordinate {

    /** 위도 (Latitude) - Kakao 응답의 y 값 */
    private final double latitude;

    /** 경도 (Longitude) - Kakao 응답의 x 값 */
    private final double longitude;

    /** 검색에 사용된 주소명 (도로명 또는 지번) */
    private final String addressName;

    /** 도로명 주소 */
    private final String roadAddressName;

    /** 건물명 */
    private final String buildingName;

    /** 우편번호 */
    private final String zoneNo;
}