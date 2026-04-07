package org.dev.hehe.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;

/**
 *
 *
 * @author JOSH
 * @since 1.0.0 (26. 2. 27.)
 */
@Slf4j
public class KakaoLocalApiTest {
    public void test() {
        String restApiKey = "8ec4ba2fd36498cb2f5107bc9bf805cc";
        String address = "서울특별시 강남구 테헤란로 123";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        // 1. 주소 인코딩 확인 (UriComponentsBuilder 권장)
        String url = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", address) // 여기서 자동 인코딩됨
                .build()
                .toUriString();

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // 결과가 JSON으로 출력되면 성공!
        System.out.println(response.getBody());
        log.info("경도 (latitude) : {}", response.getBody());

    }

    public void keywordTest() {
        String restApiKey = "8ec4ba2fd36498cb2f5107bc9bf805cc";
        String address = "젠틀맥스";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        // 1. 주소 인코딩 확인 (UriComponentsBuilder 권장)
        String url = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", address) // 여기서 자동 인코딩됨
                .build()
                .toUriString();

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // 결과가 JSON으로 출력되면 성공!
        System.out.println(response.getBody());

    }

    public void searchWithLocation() {
        // 1. 강남역 좌표 및 설정값
        String x = "127.02761971"; // 경도 (Longitude)
        String y = "37.497942";     // 위도 (Latitude)
        int radius = 5000;          // 반경 (미터 단위, 최대 20000)
        String keyword = "젠틀맥스";
        String restApiKey = "8ec4ba2fd36498cb2f5107bc9bf805cc"; // 이미지에서 확인된 키

        // 2. URI 빌더를 사용하여 파라미터 조립 (인코딩 자동 처리)
        String url = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", keyword)
                .queryParam("x", x)
                .queryParam("y", y)
                .queryParam("radius", radius)
                .queryParam("sort", "distance") // 거리순 정렬 (정확도순은 accuracy)
                .build()
                .toUriString();

        // 3. 호출 로직
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        System.out.println("강남역 인근 검색 결과: " + response.getBody());
    }
}
