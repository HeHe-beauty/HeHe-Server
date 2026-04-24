package org.dev.hehe.service.pushtoken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.pushtoken.PushTokenDeactivateRequest;
import org.dev.hehe.dto.pushtoken.PushTokenRegisterRequest;
import org.dev.hehe.mapper.pushtoken.PushTokenMapper;
import org.springframework.stereotype.Service;

/**
 * FCM 푸시 토큰 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenMapper pushTokenMapper;

    /**
     * FCM 토큰 등록 또는 갱신
     *
     * <p>동일 토큰이 이미 존재하면 userId·플랫폼·알림권한·활성여부를 갱신한다.</p>
     *
     * @param userId  JWT에서 추출한 유저 ID
     * @param request 토큰 등록 요청 DTO
     */
    public void registerToken(Long userId, PushTokenRegisterRequest request) {
        log.info("FCM 토큰 등록/갱신 - userId={}, platform={}", userId, request.getPlatform());
        pushTokenMapper.upsertToken(userId, request.getToken(), request.getPlatform(),
                request.getNotificationPermissionGranted());
        log.info("FCM 토큰 등록/갱신 완료 - userId={}", userId);
    }

    /**
     * FCM 토큰 비활성화 (로그아웃 시 호출)
     *
     * <p>본인 토큰이 아니거나 존재하지 않아도 예외 없이 정상 처리한다 (idempotent).</p>
     *
     * @param userId  JWT에서 추출한 유저 ID
     * @param request 토큰 비활성화 요청 DTO
     */
    public void deactivateToken(Long userId, PushTokenDeactivateRequest request) {
        log.info("FCM 토큰 비활성화 - userId={}", userId);
        int updated = pushTokenMapper.deactivateToken(userId, request.getToken());
        log.info("FCM 토큰 비활성화 완료 - userId={}, updated={}", userId, updated);
    }
}
