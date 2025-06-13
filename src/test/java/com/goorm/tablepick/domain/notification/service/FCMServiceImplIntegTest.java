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
    @DisplayName("실제 FCM 서버를 통해 브라우저로 알림이 정상적으로 송수신이 된다.")
    void sendMessageToFcmServer() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "fWl5TIBryr_6_lBj6i7NMh:APA91bFzSm5E6CoIj5EV3e5kp7wylGaxCv6YfLN73KAm4r_TQwPPhKmecfMFySTEeSkkvJ2IaeByUAva6G9I5Vf23scItcQsBFFVoWOcePLhbECJ9GNMbac";
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
    @DisplayName("실제 FCM 서버를 통해 브라우저로 로고가 포함된 알림이 정상적으로 송수신이 된다.")
    void sendMessageWithLogoToFcmServer() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "fWl5TIBryr_6_lBj6i7NMh:APA91bFzSm5E6CoIj5EV3e5kp7wylGaxCv6YfLN73KAm4r_TQwPPhKmecfMFySTEeSkkvJ2IaeByUAva6G9I5Vf23scItcQsBFFVoWOcePLhbECJ9GNMbac";
        String title = "실제 로고 알림 테스트 제목";
        String body = "실제 로고 알림 테스트 내용입니다";
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
    
    @Test
    @DisplayName("유효하지 않은 FCM 토큰으로 알림 전송 시 null을 반환한다.")
    void sendMessageWithUnregisteredFcmToken() throws FirebaseMessagingException {
        // given
        String fcmToken = "invalid-fcm-token";
        String title = "유효하지 않은 토큰 테스트 제목";
        String body = "유효하지 않은 토큰 테스트 내용";
        Map<String, String> data = new HashMap<>();
        
        // when
        String response = fcmService.sendMessage(fcmToken, title, body, data);
        
        // then
        assertThat(response).isNull(); // 유효하지 않은 토큰은 null을 반환
    }
    
    @Test
    @DisplayName("null 또는 빈 FCM 토큰으로 알림 전송 시 null을 반환한다.")
    void sendMessageWithNullOrEmptyFcmToken() {
        // given
        String nullToken = null;
        String emptyToken = "";
        String blankToken = "   ";
        String title = "빈 토큰 테스트 제목";
        String body = "빈 토큰 테스트 내용";
        Map<String, String> data = new HashMap<>();
        
        // when
        String responseNull = fcmService.sendMessage(nullToken, title, body, data);
        String responseEmpty = fcmService.sendMessage(emptyToken, title, body, data);
        String responseBlank = fcmService.sendMessage(blankToken, title, body, data);
        
        // then
        assertThat(responseNull).isNull();
        assertThat(responseEmpty).isNull();
        assertThat(responseBlank).isNull();
        assertThat(data).isEmpty(); // 데이터가 수정되지 않았는지 확인
    }
    
}
