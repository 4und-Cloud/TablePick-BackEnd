package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FCMServiceImplIntegTest {
    
    @Autowired
    private FCMService fcmService;
    
    @Autowired
    private FirebaseMessaging firebaseMessaging;
    
    @Test
    @DisplayName("실제 FCM 서버를 통해 브라우저로 알림이 정상적으로 송수신이 된다.")
    void sendMessageToFcmServer() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "valid-fcm-token";
        String title = "실제 알림 테스트 제목";
        String body = "실제 알림 테스트 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessage(fcmToken, title, body, data);
        
        // then
        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainIgnoringCase("error")
                .doesNotContainIgnoringCase("fail")
                .doesNotContainIgnoringCase("exception");
    }
    
    @Test
    @DisplayName("실제 FCM 서버를 통해 브라우저로 로고가 포함된 알림이 정상적으로 송수신이 된다.")
    void sendMessageWithLogoToFcmServer() throws FirebaseMessagingException {
        // given 준비
        String fcmToken = "valid-fcm-token";
        String title = "실제 로고 알림 테스트 제목";
        String body = "실제 로고 알림 테스트 내용입니다";
        Map<String, String> data = new HashMap<>();
        
        // when 실행
        String response = fcmService.sendMessageWithLogo(fcmToken, title, body, data);
        
        // then
        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainIgnoringCase("error")
                .doesNotContainIgnoringCase("fail")
                .doesNotContainIgnoringCase("exception");
        
        assertThat(data.get("image")).contains("/images/logo.png");
    }
    
    @Test
    @DisplayName("유효하지 않은 FCM 토큰으로 알림 전송 시 null을 반환한다.")
    void sendMessageWithUnregisteredFcmToken() throws FirebaseMessagingException {
        // given
        String fcmToken = "invalid-fcm-token";
        String title = "유효하지 않은 토큰 테스트 제목";
        String body = "유효하지 않은 토큰 테스트 내용";
        Map<String, String> data = new HashMap<>();
        
        // when
        String response = fcmService.sendMessage(fcmToken, title, body, data);
        
        // then
        assertThat(response).isNull(); // 유효하지 않은 토큰은 null을 반환
    }
    
    @Test
    @DisplayName("null 또는 빈 FCM 토큰으로 알림 전송 시 null을 반환한다.")
    void sendMessageWithNullOrEmptyFcmToken() {
        // given
        String nullToken = null;
        String emptyToken = "";
        String blankToken = "   ";
        String title = "빈 토큰 테스트 제목";
        String body = "빈 토큰 테스트 내용";
        Map<String, String> data = new HashMap<>();
        
        // when
        String responseNull = fcmService.sendMessage(nullToken, title, body, data);
        String responseEmpty = fcmService.sendMessage(emptyToken, title, body, data);
        String responseBlank = fcmService.sendMessage(blankToken, title, body, data);
        
        // then
        assertThat(responseNull).isNull();
        assertThat(responseEmpty).isNull();
        assertThat(responseBlank).isNull();
        assertThat(data).isEmpty(); // 데이터가 수정되지 않았는지 확인
    }
    
    @Test
    @DisplayName("실제 FCM 서버를 통해 로고가 포함된 알림을 100개 동시에 전송한다.")
    void sendMultipleMessagesWithLogoToFcmServer() throws InterruptedException {
        // given 준비
        String fcmToken = "valid-fcm-token";
        int messageCount = 100;
        String baseTitle = "대량 로고 알림 테스트 제목";
        String baseBody = "대량 로고 알림 테스트 내용입니다";
        
        // ExecutorService를 사용하여 병렬 처리
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // when 실행 - 100개의 알림을 비동기로 전송
        for (int i = 1; i <= messageCount; i++) {
            final int messageNumber = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String title = baseTitle + " #" + messageNumber;
                    String body = baseBody + " (메시지 번호: " + messageNumber + ")";
                    Map<String, String> data = new HashMap<>();
                    data.put("messageNumber", String.valueOf(messageNumber));
                    data.put("batchId", "batch-" + System.currentTimeMillis());
                    
                    return fcmService.sendMessageWithLogo(fcmToken, title, body, data);
                } catch (Exception e) {
                    System.err.println("메시지 " + messageNumber + " 전송 실패: " + e.getMessage());
                    return null;
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 모든 작업 완료 대기
        List<String> responses = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            try {
                String response = future.get(30, TimeUnit.SECONDS); // 30초 타임아웃
                responses.add(response);
            } catch (Exception e) {
                System.err.println("Future 처리 중 오류: " + e.getMessage());
                responses.add(null);
            }
        }
        
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // then 검증
        System.out.println("\n========== 대량 알림 전송 결과 ==========");
        System.out.println("총 전송 시도: " + messageCount + "개");
        System.out.println("총 소요 시간: " + totalTime + "ms");
        System.out.println("평균 전송 시간: " + (totalTime / messageCount) + "ms/메시지");
        
        // 성공한 응답 개수 확인
        long successCount = responses.stream()
                .filter(response -> response != null && !response.isEmpty())
                .count();
        
        long failureCount = messageCount - successCount;
        
        System.out.println("성공한 전송: " + successCount + "개");
        System.out.println("실패한 전송: " + failureCount + "개");
        System.out.println("성공률: " + String.format("%.2f", (successCount * 100.0 / messageCount)) + "%");
        System.out.println("=======================================\n");
        
        // 검증: 최소 80% 이상의 성공률을 기대 (네트워크 상황에 따라 일부 실패 가능)
        assertThat(successCount).isGreaterThanOrEqualTo((long) (messageCount * 0.8));
        
        // 성공한 응답들이 모두 유효한지 확인
        responses.stream()
                .filter(response -> response != null)
                .forEach(response -> {
                    assertThat(response)
                            .isNotNull()
                            .isNotEmpty()
                            .doesNotContainIgnoringCase("error")
                            .doesNotContainIgnoringCase("fail")
                            .doesNotContainIgnoringCase("exception");
                });
    }
    
    @Test
    @DisplayName("실제 FCM 서버를 통해 로고가 포함된 알림을 100개 순차적으로 전송한다.")
    void sendSequentialMessagesWithLogoToFcmServer() {
        // given 준비
        String fcmToken = "valid-fcm-token";
        int messageCount = 100;
        String baseTitle = "순차 로고 알림 테스트 제목";
        String baseBody = "순차 로고 알림 테스트 내용입니다";
        
        List<String> responses = new ArrayList<>();
        List<String> failedMessages = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // when 실행 - 100개의 알림을 순차적으로 전송
        for (int i = 1; i <= messageCount; i++) {
            try {
                String title = baseTitle + " #" + i;
                String body = baseBody + " (메시지 번호: " + i + ")";
                Map<String, String> data = new HashMap<>();
                data.put("messageNumber", String.valueOf(i));
                data.put("batchId", "sequential-batch-" + System.currentTimeMillis());
                
                String response = fcmService.sendMessageWithLogo(fcmToken, title, body, data);
                responses.add(response);
                
                // 로고 이미지 URL이 데이터에 포함되었는지 확인
                assertThat(data.get("image")).contains("/images/logo.png");
                
                // FCM 서버 부하를 줄이기 위해 짧은 지연 추가
                Thread.sleep(50); // 50ms 지연
                
            } catch (Exception e) {
                System.err.println("메시지 " + i + " 전송 실패: " + e.getMessage());
                failedMessages.add("메시지 #" + i + ": " + e.getMessage());
                responses.add(null);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // then 검증
        System.out.println("\n========== 순차 대량 알림 전송 결과 ==========");
        System.out.println("총 전송 시도: " + messageCount + "개");
        System.out.println("총 소요 시간: " + totalTime + "ms");
        System.out.println("평균 전송 시간: " + (totalTime / messageCount) + "ms/메시지");
        
        // 성공한 응답 개수 확인
        long successCount = responses.stream()
                .filter(response -> response != null && !response.isEmpty())
                .count();
        
        long failureCount = messageCount - successCount;
        
        System.out.println("성공한 전송: " + successCount + "개");
        System.out.println("실패한 전송: " + failureCount + "개");
        System.out.println("성공률: " + String.format("%.2f", (successCount * 100.0 / messageCount)) + "%");
        
        if (!failedMessages.isEmpty()) {
            System.out.println("실패한 메시지들:");
            failedMessages.forEach(System.out::println);
        }
        
        System.out.println("=======================================\n");
        
        // 검증: 최소 90% 이상의 성공률을 기대 (순차 전송이므로 더 높은 성공률 기대)
        assertThat(successCount).isGreaterThanOrEqualTo((long) (messageCount * 0.9));
        
        // 성공한 응답들이 모두 유효한지 확인
        responses.stream()
                .filter(response -> response != null)
                .forEach(response -> {
                    assertThat(response)
                            .isNotNull()
                            .isNotEmpty()
                            .doesNotContainIgnoringCase("error")
                            .doesNotContainIgnoringCase("fail")
                            .doesNotContainIgnoringCase("exception");
                });
    }
    
}
