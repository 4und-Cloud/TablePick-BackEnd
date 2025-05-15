package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMService { // FCM 메시지 전송
    private final FirebaseMessaging firebaseMessaging;

    public String sendMessage(String token, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .setToken(token)
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("성공적으로 메시지를 보냈어용 ^^: {}", response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송에 실패했어용 ㅠㅠ: {}", e.getMessage());
            throw new RuntimeException("FCM 메시지 전송에 실패했어용 ㅠㅠ", e);
        }
    }
}
