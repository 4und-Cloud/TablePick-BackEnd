package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationLog;
import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationLogRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
// 알림 관리 담당 서비스
public class NotificationService {
    private final NotificationQueueRepository notificationQueueRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final FCMService fcmService;
    private final FCMTokenService fcmTokenService;
    private final ReservationRepository reservationRepository;

    // 최대 재시도 횟수 = 3번
    private static final int MAX_RETRY_COUNT = 3;

    // 재시도 간격 (분) = 5분
    private static final int RETRY_DELAY_MINUTES = 5;
    private final NotificationTypesRepository notificationTypesRepository;


    // 알림 예약
    // 지정된 시간에 알림이 전송되도록 큐에 등록
    public NotificationResponse scheduleNotification(NotificationRequest request) {
        NotificationTypes type = notificationTypesRepository.findById(request.getNotificationTypeId())
                .orElseThrow(() -> new NotificationException("Notification type not found", "TYPE_NOT_FOUND"));

        NotificationQueue queue = NotificationQueue.builder()
                .notificationTypes(type)
                .memberId(request.getMemberId())
                .reservationId(request.getReservationId())
                .scheduledAt(request.getScheduledAt())
                .status(NotificationStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();

        NotificationQueue savedQueue = notificationQueueRepository.save(queue);
        return createNotificationResponse(savedQueue);
    }

    // 예약된 알림 처리
    // 1분마다 실행되며 현재 시간 이전에 예약된 PENDING 상태의 알림을 처리
//    @Scheduled(fixedRate = 60000)
//    public void processNotificationQueue() {
//        List<NotificationQueue> pendingNotifications = notificationQueueRepository
//                .findByStatusAndScheduledAtBefore(NotificationStatus.PENDING.name(), LocalDateTime.now());
//
//        for (NotificationQueue notification : pendingNotifications) {
//            try {
//                processNotification(notification);
//            } catch (Exception e) {
//                log.error("Failed to process notification: {}", e.getMessage());
//                handleNotificationError(notification, e);
//            }
//        }
//    }

    // 개별 알림 처리
    // 회원의 FCM토큰 조회하고 알림 전송, 성공시 알림 상태 SENT로 업데이트하고 로그 기록
//    private void processNotification(NotificationQueue notification) {
//        try {
//            String fcmToken = fcmTokenService.getFcmToken(notification.getMemberId());
//            sendFcmNotification(notification, fcmToken);
//            updateNotificationStatus(notification, NotificationStatus.SENT);
//            saveNotificationLog(notification, true, null);
//        } catch (NotificationException e) {
//            log.error("Notification error: {}", e.getMessage());
//            handleNotificationError(notification, e);
//        }
//    }

    // FCM 메시지 전송
    private void sendFcmNotification(NotificationQueue notification, String fcmToken) {
        NotificationTypes type = notification.getNotificationTypes();

        // 예약 정보 조회
        Reservation reservation = reservationRepository.getReservationById(notification.getReservationId());

        // 파라미터 맵 생성
        Map<String, String> parameters = new HashMap<>();
        parameters.put("id", notification.getReservationId().toString());
        parameters.put("restaurantName", reservation.getReservationSlot().getRestaurant().getName());

        // 플레이스홀더 치환
        String formattedBody = type.getFormattedBody(parameters);
        String formattedUrl = type.getFormattedUrl(parameters);

        // 알림 데이터 생성
        Map<String, String> data = createNotificationData(notification, type);
        data.put("url", formattedUrl); // 포맷된 URL로 업데이트

        fcmService.sendMessage(fcmToken, type.getTitle(), formattedBody, data);
    }

    // 알림 데이터 생성
    private Map<String, String> createNotificationData(NotificationQueue notification, NotificationTypes type) {
        Map<String, String> data = new HashMap<>();
        data.put("reservationId", notification.getReservationId().toString());
        data.put("type", type.getType());
        data.put("url", type.getUrl());
        return data;
    }

    // 알림 오류 처리
    private void handleNotificationError(NotificationQueue notification, Exception e) {
        if (isInvalidTokenError(e)) {
            fcmTokenService.deleteFcmToken(notification.getMemberId());
            updateNotificationStatus(notification, NotificationStatus.FAILED);
            saveNotificationLog(notification, false, "Invalid FCM token");
            return;
        }

        if (notification.getRetryCount() < MAX_RETRY_COUNT) {
            retryNotification(notification);
        } else {
            updateNotificationStatus(notification, NotificationStatus.FAILED);
            saveNotificationLog(notification, false, e.getMessage());
        }
    }

    // 알림 재시도 설정
    // 재시도 횟수 증가시키고, 다음 재시도 시간 설정
    private void retryNotification(NotificationQueue notification) {
        notification.incrementRetryCount();
        notification.setScheduledAt(LocalDateTime.now().plusMinutes(RETRY_DELAY_MINUTES));
        notificationQueueRepository.save(notification);
        saveNotificationLog(notification, false, "Scheduled for retry");
    }

    // 오류 코드 확인하여 FCM 토큰 오류인지 확인
    private boolean isInvalidTokenError(Exception e) {
        return e instanceof FirebaseMessagingException &&
                ((FirebaseMessagingException) e).getMessagingErrorCode() ==
                        com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT;
    }

    // 알림 상태를 업데이트
    private void updateNotificationStatus(NotificationQueue notification, NotificationStatus status) {
        notification.setStatus(status.name());
        notificationQueueRepository.save(notification);
    }

    // 알림 처리 결과를 로그로 저장
    private void saveNotificationLog(NotificationQueue notification, boolean success, String errorMessage) {
        NotificationLog log = NotificationLog.builder()
                .notificationQueueId(notification.getId())
                .sentAt(LocalDateTime.now())
                .isSuccess(success)
                .errorMessage(errorMessage)
                .build();

        notificationLogRepository.save(log);
    }

    // 알림 응답 DTO를 생성
    // 알림 큐 항목을 클라이언트에 반환할 응답 형식으로 변환
    private NotificationResponse createNotificationResponse(NotificationQueue queue) {
        return NotificationResponse.builder()
                .id(queue.getId())
                .status(queue.getStatus())
                .scheduledAt(queue.getScheduledAt())
                .build();
    }

    // 알림 상태 조회
    // 알림 ID로 특정 알림의 상태를 조회
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationStatus(Long id) {
        NotificationQueue notification = notificationQueueRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification not found", "NOTIFICATION_NOT_FOUND"));

        return createNotificationResponse(notification);
    }

    // 특정 회원 알림 목록 조회
    // 회원 ID로 알림 목록을 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMemberNotifications(Long memberId, String status) {
        List<NotificationQueue> notifications;
        if (status != null && !status.isEmpty()) {
            notifications = notificationQueueRepository.findByMemberIdAndStatus(memberId, status);
        } else {
            notifications = notificationQueueRepository.findByMemberId(memberId);
        }

        return notifications.stream()
                .map(this::createNotificationResponse)
                .collect(Collectors.toList());
    }
}
