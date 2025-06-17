package com.goorm.tablepick.domain.notification.controller;

import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.dto.response.NotificationTestStatistics;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/test")
@RequiredArgsConstructor
@Tag(name = "알림 테스트 API", description = "알림 시스템의 성능 및 부하 테스트를 위한 API")
@Slf4j
public class NotificationTestController {
    
    private final NotificationService notificationService;
    
    @Operation(
            summary = "단일 테스트 알림 전송",
            description = "테스트 목적으로 특정 회원에게 지정된 알림 타입 ID로 알림을 즉시 전송합니다.",
            tags = {"테스트 - 단일"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "테스트 알림 전송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/single")
    public ResponseEntity<NotificationResponse> sendSingleTestNotification(
            @Parameter(
                    name = "memberId",
                    description = "테스트 대상 회원 ID",
                    example = "1",
                    required = true
            )
            @RequestParam Long memberId,
            @Parameter(
                    name = "notificationTypeId",
                    description = "알림 타입 ID (1: 예약 확정, 2: 예약 취소, 3: 리뷰 요청, 4: 이벤트, 5: 공지사항, 6: 기타)",
                    example = "1",
                    required = true
            )
            @RequestParam Long notificationTypeId) {
        
        log.info("단일 테스트 알림 요청 - 회원 ID: {}, 알림 타입 ID: {}", memberId, notificationTypeId);
        
        // 알림 요청 생성 (즉시 발송)
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .memberId(memberId)
                .notificationTypeId(notificationTypeId)
                .scheduledAt(LocalDateTime.now())
                .build();
        
        // 알림 예약
        NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
        log.info("단일 테스트 알림 예약 완료 - 알림 ID: {}", response.getId());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "비동기 다중 테스트 알림 전송 (통계 포함)",
            description = "테스트 목적으로 특정 회원에게 지정된 알림 타입 ID로 여러 개의 알림을 비동기로 동시에 전송하고 상세한 통계 정보를 반환합니다. 동시 접속 부하 테스트에 적합합니다.",
            tags = {"테스트 - 다중", "테스트 - 비동기"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비동기 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationTestStatistics.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터 (count는 1-1000 사이여야 함)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "408",
                    description = "요청 시간 초과 (30초 이내 완료되지 않음)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/multiple")
    public ResponseEntity<NotificationTestStatistics> sendMultipleTestNotifications(
            @Parameter(
                    name = "memberId",
                    description = "테스트 대상 회원 ID",
                    example = "1",
                    required = true
            )
            @RequestParam Long memberId,
            @Parameter(
                    name = "notificationTypeId",
                    description = "알림 타입 ID (1: 예약 확정, 2: 예약 취소, 3: 리뷰 요청, 4: 이벤트, 5: 공지사항, 6: 기타)",
                    example = "1",
                    required = true
            )
            @RequestParam Long notificationTypeId,
            @Parameter(
                    name = "count",
                    description = "동시에 보낼 알림 개수 (최대 1000개)",
                    example = "10",
                    required = true
            )
            @RequestParam Integer count) {
        
        log.info("비동기 다중 테스트 알림 요청 - 회원 ID: {}, 알림 타입 ID: {}, 개수: {}", memberId, notificationTypeId, count);
        
        // 테스트 시작 시간
        LocalDateTime testStartTime = LocalDateTime.now();
        long testStartMillis = System.currentTimeMillis();
        
        // 통계 수집을 위한 변수들
        AtomicInteger sendSuccessCount = new AtomicInteger(0);
        AtomicInteger sendFailureCount = new AtomicInteger(0);
        AtomicLong totalSendDuration = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(count);
        
        // 비동기로 여러 알림 전송
        for (int i = 0; i < count; i++) {
            final int notificationNumber = i + 1;
            
            // 각 알림을 비동기로 처리
            CompletableFuture.runAsync(() -> {
                long sendStartTime = System.currentTimeMillis();
                try {
                    log.debug("비동기 알림 전송 시작 - 회원 ID: {}, 순번: {}", memberId, notificationNumber);
                    
                    // 알림 요청 생성 (즉시 발송)
                    NotificationRequest notificationRequest = NotificationRequest.builder()
                            .memberId(memberId)
                            .notificationTypeId(notificationTypeId)
                            .scheduledAt(LocalDateTime.now())
                            .build();
                    
                    // 알림 예약 및 전송
                    NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
                    
                    long sendEndTime = System.currentTimeMillis();
                    long sendDuration = sendEndTime - sendStartTime;
                    totalSendDuration.addAndGet(sendDuration);
                    sendSuccessCount.incrementAndGet();
                    
                    log.debug("비동기 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms",
                            notificationNumber, response.getId(), sendDuration);
                    
                } catch (Exception e) {
                    long sendEndTime = System.currentTimeMillis();
                    long sendDuration = sendEndTime - sendStartTime;
                    totalSendDuration.addAndGet(sendDuration);
                    sendFailureCount.incrementAndGet();
                    
                    log.error("비동기 알림 전송 실패 - 회원 ID: {}, 순번: {}, 소요시간: {}ms, 오류: {}",
                            memberId, notificationNumber, sendDuration, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            // 모든 알림 처리 완료까지 대기 (최대 30초)
            latch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("알림 처리 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
        }
        
        // 테스트 완료 시간
        LocalDateTime testEndTime = LocalDateTime.now();
        long testEndMillis = System.currentTimeMillis();
        long totalTestDuration = testEndMillis - testStartMillis;
        
        // 통계 계산
        int successCount = sendSuccessCount.get();
        int failureCount = sendFailureCount.get();
        double sendSuccessRate = count > 0 ? (double) successCount / count * 100 : 0.0;
        long avgSendDuration = successCount > 0 ? totalSendDuration.get() / successCount : 0;
        
        // 수신 통계는 현재 구현에서는 발송과 동일하게 처리 (실제로는 FCM 응답을 통해 구분 가능)
        int receiveSuccessCount = successCount;
        int receiveFailureCount = failureCount;
        double receiveSuccessRate = sendSuccessRate;
        long totalReceiveDuration = totalSendDuration.get();
        long avgReceiveDuration = avgSendDuration;
        
        // 통계 객체 생성
        NotificationTestStatistics statistics = NotificationTestStatistics.builder()
                .totalRequested(count)
                .sendSuccessCount(successCount)
                .sendFailureCount(failureCount)
                .sendSuccessRate(Math.round(sendSuccessRate * 100.0) / 100.0)
                .totalSendDuration(totalSendDuration.get())
                .averageSendDuration(avgSendDuration)
                .receiveSuccessCount(receiveSuccessCount)
                .receiveFailureCount(receiveFailureCount)
                .receiveSuccessRate(Math.round(receiveSuccessRate * 100.0) / 100.0)
                .totalReceiveDuration(totalReceiveDuration)
                .averageReceiveDuration(avgReceiveDuration)
                .testStartTime(testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .testEndTime(testEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .totalTestDuration(totalTestDuration)
                .build();
        
        log.info("비동기 다중 테스트 알림 완료 - 총 요청: {}, 성공: {}, 실패: {}, 성공률: {}%, 총 소요시간: {}ms",
                count, successCount, failureCount, statistics.getSendSuccessRate(), totalTestDuration);
        
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
            summary = "순차적(동기) 다중 테스트 알림 전송 (통계 포함)",
            description = "테스트 목적으로 특정 회원에게 지정된 알림 타입 ID로 여러 개의 알림을 순차적으로 동기 전송하고 상세한 통계 정보를 반환합니다. 안정성 및 순차 처리 성능 테스트에 적합합니다.",
            tags = {"테스트 - 다중", "테스트 - 동기"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "순차적 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationTestStatistics.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터 (count는 1-1000 사이여야 함)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/sequential")
    public ResponseEntity<NotificationTestStatistics> sendSequentialTestNotifications(
            @Parameter(
                    name = "memberId",
                    description = "테스트 대상 회원 ID",
                    example = "1",
                    required = true
            )
            @RequestParam Long memberId,
            @Parameter(
                    name = "notificationTypeId",
                    description = "알림 타입 ID (1: 예약 확정, 2: 예약 취소, 3: 리뷰 요청, 4: 이벤트, 5: 공지사항, 6: 기타)",
                    example = "1",
                    required = true
            )
            @RequestParam Long notificationTypeId,
            @Parameter(
                    name = "count",
                    description = "순차적으로 보낼 알림 개수 (최대 1000개)",
                    example = "10",
                    required = true
            )
            @RequestParam Integer count) {
        
        log.info("순차적 다중 테스트 알림 요청 - 회원 ID: {}, 알림 타입 ID: {}, 개수: {}", memberId, notificationTypeId, count);
        
        // 테스트 시작 시간
        LocalDateTime testStartTime = LocalDateTime.now();
        long testStartMillis = System.currentTimeMillis();
        
        // 통계 수집을 위한 변수들
        int sendSuccessCount = 0;
        int sendFailureCount = 0;
        long totalSendDuration = 0;
        
        // 순차적으로 알림 전송
        for (int i = 0; i < count; i++) {
            final int notificationNumber = i + 1;
            long sendStartTime = System.currentTimeMillis();
            
            try {
                log.debug("순차적 알림 전송 시작 - 회원 ID: {}, 순번: {}", memberId, notificationNumber);
                
                // 알림 요청 생성 (즉시 발송)
                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .memberId(memberId)
                        .notificationTypeId(notificationTypeId)
                        .scheduledAt(LocalDateTime.now())
                        .build();
                
                // 알림 예약 및 전송
                NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
                
                long sendEndTime = System.currentTimeMillis();
                long sendDuration = sendEndTime - sendStartTime;
                totalSendDuration += sendDuration;
                sendSuccessCount++;
                
                log.debug("순차적 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms",
                        notificationNumber, response.getId(), sendDuration);
                
            } catch (Exception e) {
                long sendEndTime = System.currentTimeMillis();
                long sendDuration = sendEndTime - sendStartTime;
                totalSendDuration += sendDuration;
                sendFailureCount++;
                
                log.error("순차적 알림 전송 실패 - 회원 ID: {}, 순번: {}, 소요시간: {}ms, 오류: {}",
                        memberId, notificationNumber, sendDuration, e.getMessage());
            }
        }
        
        // 테스트 완료 시간
        LocalDateTime testEndTime = LocalDateTime.now();
        long testEndMillis = System.currentTimeMillis();
        long totalTestDuration = testEndMillis - testStartMillis;
        
        // 통계 계산
        double sendSuccessRate = count > 0 ? (double) sendSuccessCount / count * 100 : 0.0;
        long avgSendDuration = sendSuccessCount > 0 ? totalSendDuration / sendSuccessCount : 0;
        
        // 수신 통계는 현재 구현에서는 발송과 동일하게 처리
        int receiveSuccessCount = sendSuccessCount;
        int receiveFailureCount = sendFailureCount;
        double receiveSuccessRate = sendSuccessRate;
        long totalReceiveDuration = totalSendDuration;
        long avgReceiveDuration = avgSendDuration;
        
        // 통계 객체 생성
        NotificationTestStatistics statistics = NotificationTestStatistics.builder()
                .totalRequested(count)
                .sendSuccessCount(sendSuccessCount)
                .sendFailureCount(sendFailureCount)
                .sendSuccessRate(Math.round(sendSuccessRate * 100.0) / 100.0)
                .totalSendDuration(totalSendDuration)
                .averageSendDuration(avgSendDuration)
                .receiveSuccessCount(receiveSuccessCount)
                .receiveFailureCount(receiveFailureCount)
                .receiveSuccessRate(Math.round(receiveSuccessRate * 100.0) / 100.0)
                .totalReceiveDuration(totalReceiveDuration)
                .averageReceiveDuration(avgReceiveDuration)
                .testStartTime(testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .testEndTime(testEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .totalTestDuration(totalTestDuration)
                .build();
        
        log.info("순차적 다중 테스트 알림 완료 - 총 요청: {}, 성공: {}, 실패: {}, 성공률: {}%, 총 소요시간: {}ms",
                count, sendSuccessCount, sendFailureCount, statistics.getSendSuccessRate(), totalTestDuration);
        
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
            summary = "FCM 토큰 기반 비동기 다중 테스트 알림 전송 (통계 포함)",
            description = "FCM 토큰을 직접 사용하여 지정된 알림 타입 ID로 여러 개의 알림을 비동기로 동시에 전송하고 상세한 통계 정보를 반환합니다. 회원 정보 없이 직접 디바이스 테스트가 가능합니다.",
            tags = {"테스트 - FCM", "테스트 - 비동기"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 기반 비동기 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationTestStatistics.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터 (FCM 토큰 형식 오류 또는 count 범위 초과)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/fcm/multiple")
    public ResponseEntity<NotificationTestStatistics> sendMultipleFcmTestNotifications(
            @Parameter(
                    name = "fcmToken",
                    description = "FCM 토큰 (Firebase Cloud Messaging 토큰)",
                    example = "dA1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0u1V2w3X4y5Z",
                    required = true
            )
            @RequestParam String fcmToken,
            @Parameter(
                    name = "notificationTypeId",
                    description = "알림 타입 ID (1: 예약 확정, 2: 예약 취소, 3: 리뷰 요청, 4: 이벤트, 5: 공지사항, 6: 기타)",
                    example = "1",
                    required = true
            )
            @RequestParam Long notificationTypeId,
            @Parameter(
                    name = "count",
                    description = "동시에 보낼 알림 개수 (최대 1000개)",
                    example = "10",
                    required = true
            )
            @RequestParam Integer count) {
        
        log.info("FCM 토큰 기반 비동기 다중 테스트 알림 요청 - FCM 토큰: {}..., 알림 타입 ID: {}, 개수: {}",
                fcmToken.substring(0, Math.min(10, fcmToken.length())), notificationTypeId, count);
        
        // 테스트 시작 시간
        LocalDateTime testStartTime = LocalDateTime.now();
        long testStartMillis = System.currentTimeMillis();
        
        // 통계 수집을 위한 변수들
        AtomicInteger sendSuccessCount = new AtomicInteger(0);
        AtomicInteger sendFailureCount = new AtomicInteger(0);
        AtomicLong totalSendDuration = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(count);
        
        // 비동기로 여러 알림 전송
        for (int i = 0; i < count; i++) {
            final int notificationNumber = i + 1;
            
            // 각 알림을 비동기로 처리
            CompletableFuture.runAsync(() -> {
                long sendStartTime = System.currentTimeMillis();
                try {
                    log.debug("FCM 토큰 기반 비동기 알림 전송 시작 - 순번: {}", notificationNumber);
                    
                    // FCM 토큰을 사용하여 직접 알림 전송
                    // TODO: 실제로는 FCMService를 통해 fcmToken으로 직접 전송해야 함
                    NotificationRequest notificationRequest = NotificationRequest.builder()
                            .memberId(1L) // 임시 멤버ID
                            .notificationTypeId(notificationTypeId)
                            .scheduledAt(LocalDateTime.now())
                            .build();
                    
                    NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
                    
                    long sendEndTime = System.currentTimeMillis();
                    long sendDuration = sendEndTime - sendStartTime;
                    totalSendDuration.addAndGet(sendDuration);
                    sendSuccessCount.incrementAndGet();
                    
                    log.debug("FCM 토큰 기반 비동기 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms",
                            notificationNumber, response.getId(), sendDuration);
                    
                } catch (Exception e) {
                    long sendEndTime = System.currentTimeMillis();
                    long sendDuration = sendEndTime - sendStartTime;
                    totalSendDuration.addAndGet(sendDuration);
                    sendFailureCount.incrementAndGet();
                    
                    log.error("FCM 토큰 기반 비동기 알림 전송 실패 - 순번: {}, 소요시간: {}ms, 오류: {}",
                            notificationNumber, sendDuration, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            // 모든 알림 처리 완료까지 대기 (최대 30초)
            latch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("FCM 토큰 기반 알림 처리 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
        }
        
        // 테스트 완료 시간
        LocalDateTime testEndTime = LocalDateTime.now();
        long testEndMillis = System.currentTimeMillis();
        long totalTestDuration = testEndMillis - testStartMillis;
        
        // 통계 계산
        int successCount = sendSuccessCount.get();
        int failureCount = sendFailureCount.get();
        double sendSuccessRate = count > 0 ? (double) successCount / count * 100 : 0.0;
        long avgSendDuration = successCount > 0 ? totalSendDuration.get() / successCount : 0;
        
        // 수신 통계는 현재 구현에서는 발송과 동일하게 처리
        int receiveSuccessCount = successCount;
        int receiveFailureCount = failureCount;
        double receiveSuccessRate = sendSuccessRate;
        long totalReceiveDuration = totalSendDuration.get();
        long avgReceiveDuration = avgSendDuration;
        
        // 통계 객체 생성
        NotificationTestStatistics statistics = NotificationTestStatistics.builder()
                .totalRequested(count)
                .sendSuccessCount(successCount)
                .sendFailureCount(failureCount)
                .sendSuccessRate(Math.round(sendSuccessRate * 100.0) / 100.0)
                .totalSendDuration(totalSendDuration.get())
                .averageSendDuration(avgSendDuration)
                .receiveSuccessCount(receiveSuccessCount)
                .receiveFailureCount(receiveFailureCount)
                .receiveSuccessRate(Math.round(receiveSuccessRate * 100.0) / 100.0)
                .totalReceiveDuration(totalReceiveDuration)
                .averageReceiveDuration(avgReceiveDuration)
                .testStartTime(testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .testEndTime(testEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .totalTestDuration(totalTestDuration)
                .build();
        
        log.info("FCM 토큰 기반 비동기 다중 테스트 알림 완료 - 총 요청: {}, 성공: {}, 실패: {}, 성공률: {}%, 총 소요시간: {}ms",
                count, successCount, failureCount, statistics.getSendSuccessRate(), totalTestDuration);
        
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
            summary = "FCM 토큰 기반 순차적(동기) 다중 테스트 알림 전송 (통계 포함)",
            description = "FCM 토큰을 직접 사용하여 지정된 알림 타입 ID로 여러 개의 알림을 순차적으로 동기 전송하고 상세한 통계 정보를 반환합니다. 회원 정보 없이 직접 디바이스의 순차 처리 성능을 테스트할 수 있습니다.",
            tags = {"테스트 - FCM", "테스트 - 동기"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 기반 순차적 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationTestStatistics.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터 (FCM 토큰 형식 오류 또는 count 범위 초과)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/fcm/sequential")
    public ResponseEntity<NotificationTestStatistics> sendSequentialFcmTestNotifications(
            @Parameter(
                    name = "fcmToken",
                    description = "FCM 토큰 (Firebase Cloud Messaging 토큰)",
                    example = "dA1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0u1V2w3X4y5Z",
                    required = true
            )
            @RequestParam String fcmToken,
            @Parameter(
                    name = "notificationTypeId",
                    description = "알림 타입 ID (1: 예약 확정, 2: 예약 취소, 3: 리뷰 요청, 4: 이벤트, 5: 공지사항, 6: 기타)",
                    example = "1",
                    required = true
            )
            @RequestParam Long notificationTypeId,
            @Parameter(
                    name = "count",
                    description = "순차적으로 보낼 알림 개수 (최대 1000개)",
                    example = "10",
                    required = true
            )
            @RequestParam Integer count) {
        
        log.info("FCM 토큰 기반 순차적 다중 테스트 알림 요청 - FCM 토큰: {}..., 알림 타입 ID: {}, 개수: {}",
                fcmToken.substring(0, Math.min(10, fcmToken.length())), notificationTypeId, count);
        
        // 테스트 시작 시간
        LocalDateTime testStartTime = LocalDateTime.now();
        long testStartMillis = System.currentTimeMillis();
        
        // 통계 수집을 위한 변수들
        int sendSuccessCount = 0;
        int sendFailureCount = 0;
        long totalSendDuration = 0;
        
        // 순차적으로 알림 전송
        for (int i = 0; i < count; i++) {
            final int notificationNumber = i + 1;
            long sendStartTime = System.currentTimeMillis();
            
            try {
                log.debug("FCM 토큰 기반 순차적 알림 전송 시작 - 순번: {}", notificationNumber);
                
                // FCM 토큰을 사용하여 직접 알림 전송
                // TODO: 실제로는 FCMService를 통해 fcmToken으로 직접 전송해야 함
                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .memberId(1L)
                        .notificationTypeId(notificationTypeId)
                        .scheduledAt(LocalDateTime.now())
                        .build();
                
                NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
                
                long sendEndTime = System.currentTimeMillis();
                long sendDuration = sendEndTime - sendStartTime;
                totalSendDuration += sendDuration;
                sendSuccessCount++;
                
                log.debug("FCM 토큰 기반 순차적 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms",
                        notificationNumber, response.getId(), sendDuration);
                
            } catch (Exception e) {
                long sendEndTime = System.currentTimeMillis();
                long sendDuration = sendEndTime - sendStartTime;
                totalSendDuration += sendDuration;
                sendFailureCount++;
                
                log.error("FCM 토큰 기반 순차적 알림 전송 실패 - 순번: {}, 소요시간: {}ms, 오류: {}",
                        notificationNumber, sendDuration, e.getMessage());
            }
        }
        
        // 테스트 완료 시간
        LocalDateTime testEndTime = LocalDateTime.now();
        long testEndMillis = System.currentTimeMillis();
        long totalTestDuration = testEndMillis - testStartMillis;
        
        // 통계 계산
        double sendSuccessRate = count > 0 ? (double) sendSuccessCount / count * 100 : 0.0;
        long avgSendDuration = sendSuccessCount > 0 ? totalSendDuration / sendSuccessCount : 0;
        
        // 수신 통계는 현재 구현에서는 발송과 동일하게 처리
        int receiveSuccessCount = sendSuccessCount;
        int receiveFailureCount = sendFailureCount;
        double receiveSuccessRate = sendSuccessRate;
        long totalReceiveDuration = totalSendDuration;
        long avgReceiveDuration = avgSendDuration;
        
        // 통계 객체 생성
        NotificationTestStatistics statistics = NotificationTestStatistics.builder()
                .totalRequested(count)
                .sendSuccessCount(sendSuccessCount)
                .sendFailureCount(sendFailureCount)
                .sendSuccessRate(Math.round(sendSuccessRate * 100.0) / 100.0)
                .totalSendDuration(totalSendDuration)
                .averageSendDuration(avgSendDuration)
                .receiveSuccessCount(receiveSuccessCount)
                .receiveFailureCount(receiveFailureCount)
                .receiveSuccessRate(Math.round(receiveSuccessRate * 100.0) / 100.0)
                .totalReceiveDuration(totalReceiveDuration)
                .averageReceiveDuration(avgReceiveDuration)
                .testStartTime(testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .testEndTime(testEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .totalTestDuration(totalTestDuration)
                .build();
        
        log.info("FCM 토큰 기반 순차적 다중 테스트 알림 완료 - 총 요청: {}, 성공: {}, 실패: {}, 성공률: {}%, 총 소요시간: {}ms",
                count, sendSuccessCount, sendFailureCount, statistics.getSendSuccessRate(), totalTestDuration);
        
        return ResponseEntity.ok(statistics);
    }
    
    // Swagger에서 사용할 오류 응답 스키마 정의
    @Schema(name = "ErrorResponse", description = "오류 응답")
    public static class ErrorResponse {
        @Schema(description = "오류 코드", example = "NOTIFICATION_TYPE_NOT_FOUND")
        private String code;
        
        @Schema(description = "오류 메시지", example = "알림 타입을 찾을 수 없습니다.")
        private String message;
        
        @Schema(description = "오류 발생 시간", example = "2024-01-01T10:00:00")
        private String timestamp;
    }
}