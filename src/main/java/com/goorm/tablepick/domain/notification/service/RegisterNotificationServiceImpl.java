package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterNotificationServiceImpl implements RegisterNotificationService {

    private final NotificationService notificationService;
    private final NotificationTypesRepository notificationTypesRepository;
    private final FCMTokenService fcmTokenService;
    
    // 최대 재시도 횟수
    private static final int MAX_RETRY_COUNT = 5;
    
    // 재시도 간격 (초)
    private static final int RETRY_DELAY_SECONDS = 1;

    // 회원가입 축하 알림
    @Override
    @Transactional
    public void sendWelcomeNotification(Member member) {
        // 회원의 생성 시간과 현재 시간의 차이 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = member.getCreatedAt();

        // 생성 시간이 null이면 처리하지 않음
        if (createdAt == null) {
            log.warn("Member createdAt is null for member ID: {}", member.getId());
            return;
        }

        // 생성 시간과 현재 시간의 차이가 1분 이내인지 확인
        Duration duration = Duration.between(createdAt, now);
        if (duration.toMinutes() >= 1) {
            log.info("Member is not new (created {} minutes ago), skipping welcome notification", duration.toMinutes());
            return;
        }

        // 회원가입 축하 알림 타입 조회
        NotificationTypes notificationType = notificationTypesRepository.findByType(NotificationTypes.REGISTER_COMPLETED)
                .orElseThrow(() -> new RuntimeException("Register completed notification type not found"));

        // FCM 토큰 확인 및 알림 예약 시도
        tryScheduleWelcomeNotification(member.getId(), notificationType.getId(), 0);
    }

    // FCM 토큰을 확인하고 알림 예약을 시도하는 메서드
    // 토큰이 없으면 최대 5번까지 1초 간격으로 재시도
    @Async
    public void tryScheduleWelcomeNotification(Long memberId, Long notificationTypeId, int retryCount) {
        log.info("Trying to schedule welcome notification for member ID: {}, retry count: {}", memberId, retryCount);
        
        // FCM 토큰 확인
        String fcmToken = fcmTokenService.getFcmToken(memberId);
        
        if (fcmToken != null && !fcmToken.trim().isEmpty()) {
            // FCM 토큰이 있으면 알림 예약
            scheduleWelcomeNotification(memberId, notificationTypeId);
            log.info("Welcome notification scheduled for member ID: {} with FCM token", memberId);
        } else {
            // 최대 재시도 횟수를 초과하지 않았으면 재시도
            if (retryCount < MAX_RETRY_COUNT) {
                log.info("FCM token not found for member ID: {}, will retry in {} seconds (retry {}/{})", 
                        memberId, RETRY_DELAY_SECONDS, retryCount + 1, MAX_RETRY_COUNT);
                
                // 1초 후에 재시도
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.schedule(() -> {
                    tryScheduleWelcomeNotification(memberId, notificationTypeId, retryCount + 1);
                }, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
                executor.shutdown();
            } else {
                log.warn("Failed to schedule welcome notification for member ID: {} after {} retries. No FCM token found.", 
                        memberId, MAX_RETRY_COUNT);
            }
        }
    }
    
    // 회원가입 축하 알림 예약
    @Transactional
    public void scheduleWelcomeNotification(Long memberId, Long notificationTypeId) {
        // 알림 요청 생성 (즉시 발송)
        NotificationRequest request = NotificationRequest.builder()
                .memberId(memberId)
                .notificationTypeId(notificationTypeId)
                .scheduledAt(LocalDateTime.now()) // 즉시 발송으로 변경
                .build();

        // 알림 예약
        notificationService.scheduleNotification(request);
        log.info("Welcome notification scheduled for member ID: {}", memberId);
    }
}
