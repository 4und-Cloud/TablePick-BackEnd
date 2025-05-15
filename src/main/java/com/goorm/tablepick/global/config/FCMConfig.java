package com.goorm.tablepick.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;


// Firebase Cloud Messaging(FCM) 설정을 위한 Configuration 클래스 FCM을 통한
// 푸시 알림 발송을 위한 초기 설정을 담당
@Configuration
@Slf4j
public class FCMConfig {

    // Firebase Admin SDK 서비스 계정 키 파일 경로 application.yml에서 설정된 경로 주입받음
    @Value("${firebase.service-account-file}")
    private Resource serviceAccountResource;


    // Firebase 애플리케이션 초기화를 위한 Bean 설정 이미 초기화된 앱이 있다면 해당 앱을 반환하고, 없다면 새로 초기화
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 이미 초기화된 앱이 있는지 확인 (중복 초기화 방지)
        for (FirebaseApp app : FirebaseApp.getApps()) {
            if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                return app;
            }
        }

        try {
            // Firebase 앱 초기화를 위한 설정
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountResource.getInputStream()))
                    .build();

            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Firebase 초기화 실패: {}", e.getMessage());
            throw e;
        }
    }

    // FCM 서비스 사용을 위한 FirebaseMessaging 인스턴스를 생성 푸시 알림 발송에 사용
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
