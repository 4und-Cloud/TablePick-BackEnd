package com.goorm.tablepick.domain.notification.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.goorm.tablepick.global.exception.NotificationException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMServiceImpl implements FCMService {
    private final FirebaseMessaging firebaseMessaging;
    
    @Value("${server.domain:http://localhost:8080}")
    private String serverDomain;
    
    private boolean isValidToken(String token) {
        return token != null && !token.trim().isEmpty();
    }
    
    @Override
    public String sendMessage(String token, String title, String body, Map<String, String> data) {
        if (!isValidToken(token)) {
            log.warn("FCM 토큰이 null 또는 공백이라서 메시지를 보낼 수 없습니다.");
            System.out.println("FCM 토큰이 null 또는 공백이라서 메시지를 보낼 수 없습니다.");
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
            // FCM 알림 전송
            String response = firebaseMessaging.send(message, false);
            
            System.out.println("\n========== FCM 알림 전송 성공 ==========");
            
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송에 실패했습니다: {}", e.getMessage());
            
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
    
    @Override
    public String sendMessageWithLogo(String token, String title, String body, Map<String, String> data) {
        if (!isValidToken(token)) {
            log.warn("FCM 토큰이 null 또는 공백이라서 로고가 포함된 메시지를 보낼 수 없습니다.");
            System.out.println("FCM 토큰이 null 또는 공백이라서 로고가 포함된 메시지를 보낼 수 없습니다.");
            return null;
        }
        
        // 로고 이미지 URL 생성
        String logoUrl = serverDomain + "/images/logo.png";
        
        // 데이터에 제목, 내용, 이미지 추가 (서비스 워커에서 사용)
        data.put("title", title);
        data.put("body", body);
        data.put("image", logoUrl);
        
        // Notification 객체 생성 (로고 이미지 포함)
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .setImage(logoUrl)
                .build();
        
        // 알림과 데이터 모두 포함하는 메시지 생성
        Message message = Message.builder()
                .setNotification(notification)
                .putAllData(data)
                .setToken(token)
                .build();
        
        try {
            // FCM 알림 전송
            String response = firebaseMessaging.send(message, true);
            
            log.info("========== FCM 알림 동기 전송 요청 시작 ==========");
            
            System.out.println("\n========== FCM 알림 동기 전송 성공 ==========");
            
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM 로고 메시지 전송에 실패했습니다: {}", e.getMessage());
            
            if (e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT ||
                    e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED) {
                log.warn("FCM 토큰이 유효하지 않습니다. 토큰: {}", token);
                return null;
            }
            
            throw new NotificationException("FCM 로고 메시지 전송에 실패했습니다: " + e.getMessage(), "FCM_SEND_FAILED");
        } catch (Exception e) {
            log.error("FCM 로고 메시지 전송 중 예상치 못한 오류 발생: {}", e.getMessage());
            throw new NotificationException("FCM 로고 메시지 전송 중 예상치 못한 오류 발생: " + e.getMessage(), "FCM_UNEXPECTED_ERROR");
        }
    }
    
    @Override
    public ApiFuture<String> sendMessageAsync(String token, String title, String body, Map<String, String> data,
                                              boolean dryRun) {
        if (!isValidToken(token)) {
            log.warn("FCM 토큰이 null 또는 공백이라서 메시지를 보낼 수 없습니다.");
            System.out.println("FCM 토큰이 null 또는 공백이라서 메시지를 보낼 수 없습니다.");
            return ApiFutures.immediateFuture(null);
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
            
            ApiFuture<String> responseFuture = firebaseMessaging.sendAsync(message, dryRun);
            log.info("========== FCM 알림 비동기 전송 요청 시작 ==========");
            
            System.out.println("\n========== FCM 알림 비동기 전송 요청 시작 ==========");
            
            return responseFuture;
        } catch (Exception e) {
            log.error("FCM 비동기 메시지 전송 요청 중 예상치 못한 오류 발생: {}", e.getMessage());
            throw new NotificationException("FCM 비동기 메시지 전송 요청 중 예상치 못한 오류 발생: " + e.getMessage(),
                    "FCM_UNEXPECTED_ERROR");
        }
    }
}