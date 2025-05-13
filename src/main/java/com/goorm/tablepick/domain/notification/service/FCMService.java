package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FCMService {
    private final FirebaseMessaging firebaseMessaging;

    public FCMService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
        log.info("FCM 서비스 생성");
    }

    /**
     * 단일 디바이스에 FCM 알림을 전송
     *
     * @param token FCM 등록 토큰
     * @param title 알림 제목
     * @param body  알림 내용
     * @param data  추가 데이터 (key-value)
     * @return 메시지 전송 결과 ID
     * @throws IllegalArgumentException 토큰이 null이거나 비어있는 경우
     * @throws RuntimeException         FCM 메시지 전송 실패 시
     */
    public String sendMessage(String token, String title, String body, Map<String, String> data) {
        // 토큰 유효성 검사
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("FCM 토큰이 없습니다.");
        }

        try {
            // FCM 메시지 구성
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    // 추가 데이터가 없는 경우 빈 Map 사용
                    .putAllData(data != null ? data : new HashMap<>())
                    .build();

            // 메시지 비동기 전송 및 결과 대기
            String response = firebaseMessaging.sendAsync(message).get();
            log.info("FCM 메시지 전송 성공 - 토큰: {}, 제목: {}", token, title);
            return response;
        } catch (InterruptedException | ExecutionException e) {
            log.error("FCM 메시지 전송 실패 - 토큰: {}, 제목: {}", token, title, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("FCM 메시지 전송 실패", e);
        }
    }

    /**
     * 다수의 디바이스에 동일한 FCM 알림을 전송
     *
     * @param tokens FCM 등록 토큰 리스트
     * @param title  알림 제목
     * @param body   알림 내용
     * @param data   추가 데이터 (key-value)
     * @return 배치 전송 결과
     * @throws IllegalArgumentException 토큰 리스트가 null이거나 비어있는 경우
     * @throws RuntimeException         FCM 메시지 전송 실패 시
     */
    public BatchResponse sendMulticastMessage(List<String> tokens, String title, String body,
                                              Map<String, String> data) {
        // 토큰 리스트 유효성 검사
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("FCM 토큰 목록이 비어있습니다.");
        }

        try {
            // 멀티캐스트 메시지 구성
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    // 추가 데이터가 없는 경우 빈 Map 사용
                    .putAllData(data != null ? data : new HashMap<>())
                    .build();

            // 메시지 비동기 전송 및 결과 대기
            BatchResponse response = firebaseMessaging.sendMulticastAsync(message).get();
            log.info("FCM 멀티캐스트 메시지 전송 - 성공: {}/{}, 제목: {}",
                    response.getSuccessCount(), tokens.size(), title);

            // 실패한 메시지 처리 및 로깅
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.error("토큰 {} 에 대한 메시지 전송 실패: {}",
                                tokens.get(i), responses.get(i).getException().getMessage());
                    }
                }
            }

            return response;
        } catch (InterruptedException | ExecutionException e) {
            log.error("FCM 멀티캐스트 메시지 전송 실패 - 제목: {}", title, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("FCM 멀티캐스트 메시지 전송 실패", e);
        }
    }
}
