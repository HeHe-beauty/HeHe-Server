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
 * 카카오 OAuth 클라이언트
 * - FE가 전달한 provider access token으로 카카오 유저 정보 조회
 * - 조회 대상 필드: id(socialId), kakao_account.profile.nickname
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final WebClient webClient;

    @Value("${oauth.kakao.user-info-uri}")
    private String userInfoUri;

    /**
     * 카카오 유저 정보 조회
     *
     * @param accessToken FE로부터 받은 카카오 provider access token
     * @return OAuthUserInfo (socialId, nickname, provider="kakao")
     * @throws CommonException AU004 (카카오 API 호출 실패)
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            Map<?, ?> response = webClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String socialId = String.valueOf(response.get("id"));
            String nickname = extractKakaoNickname(response);

            log.info("[Kakao OAuth] 유저 정보 조회 성공 - socialId: {}", socialId);

            return new KakaoUserInfo(socialId, nickname);

        } catch (WebClientResponseException e) {
            log.warn("[Kakao OAuth] 유저 정보 조회 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CommonException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
    }

    /**
     * 카카오 응답에서 닉네임 추출
     * 응답 구조: { kakao_account: { profile: { nickname: "..." } } }
     */
    @SuppressWarnings("unchecked")
    private String extractKakaoNickname(Map<?, ?> response) {
        try {
            Map<?, ?> kakaoAccount = (Map<?, ?>) response.get("kakao_account");
            Map<?, ?> profile = (Map<?, ?>) kakaoAccount.get("profile");
            return (String) profile.get("nickname");
        } catch (Exception e) {
            log.warn("[Kakao OAuth] 닉네임 파싱 실패 - 기본값 사용");
            return "카카오 유저";
        }
    }

    // ── Inner class ──────────────────────────────────────────────────────────

    private record KakaoUserInfo(String socialId, String nickname) implements OAuthUserInfo {

        @Override
        public String getSocialId() { return socialId; }

        @Override
        public String getNickname() { return nickname; }

        @Override
        public String getProvider() { return "kakao"; }
    }
}