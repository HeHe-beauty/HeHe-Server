package org.dev.hehe.service.auth.oauth;

/**
 * 소셜 로그인 제공자(카카오/네이버)로부터 조회한 유저 정보 공통 인터페이스
 */
public interface OAuthUserInfo {

    /** 소셜 제공자 고유 유저 ID */
    String getSocialId();

    /** 유저 닉네임 */
    String getNickname();

    /** 소셜 제공자 구분 (kakao / naver) */
    String getProvider();
}