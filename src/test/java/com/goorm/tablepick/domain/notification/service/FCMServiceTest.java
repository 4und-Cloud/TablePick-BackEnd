package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FCMServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FCMService fcmService;

    private String token;
    private String title;
    private String body;
    private Map<String, String> data;

    @BeforeEach
    void setUp() {
        token = "test-fcm-token";
        title = "Test Title";
        body = "Test Body";
        data = new HashMap<>();
        data.put("reservationId", "1");
        data.put("type", "RESERVATION_1DAY_BEFORE");
        data.put("url", "/reservations/1");
    }

    @Test
    @DisplayName("FCM 메시지 전송 성공 테스트")
    void sendMessage_ShouldReturnMessageId() throws FirebaseMessagingException {
        // Given
        String messageId = "message-id-123";
        when(firebaseMessaging.send(any(Message.class))).thenReturn(messageId);

        // When
        String result = fcmService.sendMessage(token, title, body, data);

        // Then
        assertEquals(messageId, result);

        // Verify that send was called once with any Message
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("FCM 메시지 전송 실패 테스트")
    void sendMessage_WhenFirebaseThrowsException_ShouldThrowRuntimeException() throws FirebaseMessagingException {
        // Given
        FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(mockException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fcmService.sendMessage(token, title, body, data);
        });

        assertTrue(exception.getMessage().contains("FCM 메시지 전송에 실패했어용"));
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }
}
