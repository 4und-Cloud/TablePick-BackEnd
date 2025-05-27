package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.goorm.tablepick.global.exception.NotificationException;
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
        if (token == null || token.trim().isEmpty()) {
            log.error("FCM 토큰이 없어서 메시지를 보낼 수 없습니다.");
            return null;
        }
        
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
            
            // 토큰 관련 오류인 경우 MessagingErrorCode 확인
            if (e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT ||
                e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED) {
                log.warn("FCM 토큰이 유효하지 않습니다. 토큰: {}", token);
                return null;
            }
            
            throw new NotificationException("FCM 메시지 전송에 실패했습니다: " + e.getMessage(), "FCM_SEND_FAILED");
        } catch (Exception e) {
            log.error("FCM 메시지 전송 중 예상치 못한 오류 발생: {}", e.getMessage());
            throw new NotificationException("FCM 메시지 전송 중 예상치 못한 오류 발생: " + e.getMessage(), "FCM_UNEXPECTED_ERROR");
        }
    }
}
