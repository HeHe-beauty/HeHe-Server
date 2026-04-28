package org.dev.hehe.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase Admin SDK 초기화 설정
 * FIREBASE_CREDENTIALS_PATH 환경변수가 설정되지 않으면 FCM 기능이 비활성화된다.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    /**
     * 애플리케이션 기동 시 Firebase 초기화
     * 환경변수 미설정 시 경고 로그 출력 후 건너뜀 (FCM 비활성화)
     */
    @PostConstruct
    public void init() throws IOException {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("FIREBASE_CREDENTIALS_PATH가 설정되지 않아 FCM 기능이 비활성화됩니다.");
            return;
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase 초기화 완료: credentials={}", credentialsPath);
        }
    }
}