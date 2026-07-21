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

    @Value("${oauth.kakao.unlink-uri}")
    private String unlinkUri;

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

            log.debug("[Kakao OAuth] 응답 원문 - JOSH260416: {}", response);

            String socialId = String.valueOf(response.get("id"));
            String nickname = extractKakaoNickname(response);
            String email = extractKakaoEmail(response);

            log.info("[Kakao OAuth] 유저 정보 조회 성공 - JOSH260416 - socialId: {}, nickname: {}", socialId, nickname);

            return new KakaoUserInfo(socialId, nickname, email);

        } catch (WebClientResponseException e) {
            log.warn("[Kakao OAuth] 유저 정보 조회 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CommonException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
    }

    /**
     * 카카오 연결 끊기 (unlink) — 회원 탈퇴 시 호출
     *
     * <p>유저 access token으로 호출하면 admin key 없이도 연결을 해제할 수 있다.</p>
     *
     * @param accessToken FE가 탈퇴 시점에 재획득한 카카오 access token
     * @return unlink 성공 여부
     */
    public boolean unlink(String accessToken) {
        try {
            webClient.post()
                    .uri(unlinkUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("[Kakao OAuth] 연결 끊기 성공");
            return true;

        } catch (WebClientResponseException e) {
            log.warn("[Kakao OAuth] 연결 끊기 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return false;
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
            String nickname = (String) profile.get("nickname");
            // nickname → name → 기본값 순으로 적용
            if (nickname != null && !nickname.isBlank()) {
                return nickname;
            }
            String name = (String) profile.get("name");
            if (name != null && !name.isBlank()) {
                log.info("[Kakao OAuth] nickname 없음 - name 값으로 대체: {}", name);
                return name;
            }
            log.warn("[Kakao OAuth] nickname, name 모두 없음 - 기본값 사용");
            return "카카오 유저";
        } catch (Exception e) {
            log.warn("[Kakao OAuth] 닉네임 파싱 실패 - 기본값 사용");
            return "카카오 유저";
        }
    }

    /**
     * 카카오 응답에서 이메일 추출
     * 응답 구조: { kakao_account: { email: "..." } } — 이메일 제공 동의 없으면 필드 자체가 없음
     */
    private String extractKakaoEmail(Map<?, ?> response) {
        try {
            Map<?, ?> kakaoAccount = (Map<?, ?>) response.get("kakao_account");
            return (String) kakaoAccount.get("email");
        } catch (Exception e) {
            log.debug("[Kakao OAuth] 이메일 없음 (제공 동의 없거나 미제공)");
            return null;
        }
    }

    // ── Inner class ──────────────────────────────────────────────────────────

    private record KakaoUserInfo(String socialId, String nickname, String email) implements OAuthUserInfo {

        @Override
        public String getSocialId() { return socialId; }

        @Override
        public String getNickname() { return nickname; }

        @Override
        public String getEmail() { return email; }

        @Override
        public String getProvider() { return "kakao"; }
    }
}