package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        String validToken = "fcm-token";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        additionalData.put("key2", "value2");
        String expectedResponse = "message_id_12345";

        when(firebaseMessaging.send(any(Message.class))).thenReturn(expectedResponse); // 메시지 객체 모킹

        // when
        String result = fcmService.sendMessage(validToken, title, body, additionalData);

        // then
        assertThat(additionalData).containsEntry("title", title); // additionalData가 서비스 내에서 수정되었는지 확인
        assertThat(additionalData).containsEntry("body", body);
        assertThat(additionalData).containsEntry("key1", "value1");
        assertThat(additionalData).containsEntry("key2", "value2");

        verify(firebaseMessaging, times(1)).send(any(Message.class)); // 메시지 전송 메서드가 정확히 1번 호출되었는지 확인

        assertThat(result).isEqualTo(expectedResponse); // sendMessage가 반환한 값 검증
    }



    @Test
    @DisplayName("FCM 서비스가 이미지가 포함된 메시지 전송 요청을 올바르게 전송하고 응답 ID를 반환한다.")
    void sendMessageWithLogo() throws FirebaseMessagingException {
        // given 준비
        String validToken = "fcm-token";
        String title = "테스트 알림 제목";
        String body = "테스트 알림 내용입니다";
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("key1", "value1");
        additionalData.put("key2", "value2");
        String expectedResponse = "message_id_12345";

        when(firebaseMessaging.send(any(Message.class))).thenReturn(expectedResponse);

        // when 실행
        String result = fcmService.sendMessageWithLogo(validToken, title, body, additionalData);

        // then 검증
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);

        verify(firebaseMessaging, times(1)).send(any(Message.class));

        assertThat(additionalData).containsEntry("title", title);
        assertThat(additionalData).containsEntry("body", body);
        assertThat(additionalData).containsKey("image");
        assertThat(additionalData.get("image")).contains("/images/logo.png"); // 이미지 확인
        assertThat(additionalData).containsEntry("key1", "value1");
        assertThat(additionalData).containsEntry("key2", "value2");
    }

    private Map<String, String> createMessageData(String title, String body, Map<String, String> additionalData) {
        Map<String, String> data = new HashMap<>(additionalData);
        data.put("title", title);
        data.put("body", body);
        return data;
    }
}