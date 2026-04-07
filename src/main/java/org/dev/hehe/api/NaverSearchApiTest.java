package org.dev.hehe.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 *
 * @author JOSH
 * @since 1.0.0 (26. 2. 27.)
 */


@Slf4j
public class NaverSearchApiTest {
    public void searchByNaver() {
        String clientId = "_GAu5Djr82tbcHyUJ6vn"; // 발급받은 ID
        String clientSecret = "jMACmRimho"; // 발급받은 Secret

        // 네이버는 '기기명 + 지역명' 조합이 결과가 더 잘 나옵니다.
        String query = "삼겹살";

        String url = UriComponentsBuilder.fromHttpUrl("https://openapi.naver.com/v1/search/local.json")
                .queryParam("query", query)
                .queryParam("display", 5) // 한번에 가져올 결과 수 (최대 5)
                .queryParam("start", 2)   // 시작 위치
                .queryParam("sort", "comment") // 업체 리뷰(코멘트) 순 정렬
                .build()
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        System.out.println("네이버 검색 결과: " + response.getBody());
    }
}
