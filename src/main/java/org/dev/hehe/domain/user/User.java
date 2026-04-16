package org.dev.hehe.domain.user;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * tb_user 테이블 매핑 도메인 객체
 *
 * id      : 내부 AUTO_INCREMENT PK
 * userId  : 비즈니스용 유저 ID (앱 내 식별자, JWT claim에 사용)
 * socialId: 소셜 제공자(카카오/네이버)의 고유 유저 ID
 * provider: 소셜 제공자 구분 (KAKAO / NAVER)
 */
@Getter
public class User {

    private Long id;
    private Long userId;
    private String socialId;
    private String provider;
    private String email;
    private String nickname;
    private String fcmToken;
    private boolean pushAgreed;
    private boolean nightAgreed;
    private boolean mktAgreed;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}