package org.dev.hehe.service.fcm;

import org.dev.hehe.dto.fcm.FcmTestResponse;
import org.dev.hehe.mapper.fcm.FcmMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

    @InjectMocks
    private FcmService fcmService;

    @Mock
    private FcmMapper fcmMapper;

    @Mock
    private FcmSendService fcmSendService;

    @Test
    @DisplayName("활성 토큰이 없으면 successCount와 failCount 모두 0을 반환한다")
    void sendTestPush_noActiveTokens_returnsZeroCounts() {
        given(fcmMapper.findActiveTokensByUserId(1L)).willReturn(List.of());

        FcmTestResponse response = fcmService.sendTestPush(1L);

        assertThat(response.getSuccessCount()).isZero();
        assertThat(response.getFailCount()).isZero();
        verify(fcmSendService, never()).sendMulticast(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("활성 토큰이 있으면 FCM 발송 후 성공 건수를 반환하고 이력을 저장한다")
    void sendTestPush_hasActiveTokens_returnsSuccessCountAndSavesHistory() {
        given(fcmMapper.findActiveTokensByUserId(1L)).willReturn(List.of("token1", "token2"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(2);

        FcmTestResponse response = fcmService.sendTestPush(1L);

        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getFailCount()).isZero();
        verify(fcmMapper).insertPushHistory(eq(1L), isNull(), anyString(), anyString(), eq(2), eq(0));
    }

    @Test
    @DisplayName("일부 발송 실패 시 failCount에 정확히 반영되고 이력을 저장한다")
    void sendTestPush_partialFailure_returnsCorrectCountsAndSavesHistory() {
        given(fcmMapper.findActiveTokensByUserId(1L)).willReturn(List.of("token1", "token2", "token3"));
        given(fcmSendService.sendMulticast(any(), anyString(), anyString())).willReturn(2);

        FcmTestResponse response = fcmService.sendTestPush(1L);

        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getFailCount()).isEqualTo(1);
        verify(fcmMapper).insertPushHistory(eq(1L), isNull(), anyString(), anyString(), eq(2), eq(1));
    }
}