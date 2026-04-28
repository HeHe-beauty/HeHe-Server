package org.dev.hehe.service.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import org.dev.hehe.mapper.fcm.FcmMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmSendServiceTest {

    @InjectMocks
    private FcmSendService fcmSendService;

    @Mock
    private FcmMapper fcmMapper;

    @Test
    @DisplayName("Firebase 미초기화 상태이면 발송 없이 0을 반환한다")
    void sendMulticast_firebaseNotInitialized_returnsZero() {
        try (MockedStatic<FirebaseApp> mockedApp = mockStatic(FirebaseApp.class)) {
            mockedApp.when(FirebaseApp::getApps).thenReturn(List.of());

            int result = fcmSendService.sendMulticast(List.of("token1"), "제목", "본문");

            assertThat(result).isZero();
            verify(fcmMapper, never()).deactivateExpiredToken(any());
        }
    }

    @Test
    @DisplayName("토큰 목록이 비어 있으면 발송 없이 0을 반환한다")
    void sendMulticast_emptyTokens_returnsZero() {
        try (MockedStatic<FirebaseApp> mockedApp = mockStatic(FirebaseApp.class)) {
            mockedApp.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));

            int result = fcmSendService.sendMulticast(List.of(), "제목", "본문");

            assertThat(result).isZero();
        }
    }

    @Test
    @DisplayName("전체 발송 성공 시 성공 건수를 반환한다")
    void sendMulticast_allSuccess_returnsSuccessCount() throws FirebaseMessagingException {
        try (MockedStatic<FirebaseApp> mockedApp = mockStatic(FirebaseApp.class);
             MockedStatic<FirebaseMessaging> mockedMessaging = mockStatic(FirebaseMessaging.class)) {

            mockedApp.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));

            SendResponse successResponse = mock(SendResponse.class);
            given(successResponse.isSuccessful()).willReturn(true);

            BatchResponse batchResponse = mock(BatchResponse.class);
            given(batchResponse.getSuccessCount()).willReturn(2);
            given(batchResponse.getFailureCount()).willReturn(0);
            given(batchResponse.getResponses()).willReturn(List.of(successResponse, successResponse));

            FirebaseMessaging mockMessaging = mock(FirebaseMessaging.class);
            given(mockMessaging.sendEachForMulticast(any())).willReturn(batchResponse);
            mockedMessaging.when(FirebaseMessaging::getInstance).thenReturn(mockMessaging);

            int result = fcmSendService.sendMulticast(List.of("token1", "token2"), "제목", "본문");

            assertThat(result).isEqualTo(2);
            verify(fcmMapper, never()).deactivateExpiredToken(any());
        }
    }

    @Test
    @DisplayName("UNREGISTERED 오류 토큰은 발송 후 비활성화된다")
    void sendMulticast_unregisteredToken_deactivatesToken() throws FirebaseMessagingException {
        try (MockedStatic<FirebaseApp> mockedApp = mockStatic(FirebaseApp.class);
             MockedStatic<FirebaseMessaging> mockedMessaging = mockStatic(FirebaseMessaging.class)) {

            mockedApp.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));

            // 첫 번째 토큰: 성공
            SendResponse successResponse = mock(SendResponse.class);
            given(successResponse.isSuccessful()).willReturn(true);

            // 두 번째 토큰: UNREGISTERED 오류
            FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
            given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNREGISTERED);
            SendResponse failResponse = mock(SendResponse.class);
            given(failResponse.isSuccessful()).willReturn(false);
            given(failResponse.getException()).willReturn(exception);

            BatchResponse batchResponse = mock(BatchResponse.class);
            given(batchResponse.getSuccessCount()).willReturn(1);
            given(batchResponse.getFailureCount()).willReturn(1);
            given(batchResponse.getResponses()).willReturn(List.of(successResponse, failResponse));

            FirebaseMessaging mockMessaging = mock(FirebaseMessaging.class);
            given(mockMessaging.sendEachForMulticast(any())).willReturn(batchResponse);
            mockedMessaging.when(FirebaseMessaging::getInstance).thenReturn(mockMessaging);

            int result = fcmSendService.sendMulticast(List.of("token1", "expired-token-2"), "제목", "본문");

            assertThat(result).isEqualTo(1);
            verify(fcmMapper).deactivateExpiredToken("expired-token-2");
        }
    }

    @Test
    @DisplayName("FirebaseMessagingException 발생 시 0을 반환한다")
    void sendMulticast_firebaseException_returnsZero() throws FirebaseMessagingException {
        try (MockedStatic<FirebaseApp> mockedApp = mockStatic(FirebaseApp.class);
             MockedStatic<FirebaseMessaging> mockedMessaging = mockStatic(FirebaseMessaging.class)) {

            mockedApp.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));

            FirebaseMessaging mockMessaging = mock(FirebaseMessaging.class);
            given(mockMessaging.sendEachForMulticast(any())).willThrow(mock(FirebaseMessagingException.class));
            mockedMessaging.when(FirebaseMessaging::getInstance).thenReturn(mockMessaging);

            int result = fcmSendService.sendMulticast(List.of("token1"), "제목", "본문");

            assertThat(result).isZero();
        }
    }
}
