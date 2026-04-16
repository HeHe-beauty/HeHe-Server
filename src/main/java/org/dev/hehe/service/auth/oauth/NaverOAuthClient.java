package org.dev.hehe.service.auth.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * 네이버 OAuth 클라이언트
 * - FE가 전달한 provider access token으로 네이버 유저 정보 조회
 * - 조회 대상 필드: response.id(socialId), response.nickname
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOAuthClient {

    private final WebClient webClient;

    @Value("${oauth.naver.user-info-uri}")
    private String userInfoUri;

    /**
     * 네이버 유저 정보 조회
     *
     * @param accessToken FE로부터 받은 네이버 provider access token
     * @return OAuthUserInfo (socialId, nickname, provider="naver")
     * @throws CommonException AU004 (네이버 API 호출 실패)
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            Map<?, ?> response = webClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.debug("[Naver OAuth] 응답 원문 - JOSH260416 : {}", response);

            Map<?, ?> userResponse = (Map<?, ?>) response.get("response");
            String socialId = (String) userResponse.get("id");
            String rawNickname = (String) userResponse.get("nickname");
            String rawName = (String) userResponse.get("name");
            // nickname → name → 기본값 순으로 적용
            String nickname;
            if (rawNickname != null && !rawNickname.isBlank()) {
                nickname = rawNickname;
            } else if (rawName != null && !rawName.isBlank()) {
                nickname = rawName;
                log.info("[Naver OAuth] nickname 없음 - name 값으로 대체: {}", rawName);
            } else {
                nickname = "네이버 유저";
                log.warn("[Naver OAuth] nickname, name 모두 없음 - 기본값 사용");
            }

            log.info("[Naver OAuth] 유저 정보 조회 성공 - JOSH260416 - socialId: {}, nickname: {}", socialId, nickname);

            return new NaverUserInfo(socialId, nickname);

        } catch (WebClientResponseException e) {
            log.warn("[Naver OAuth] 유저 정보 조회 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CommonException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
    }

    // ── Inner class ──────────────────────────────────────────────────────────

    private record NaverUserInfo(String socialId, String nickname) implements OAuthUserInfo {

        @Override
        public String getSocialId() { return socialId; }

        @Override
        public String getNickname() { return nickname; }

        @Override
        public String getProvider() { return "naver"; }
    }
}