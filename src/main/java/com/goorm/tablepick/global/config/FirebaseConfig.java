package com.goorm.tablepick.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

// Firebase 설정을 위한 구성 클래스 FCM(Firebase Cloud Messaging)을 사용하기 위한 초기 설정을 담당
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-file}") // Firebase 서비스 계정 키 파일 경로
    private Resource serviceAccountResource;


    @Bean // Firebase 애플리케이션 초기화를 위한 Bean 설정
    public FirebaseApp firebaseApp() throws IOException {
        // 이미 초기화된 앱이 있는지 확인
        // Firebase는 싱글톤 패턴을 사용하므로, 중복 초기화 방지
        for (FirebaseApp app : FirebaseApp.getApps()) {
            if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                return app;
            }
        }

        // 새로운 Firebase 앱 초기화
        // 서비스 계정 키를 사용하여 인증 정보 설정
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountResource.getInputStream()))
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean // Firebase Cloud Messaging 서비스를 사용하기 위한 Bean 설정
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
