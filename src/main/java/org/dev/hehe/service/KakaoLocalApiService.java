package org.dev.hehe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.kakao.GeoCoordinate;
import org.dev.hehe.dto.kakao.KakaoAddressResponse;
import org.dev.hehe.dto.kakao.KakaoAddressResponse.Document;
import org.dev.hehe.dto.kakao.KakaoAddressResponse.RoadAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Kakao Local API - 주소 검색 서비스
 *
 * <p>주소 문자열을 입력받아 위도/경도 좌표로 변환합니다.
 *
 * @author JOSH
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLocalApiService {

    private static final String KAKAO_ADDRESS_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestTemplate restTemplate;

    @Value("${kakao.api.key}")
    private String restApiKey;

    /**
     * 주소를 좌표(위도, 경도)로 변환합니다.
     *
     * <p>결과가 여러 개인 경우 첫 번째 결과를 반환합니다.
     *
     * @param address 도로명 주소 또는 지번 주소
     * @return 변환된 좌표 정보. 검색 결과가 없으면 {@link Optional#empty()}
     */
    public Optional<GeoCoordinate> getGeoCoordinate(String address) {
        KakaoAddressResponse response = searchAddress(address);

        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            log.warn("주소 검색 결과 없음: address={}", address);
            return Optional.empty();
        }

        Document doc = response.getDocuments().get(0);
        RoadAddress roadAddress = doc.getRoadAddress();

        GeoCoordinate coordinate = GeoCoordinate.builder()
                .latitude(doc.getLatitude())
                .longitude(doc.getLongitude())
                .addressName(doc.getAddressName())
                .roadAddressName(roadAddress != null ? roadAddress.getAddressName() : null)
                .buildingName(roadAddress != null ? roadAddress.getBuildingName() : null)
                .zoneNo(roadAddress != null ? roadAddress.getZoneNo() : null)
                .build();

        log.info("주소 변환 성공 - address={}, latitude={}, longitude={}",
                address, coordinate.getLatitude(), coordinate.getLongitude());

        return Optional.of(coordinate);
    }

    /**
     * 주소 문자열로 Kakao Local API 전체 응답을 조회합니다.
     *
     * @param address 도로명 주소 또는 지번 주소
     * @return Kakao API 원본 응답. 실패 시 null
     */
    public KakaoAddressResponse searchAddress(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(KAKAO_ADDRESS_URL)
                .queryParam("query", address)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoAddressResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, KakaoAddressResponse.class
            );
            log.debug("Kakao API 응답 수신 - status={}", response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            log.error("Kakao Local API 호출 실패 - address={}, error={}", address, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 여러 주소를 한 번에 좌표로 변환합니다.
     *
     * @param addresses 주소 목록
     * @return 변환 성공한 좌표 목록 (실패한 주소는 제외됨)
     */
    public List<GeoCoordinate> getGeoCoordinates(List<String> addresses) {
        return addresses.stream()
                .map(this::getGeoCoordinate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}