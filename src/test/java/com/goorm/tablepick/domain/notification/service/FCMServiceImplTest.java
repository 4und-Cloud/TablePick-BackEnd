package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        Map<String, String> data = createMessageData("테스트 알림 제목", "테스트 알림 내용입니다", Map.of("key1", "value1", "key2", "value2"));
        String expectedResponse = "message_id_12345";
        
        when(firebaseMessaging.send(any(Message.class))).thenReturn(expectedResponse);

        // when
        String result = fcmService.sendMessage(validToken, data.get("title"), data.get("body"), data);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        
        verify(firebaseMessaging, times(1)).send(any(Message.class));
        
        assertThat(data)
                .extracting("title", "body")
                .contains(data.get("title"), data.get("body"));
    }
    
    private Map<String, String> createMessageData(String title, String body, Map<String, String> additionalData) {
        Map<String, String> data = new HashMap<>(additionalData);
        data.put("title", title);
        data.put("body", body);
        return data;
    }
}