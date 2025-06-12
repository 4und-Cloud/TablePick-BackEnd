package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class FCMServiceImplIntegTest {
    
    @Autowired
    private FCMService fcmService;
    
    @Autowired
    private FirebaseMessaging firebaseMessaging;
    
    @Test
    @DisplayName("실제 FCM 서버로 데이터 메시지 전송이 성공적으로 이루어진다")
    void sendMessageToFcmServer() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "invalid-fcm-token";
        String title = "실제 알림 테스트 제목";
        String body = "실제 알림 테스트 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessage(fcmToken, title, body, data);
        
        // then
        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainIgnoringCase("error")
                .doesNotContainIgnoringCase("fail")
                .doesNotContainIgnoringCase("exception");
    }
    
    @Test
    @DisplayName("실제 FCM 서버로 로고가 포함된 알림 메시지 전송이 성공적으로 이루어진다")
    void sendMessageWithLogoToFcmServer() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "invalid-fcm-token";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessageWithLogo(fcmToken, title, body, data);
        
        // then
        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainIgnoringCase("error")
                .doesNotContainIgnoringCase("fail")
                .doesNotContainIgnoringCase("exception");
        
        assertThat(data.get("image")).contains("/images/logo.png");
    }
    
}
