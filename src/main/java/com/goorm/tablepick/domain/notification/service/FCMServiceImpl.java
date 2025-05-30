package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.goorm.tablepick.global.exception.NotificationException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMServiceImpl implements FCMService {
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public String sendMessage(String token, String title, String body, Map<String, String> data) {
        if (token == null || token.trim().isEmpty()) {
            log.error("FCM 토큰이 없어서 메시지를 보낼 수 없습니다.");
            return null;
        }

        // 데이터에 제목과 내용 추가 (서비스 워커에서 사용)
        data.put("title", title);
        data.put("body", body);

        // 데이터 메시지 형식으로 변경 (notification 필드 제거)
        Message message = Message.builder()
                .putAllData(data)
                .setToken(token)
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("========== FCM 알림 전송 성공 ==========");
            log.info("수신자 토큰: {}", token);
            log.info("제목: {}", title);
            log.info("내용: {}", body);
            log.info("데이터: {}", data);
            log.info("응답: {}", response);
            log.info("메시지 타입: 데이터 메시지 (서비스 워커에서 처리)");
            log.info("=======================================");

            // 브라우저 콘솔에서도 확인할 수 있도록 System.out으로도 출력
            System.out.println("\n========== FCM 알림 전송 성공 ==========");
            System.out.println("제목: " + title);
            System.out.println("내용: " + body);
            System.out.println("데이터: " + data);
            System.out.println("메시지 타입: 데이터 메시지 (서비스 워커에서 처리)");
            System.out.println("=======================================\n");

            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송에 실패했어용 ㅠㅠ: {}", e.getMessage());

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
