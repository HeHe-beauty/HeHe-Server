package org.dev.hehe.service.fcm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.fcm.FcmTestResponse;
import org.dev.hehe.mapper.fcm.FcmMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FCM 발송 비즈니스 서비스
 * API 요청 기반 FCM 발송 로직을 담당한다. (스케줄러 기반 발송은 FcmScheduler 참고)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmMapper fcmMapper;
    private final FcmSendService fcmSendService;

    private static final String TEST_TITLE = "[헤헤] 테스트 알림";
    private static final String TEST_BODY = "FCM 푸시 알림이 정상적으로 작동하고 있어요.";

    /**
     * 유저의 활성 FCM 토큰으로 테스트 푸시 발송
     *
     * <p>활성 토큰이 없으면 successCount·failCount 모두 0을 반환한다.</p>
     *
     * @param userId 발송 대상 유저 ID
     * @return 발송 결과 (성공·실패 건수)
     */
    public FcmTestResponse sendTestPush(Long userId) {
        List<String> tokens = fcmMapper.findActiveTokensByUserId(userId);
        log.info("FCM 테스트 발송 요청: userId={}, activeTokenCount={}", userId, tokens.size());

        if (tokens.isEmpty()) {
            log.warn("활성 FCM 토큰 없음: userId={}", userId);
            return new FcmTestResponse(0, 0);
        }

        int successCount = fcmSendService.sendMulticast(tokens, TEST_TITLE, TEST_BODY);
        int failCount = tokens.size() - successCount;

        fcmMapper.insertPushHistory(userId, null, TEST_TITLE, TEST_BODY, successCount, failCount);
        log.info("FCM 테스트 발송 완료: userId={}, success={}, fail={}", userId, successCount, failCount);
        return new FcmTestResponse(successCount, failCount);
    }
}