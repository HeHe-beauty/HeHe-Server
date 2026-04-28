package org.dev.hehe.service.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.mapper.fcm.FcmMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Firebase Cloud Messaging 발송 서비스
 * 멀티캐스트 발송 및 만료 토큰 자동 비활성화를 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    private final FcmMapper fcmMapper;

    /**
     * 복수 FCM 토큰에 알림 멀티캐스트 발송
     *
     * <p>Firebase 미초기화 상태이면 발송 없이 0을 반환한다.
     * 발송 후 만료/무효 토큰(UNREGISTERED, INVALID_ARGUMENT)은 자동으로 비활성화된다.</p>
     *
     * @param tokens FCM 토큰 목록
     * @param title  알림 제목
     * @param body   알림 본문
     * @return 발송 성공 건수
     */
    public int sendMulticast(List<String> tokens, String title, String body) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FCM 비활성화 상태 — 발송 건너뜀: title={}", title);
            return 0;
        }
        if (tokens.isEmpty()) {
            return 0;
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM 발송 완료: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());

            deactivateExpiredTokens(tokens, response.getResponses());
            return response.getSuccessCount();
        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 중 오류 발생: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * BatchResponse에서 만료/무효 토큰을 찾아 비활성화
     *
     * @param tokens    발송에 사용한 토큰 목록 (순서 중요)
     * @param responses Firebase 개별 응답 목록 (tokens와 1:1 대응)
     */
    private void deactivateExpiredTokens(List<String> tokens, List<SendResponse> responses) {
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (response.isSuccessful()) continue;

            MessagingErrorCode errorCode = response.getException().getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                String expiredToken = tokens.get(i);
                log.warn("만료된 FCM 토큰 비활성화: token={}...", expiredToken.substring(0, Math.min(20, expiredToken.length())));
                fcmMapper.deactivateExpiredToken(expiredToken);
            }
        }
    }
}