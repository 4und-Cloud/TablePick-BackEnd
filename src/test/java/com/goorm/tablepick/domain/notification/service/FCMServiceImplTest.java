package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FCMServiceImplTest {
    
    @MockBean
    private FirebaseMessaging firebaseMessaging;
    
    @Autowired
    private FirebaseMessaging firebaseMessaging2;
    
    @Autowired
    private FCMService fcmService;
    
    @AfterEach
    void tearDown() {
        reset(firebaseMessaging);
        reset(firebaseMessaging2);
    }
    
    @DisplayName("FCM 서비스가 메시지 전송 요청을 올바르게 처리하고 응답 ID를 반환한다.")
    @Test
    void sendMessage() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "valid-fcm-token";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_12345"); // 메시지 객체 모킹
        
        // when 실행
        String response = fcmService.sendMessage(fcmToken, title, body, data);
        System.out.println(response);
        
        // then 검증
        assertThat(data).containsEntry("title", title); // createMessageData로 생성된 데이터 검증
        assertThat(data).containsEntry("body", body);
        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainIgnoringCase("error")
                .doesNotContainIgnoringCase("fail")
                .doesNotContainIgnoringCase("exception");
        
        verify(firebaseMessaging, times(1)).send(any(Message.class)); // 메시지 전송 메서드가 정확히 1번 호출되었는지 확인
    }
    
    @Test
    @DisplayName("FCM 서비스가 이미지가 포함된 메시지 전송 요청을 올바르게 전송하고 응답 ID를 반환한다.")
    void sendMessageWithLogo() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "valid-fcm-token";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_12345");
        
        // when 실행
        String response = fcmService.sendMessageWithLogo(fcmToken, title, body, data);
        
        // then 검증
        assertThat(data).containsEntry("title", title);
        assertThat(data).containsEntry("body", body);
        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainIgnoringCase("error")
                .doesNotContainIgnoringCase("fail")
                .doesNotContainIgnoringCase("exception");
        
        assertThat(data.get("image")).contains("/images/logo.png");
        
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }
    
    @DisplayName("FCM 토큰이 null일 때 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithNullToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given 준비
        String fcmToken = null;
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessage(fcmToken, title, body, data);
        
        // then 검증
        assertThat(response).isNull();
        assertThat(data).isEmpty();
        
        verify(firebaseMessaging, times(0)).send(any(Message.class));
        
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 null 또는 공백이라서 메시지를 보낼 수 없습니다.");
    }
    
    @DisplayName("FCM 토큰이 비어있을 때 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithEmptyToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "   ";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessage(fcmToken, title, body, data);
        
        // then 검증
        assertThat(response).isNull();
        assertThat(data).isEmpty();
        
        verify(firebaseMessaging, times(0)).send(any(Message.class));
        
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 null 또는 공백이라서 메시지를 보낼 수 없습니다.");
    }
    
    @DisplayName("FCM 토큰이 null일 때 로고가 포함된 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithLogoNullToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given 준비
        String fcmToken = null;
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessageWithLogo(fcmToken, title, body, data);
        
        // then 검증
        assertThat(response).isNull();
        assertThat(data).isEmpty();
        
        verify(firebaseMessaging, times(0)).send(any(Message.class));
        
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 null 또는 공백이라서 로고가 포함된 메시지를 보낼 수 없습니다.");
    }
    
    @DisplayName("FCM 토큰이 비어있을 때 로고가 포함된 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithLogoEmptyToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "   ";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessageWithLogo(fcmToken, title, body, data);
        
        // then 검증
        assertThat(response).isNull();
        assertThat(data).isEmpty();
        
        verify(firebaseMessaging, times(0)).send(any(Message.class));
        
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 null 또는 공백이라서 로고가 포함된 메시지를 보낼 수 없습니다.");
    }
    
}