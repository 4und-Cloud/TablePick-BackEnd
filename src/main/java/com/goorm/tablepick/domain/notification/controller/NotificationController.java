package com.goorm.tablepick.domain.notification.controller;

import com.goorm.tablepick.domain.notification.dto.request.FCMTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.dto.response.NotificationTestStatistics;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.notification.service.FCMTokenService;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import com.goorm.tablepick.global.exception.NotificationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "알림 예약, 조회 및 FCM 토큰 관리를 위한 API")
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;
    private final FCMTokenService fcmTokenService;
    private final NotificationTypesRepository notificationTypesRepository;
    
    @Operation(
            summary = "알림 예약",
            description = "새로운 알림을 예약합니다. 회원ID, 알림타입ID, 예약ID, 예약시간을 포함해야 합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 예약 성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/schedule")
    public ResponseEntity<NotificationResponse> scheduleNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "알림 예약 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationRequest.class))
            )
            @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.scheduleNotification(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "알림 상태 조회",
            description = "특정 알림의 상태를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationStatus(
            @Parameter(
                    name = "id",
                    description = "알림 ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id) {
        try {
            NotificationResponse response = notificationService.getNotificationStatus(id);
            return ResponseEntity.ok(response);
        } catch (NotificationException e) {
            // 알림이 없을 때 예외가 발생하면 404 반환
            return ResponseEntity.notFound().build();
        }
    }
    
    
    @Operation(
            summary = "회원 알림 목록 조회",
            description = "특정 회원의 알림 목록을 조회합니다. 선택적으로 상태 필터링이 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 알림 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<NotificationResponse>> getMemberNotifications(
            @Parameter(
                    name = "memberId",
                    description = "회원 ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long memberId,
            @Parameter(
                    name = "status",
                    description = "알림 상태 필터 (예: PENDING, SENT, FAILED)",
                    required = false,
                    example = "SENT",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) String status) {
        List<NotificationResponse> notifications = notificationService.getMemberNotifications(memberId, status);
        return ResponseEntity.ok(notifications);
    }
    
    @Operation(
            summary = "FCM 토큰 업데이트",
            description = "회원의 FCM 토큰을 부분 업데이트합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 업데이트 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @Parameter(
                    name = "memberId",
                    description = "회원 ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.QUERY
            )
            @RequestParam Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "FCM 토큰 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FCMTokenRequest.class))
            )
            @RequestBody FCMTokenRequest request) {
        fcmTokenService.updateFcmToken(memberId, request.getToken());
        return ResponseEntity.ok().build();
    }
    
    @Operation(
            summary = "FCM 토큰 NULL로 변경",
            description = "회원의 FCM 토큰을 NULL로 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 NULL로 변경 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/fcm-token/remove")
    public ResponseEntity<Void> removeFcmToken(@RequestParam Long memberId) {
        fcmTokenService.updateFcmTokenToNull(memberId);
        return ResponseEntity.ok().build();
    }
    
    @Operation(
            summary = "테스트용 알림 전송",
            description = "테스트 목적으로 특정 회원에게 지정된 알림 타입 ID로 알림을 즉시 전송합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "테스트 알림 전송 성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/test")
    public ResponseEntity<NotificationResponse> sendTestNotification(
            @Parameter(description = "회원 ID", example = "1", required = true)
            @RequestParam Long memberId,
            @Parameter(description = "알림 타입 ID (1~6)", example = "1", required = true)
            @RequestParam Long notificationTypeId) {
        
        log.info("테스트 알림 요청 - 회원 ID: {}, 알림 타입 ID: {}", memberId, notificationTypeId);
        
        // 알림 요청 생성 (10초 후 발송)
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .memberId(memberId)
                .notificationTypeId(notificationTypeId)
                .scheduledAt(LocalDateTime.now())
                .build();
        
        // 알림 예약
        NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
        log.info("테스트 알림 예약 완료 - 알림 ID: {}", response.getId());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "비동기 다중 테스트 알림 전송 (통계 포함)",
            description = "테스트 목적으로 특정 회원에게 지정된 알림 타입 ID로 여러 개의 알림을 비동기로 동시에 전송하고 상세한 통계 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비동기 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(schema = @Schema(implementation = NotificationTestStatistics.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/test/multiple")
    public ResponseEntity<NotificationTestStatistics> sendMultipleTestNotifications(
            @Parameter(description = "회원 ID", example = "1", required = true)
            @RequestParam Long memberId,
            @Parameter(description = "알림 타입 ID (1~6)", example = "1", required = true)
            @RequestParam Long notificationTypeId,
            @Parameter(description = "동시에 보낼 알림 개수", example = "5", required = true)
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
                    log.info("비동기 알림 전송 시작 - 회원 ID: {}, 순번: {}", memberId, notificationNumber);
                    
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
                    
                    log.info("비동기 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms", 
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
        // 실제 환경에서는 FCM 응답을 통해 수신 성공/실패를 구분해야 함
        int receiveSuccessCount = successCount; // 임시로 발송 성공과 동일하게 처리
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
            description = "테스트 목적으로 특정 회원에게 지정된 알림 타입 ID로 여러 개의 알림을 순차적으로 동기 전송하고 상세한 통계 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "순차적 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(schema = @Schema(implementation = NotificationTestStatistics.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/test/sequential")
    public ResponseEntity<NotificationTestStatistics> sendSequentialTestNotifications(
            @Parameter(description = "회원 ID", example = "1", required = true)
            @RequestParam Long memberId,
            @Parameter(description = "알림 타입 ID (1~6)", example = "1", required = true)
            @RequestParam Long notificationTypeId,
            @Parameter(description = "순차적으로 보낼 알림 개수", example = "10", required = true)
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
                log.info("순차적 알림 전송 시작 - 회원 ID: {}, 순번: {}", memberId, notificationNumber);
                
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
                
                log.info("순차적 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms", 
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
        
        // 수신 통계는 현재 구현에서는 발송과 동일하게 처리 (실제로는 FCM 응답을 통해 구분 가능)
        // 실제 환경에서는 FCM 응답을 통해 수신 성공/실패를 구분해야 함
        int receiveSuccessCount = sendSuccessCount; // 임시로 발송 성공과 동일하게 처리
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
            description = "FCM 토큰을 직접 사용하여 지정된 알림 타입 ID로 여러 개의 알림을 비동기로 동시에 전송하고 상세한 통계 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 기반 비동기 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(schema = @Schema(implementation = NotificationTestStatistics.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/test/fcm/multiple")
    public ResponseEntity<NotificationTestStatistics> sendMultipleFcmTestNotifications(
            @Parameter(description = "FCM 토큰", example = "dA1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0u1V2w3X4y5Z", required = true)
            @RequestParam String fcmToken,
            @Parameter(description = "알림 타입 ID (1~6)", example = "1", required = true)
            @RequestParam Long notificationTypeId,
            @Parameter(description = "동시에 보낼 알림 개수", example = "5", required = true)
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
                    log.info("FCM 토큰 기반 비동기 알림 전송 시작 - 순번: {}", notificationNumber);
                    
                    // FCM 토큰을 사용하여 직접 알림 전송 (NotificationService 대신 FCMService 사용)
                    // 실제 구현에서는 FCMService를 통해 직접 전송해야 함
                    // 여기서는 예시로 NotificationRequest를 생성하되, memberId는 null로 처리
                    NotificationRequest notificationRequest = NotificationRequest.builder()
                            .memberId(null) // FCM 토큰 직접 사용 시 memberId는 null
                            .notificationTypeId(notificationTypeId)
                            .scheduledAt(LocalDateTime.now())
                            .build();
                    
                    // TODO: 실제로는 FCMService를 통해 fcmToken으로 직접 전송해야 함
                    // 현재는 NotificationService를 통한 예약 방식으로 처리
                    NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
                    
                    long sendEndTime = System.currentTimeMillis();
                    long sendDuration = sendEndTime - sendStartTime;
                    totalSendDuration.addAndGet(sendDuration);
                    sendSuccessCount.incrementAndGet();
                    
                    log.info("FCM 토큰 기반 비동기 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms", 
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
            description = "FCM 토큰을 직접 사용하여 지정된 알림 타입 ID로 여러 개의 알림을 순차적으로 동기 전송하고 상세한 통계 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 기반 순차적 다중 테스트 알림 전송 완료 및 통계 정보 반환",
                    content = @Content(schema = @Schema(implementation = NotificationTestStatistics.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "알림 타입을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/test/fcm/sequential")
    public ResponseEntity<NotificationTestStatistics> sendSequentialFcmTestNotifications(
            @Parameter(description = "FCM 토큰", example = "dA1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0u1V2w3X4y5Z", required = true)
            @RequestParam String fcmToken,
            @Parameter(description = "알림 타입 ID (1~6)", example = "1", required = true)
            @RequestParam Long notificationTypeId,
            @Parameter(description = "순차적으로 보낼 알림 개수", example = "10", required = true)
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
                log.info("FCM 토큰 기반 순차적 알림 전송 시작 - 순번: {}", notificationNumber);
                
                // FCM 토큰을 사용하여 직접 알림 전송 (NotificationService 대신 FCMService 사용)
                // 실제 구현에서는 FCMService를 통해 직접 전송해야 함
                // 여기서는 예시로 NotificationRequest를 생성하되, memberId는 null로 처리
                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .memberId(null) // FCM 토큰 직접 사용 시 memberId는 null
                        .notificationTypeId(notificationTypeId)
                        .scheduledAt(LocalDateTime.now())
                        .build();
                
                // TODO: 실제로는 FCMService를 통해 fcmToken으로 직접 전송해야 함
                // 현재는 NotificationService를 통한 예약 방식으로 처리
                NotificationResponse response = notificationService.scheduleNotification(notificationRequest);
                
                long sendEndTime = System.currentTimeMillis();
                long sendDuration = sendEndTime - sendStartTime;
                totalSendDuration += sendDuration;
                sendSuccessCount++;
                
                log.info("FCM 토큰 기반 순차적 알림 예약 완료 - 순번: {}, 알림 ID: {}, 소요시간: {}ms", 
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
    
    @Operation(
            summary = "알림 타입 목록 조회",
            description = "모든 알림 타입 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 타입 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationTypes.class)))
            )
    })
    @GetMapping("/notification-types")
    public ResponseEntity<List<NotificationTypes>> getNotificationTypes() {
        log.info("알림 타입 목록 조회 요청");
        List<NotificationTypes> notificationTypes = notificationTypesRepository.findAll();
        log.info("알림 타입 목록 조회 완료 - 총 {} 개", notificationTypes.size());
        return ResponseEntity.ok(notificationTypes);
    }
    
    
    // Swagger에서 사용할 오류 응답 스키마 정의
    @Schema(name = "ErrorResponse", description = "오류 응답")
    private static class ErrorResponse {
        @Schema(description = "오류 메시지", example = "Member not found")
        private String message;
        
        @Schema(description = "오류 코드", example = "MEMBER_NOT_FOUND")
        private String code;
    }
}
