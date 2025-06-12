package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private FCMService fcmService;
    
    @AfterEach
    void tearDown() {
        reset(firebaseMessaging);
    }
    
    @DisplayName("FCM 서비스가 메시지 전송 요청을 올바르게 처리하고 응답 ID를 반환한다.")
    @Test
    void sendMessage() throws FirebaseMessagingException {
        // given
        String fcmToken = "valid-fcm-token";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        additionalData.put("key2", "value2");
        String expectedResponse = "message_id_12345";
        
        when(firebaseMessaging.send(any(Message.class))).thenReturn(expectedResponse); // 메시지 객체 모킹
        
        // when
        String result = fcmService.sendMessage(fcmToken, title, body, additionalData);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse); // sendMessage가 반환한 값 검증
        
        assertThat(additionalData).containsEntry("title", title); // additionalData가 서비스 내에서 수정되었는지 확인
        assertThat(additionalData).containsEntry("body", body);
        assertThat(additionalData).containsEntry("key1", "value1");
        assertThat(additionalData).containsEntry("key2", "value2");
        
        verify(firebaseMessaging, times(1)).send(any(Message.class)); // 메시지 전송 메서드가 정확히 1번 호출되었는지 확인
    }
    
    
    @Test
    @DisplayName("FCM 서비스가 이미지가 포함된 메시지 전송 요청을 올바르게 전송하고 응답 ID를 반환한다.")
    void sendMessageWithLogo() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "valid-fcm-token";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        additionalData.put("key2", "value2");
        String expectedResponse = "message_id_12345";
        
        when(firebaseMessaging.send(any(Message.class))).thenReturn(expectedResponse);
        
        // when 실행
        String result = fcmService.sendMessageWithLogo(fcmToken, title, body, additionalData);
        
        // then 검증
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        
        assertThat(additionalData).containsEntry("title", title);
        assertThat(additionalData).containsEntry("body", body);
        assertThat(additionalData).containsKey("image");
        assertThat(additionalData.get("image")).contains("/images/logo.png"); // 이미지 확인
        assertThat(additionalData).containsEntry("key1", "value1");
        assertThat(additionalData).containsEntry("key2", "value2");
        
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }
    
    @DisplayName("FCM 토큰이 null일 때 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithNullToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given
        String fcmToken = null;
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        additionalData.put("key2", "value2");
        
        // when
        String result = fcmService.sendMessage(fcmToken, title, body, additionalData);
        
        // then
        assertThat(result).isNull(); // null을 반환하는지 확인
        verifyNoInteractions(firebaseMessaging); // firebaseMessaging이 호출되지 않았는지 확인
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 없어서 메시지를 보낼 수 없습니다."); // 로그 메시지 확인
    }
    
    @DisplayName("FCM 토큰이 비어있을 때 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithEmptyToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given
        String fcmToken = "  "; // 공백만 있는 토큰
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        additionalData.put("key2", "value2");
        
        // when
        String result = fcmService.sendMessage(fcmToken, title, body, additionalData);
        
        // then
        assertThat(result).isNull(); // null을 반환하는지 확인
        verifyNoInteractions(firebaseMessaging); // firebaseMessaging이 호출되지 않았는지 확인
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 없어서 메시지를 보낼 수 없습니다."); // 로그 메시지 확인
    }
    
    @DisplayName("FCM 토큰이 null일 때 로고가 포함된 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithLogoNullToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given
        String fcmToken = null;
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        
        // when
        String result = fcmService.sendMessageWithLogo(fcmToken, title, body, additionalData);
        
        // then
        assertThat(result).isNull(); // null을 반환하는지 확인
        verifyNoInteractions(firebaseMessaging); // firebaseMessaging이 호출되지 않았는지 확인
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 없어서 메시지를 보낼 수 없습니다."); // 로그 메시지 확인
    }
    
    @DisplayName("FCM 토큰이 비어있을 때 로고가 포함된 메시지 전송을 시도하지 않고 null을 반환한다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void sendMessageWithLogoEmptyToken(CapturedOutput capturedOutput) throws FirebaseMessagingException {
        // given
        String fcmToken = ""; // 빈 토큰
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        
        // when
        String result = fcmService.sendMessageWithLogo(fcmToken, title, body, additionalData);
        
        // then
        assertThat(result).isNull(); // null을 반환하는지 확인
        verifyNoInteractions(firebaseMessaging); // firebaseMessaging이 호출되지 않았는지 확인
        assertThat(capturedOutput.getOut()).contains("FCM 토큰이 없어서 메시지를 보낼 수 없습니다."); // 로그 메시지 확인
    }
    
    private Map<String, String> createMessageData(String title, String body, Map<String, String> additionalData) {
        Map<String, String> data = new HashMap<>(additionalData);
        data.put("title", title);
        data.put("body", body);
        return data;
    }
}