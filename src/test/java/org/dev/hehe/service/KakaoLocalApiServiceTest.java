package org.dev.hehe.service;

import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.kakao.GeoCoordinate;
import org.dev.hehe.dto.kakao.KakaoAddressResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class KakaoLocalApiServiceTest {

    @Autowired
    private KakaoLocalApiService kakaoLocalApiService;

    @Test
    @DisplayName("도로명 주소 → 위도/경도 변환")
    void getGeoCoordinate_도로명() {
        String address = "서울 강남구 테헤란로 123";

        Optional<GeoCoordinate> result = kakaoLocalApiService.getGeoCoordinate(address);

        assertThat(result).isPresent();

        GeoCoordinate coord = result.get();
        log.info("========== 결과 ==========");
        log.info("주소명      : {}", coord.getAddressName());
        log.info("도로명 주소  : {}", coord.getRoadAddressName());
        log.info("건물명      : {}", coord.getBuildingName());
        log.info("우편번호    : {}", coord.getZoneNo());
        log.info("위도 (y)   : {}", coord.getLatitude());
        log.info("경도 (x)   : {}", coord.getLongitude());
        log.info("==========================");
    }

    @Test
    @DisplayName("존재하지 않는 주소 → Optional.empty 반환")
    void getGeoCoordinate_없는주소() {
        String address = "존재하지않는주소999999";

        Optional<GeoCoordinate> result = kakaoLocalApiService.getGeoCoordinate(address);

        assertThat(result).isEmpty();
        log.info("없는 주소 처리 정상: Optional.empty 반환");
    }

    @Test
    @DisplayName("원본 응답 전체 파싱 확인")
    void searchAddress_원본응답() {
        String address = "서울 강남구 테헤란로 123";

        KakaoAddressResponse response = kakaoLocalApiService.searchAddress(address);

        assertThat(response).isNotNull();
        assertThat(response.getDocuments()).isNotEmpty();

        KakaoAddressResponse.Meta meta = response.getMeta();
        log.info("========== Meta ==========");
        log.info("총 결과 수    : {}", meta.getTotalCount());
        log.info("마지막 페이지 : {}", meta.isEnd());
        log.info("==========================");

        KakaoAddressResponse.Document doc = response.getDocuments().get(0);
        log.info("주소 타입    : {}", doc.getAddressType());
        log.info("x (경도)    : {}", doc.getX());
        log.info("y (위도)    : {}", doc.getY());

        if (doc.getRoadAddress() != null) {
            log.info("도로명      : {}", doc.getRoadAddress().getRoadName());
            log.info("건물명      : {}", doc.getRoadAddress().getBuildingName());
        }
    }

    @Test
    @DisplayName("여러 주소 일괄 변환")
    void getGeoCoordinates_복수주소() {
        List<String> addresses = List.of(
                "서울 강남구 테헤란로 123",
                "서울 종로구 세종대로 175",  // 광화문
                "부산 해운대구 해운대해변로 264"
        );

        List<GeoCoordinate> results = kakaoLocalApiService.getGeoCoordinates(addresses);

        assertThat(results).isNotEmpty();

        log.info("========== 일괄 변환 결과 ({}/{}건 성공) ==========", results.size(), addresses.size());
        results.forEach(coord ->
                log.info("주소: {} | 위도: {} | 경도: {}", coord.getAddressName(), coord.getLatitude(), coord.getLongitude())
        );
    }
}