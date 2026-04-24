package org.dev.hehe.service.pushtoken;

import org.dev.hehe.dto.pushtoken.PushTokenDeactivateRequest;
import org.dev.hehe.dto.pushtoken.PushTokenRegisterRequest;
import org.dev.hehe.mapper.pushtoken.PushTokenMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * PushTokenService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PushTokenService 테스트")
class PushTokenServiceTest {

    @Mock
    private PushTokenMapper pushTokenMapper;

    @InjectMocks
    private PushTokenService pushTokenService;

    @Test
    @DisplayName("FCM 토큰 등록/갱신 성공")
    void registerToken_success() {
        // given
        PushTokenRegisterRequest request = new PushTokenRegisterRequest();
        ReflectionTestUtils.setField(request, "token", "test-fcm-token");
        ReflectionTestUtils.setField(request, "platform", "ANDROID");
        ReflectionTestUtils.setField(request, "notificationPermissionGranted", true);

        // when
        pushTokenService.registerToken(1L, request);

        // then
        verify(pushTokenMapper, times(1)).upsertToken(1L, "test-fcm-token", "ANDROID", true);
    }

    @Test
    @DisplayName("FCM 토큰 비활성화 성공")
    void deactivateToken_success() {
        // given
        PushTokenDeactivateRequest request = new PushTokenDeactivateRequest();
        ReflectionTestUtils.setField(request, "token", "test-fcm-token");

        given(pushTokenMapper.deactivateToken(1L, "test-fcm-token")).willReturn(1);

        // when
        pushTokenService.deactivateToken(1L, request);

        // then
        verify(pushTokenMapper, times(1)).deactivateToken(1L, "test-fcm-token");
    }

    @Test
    @DisplayName("FCM 토큰 비활성화 - 존재하지 않는 토큰이어도 정상 처리 (idempotent)")
    void deactivateToken_notFound_noException() {
        // given
        PushTokenDeactivateRequest request = new PushTokenDeactivateRequest();
        ReflectionTestUtils.setField(request, "token", "non-existent-token");

        given(pushTokenMapper.deactivateToken(1L, "non-existent-token")).willReturn(0);

        // when & then (예외 없이 정상 처리)
        pushTokenService.deactivateToken(1L, request);

        verify(pushTokenMapper, times(1)).deactivateToken(1L, "non-existent-token");
    }
}
