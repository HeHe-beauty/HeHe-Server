package org.dev.hehe.domain.pushtoken;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FCM 푸시 토큰 도메인 (tb_push_token 매핑)
 */
@Getter
@NoArgsConstructor
public class PushToken {

    /** 푸시 토큰 ID */
    private Long id;

    /** 유저 ID */
    private Long userId;

    /** FCM 디바이스 토큰 */
    private String token;

    /** 플랫폼 (ANDROID/IOS) */
    private String platform;

    /** OS 알림 권한 허용 여부 */
    private boolean notificationPermissionGranted;

    /** 활성 여부 */
    private boolean isActive;

    /** 생성 시각 */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;
}