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
public class NotificationServiceImpl implements NotificationService {
    private final NotificationQueueRepository notificationQueueRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final FCMService fcmService;
    private final FCMTokenService fcmTokenService;
    private final ReservationRepository reservationRepository;
    private final NotificationTypesRepository notificationTypesRepository;

    // 최대 재시도 횟수 = 3번
    private static final int MAX_RETRY_COUNT = 3;

    // 재시도 간격 (분) = 2분
    private static final int RETRY_DELAY_MINUTES = 2;

    // 알림 예약
    // 지정된 시간에 알림이 전송되도록 큐에 등록
    @Override
    public NotificationResponse scheduleNotification(NotificationRequest request) {
        NotificationTypes type = notificationTypesRepository.findById(request.getNotificationTypeId())
                .orElseThrow(() -> new NotificationException("Notification type not found", "TYPE_NOT_FOUND"));

        // 현재 시간을 createdAt로 설정
        LocalDateTime now = LocalDateTime.now();

        NotificationQueue queue = NotificationQueue.builder()
                .notificationTypes(type)
                .memberId(request.getMemberId())
                .reservationId(request.getReservationId()) // reservationId는 null일 수 있음
                .scheduledAt(request.getScheduledAt()) // 알림이 발송되어야 할 시간
                .status(NotificationStatus.PENDING.name())
                .createdAt(now) // 알림이 생성된 시간
                .build();

        NotificationQueue savedQueue = notificationQueueRepository.save(queue);
        log.info("알림이 예약되었습니다. ID: {}, 타입: {}, 예약 시간: {}, 생성 시간: {}",
                savedQueue.getId(), type.getType(), savedQueue.getScheduledAt(), savedQueue.getCreatedAt());

        return createNotificationResponse(savedQueue);
    }

    // 1분마다 실행되며 현재 시간 이전에 예약된 PENDING 상태의 알림을 처리
    @Override
    @Scheduled(fixedRate = 60000)
    public void processNotificationQueue() {
        log.info("Starting to process notification queue at {}", LocalDateTime.now());

        List<NotificationQueue> pendingNotifications = notificationQueueRepository
                .findByStatusAndScheduledAtBefore(NotificationStatus.PENDING.name(), LocalDateTime.now());

        log.info("Found {} pending notifications to process", pendingNotifications.size());

        // 모든 대기 알림의 ID와 예약 시간 출력
        pendingNotifications.forEach(n ->
                log.debug("Pending notification: ID={}, scheduledAt={}, type={}",
                        n.getId(), n.getScheduledAt(), n.getNotificationTypes().getType()));

        for (NotificationQueue notification : pendingNotifications) {
            // 각 알림 처리를 별도의 트랜잭션으로 처리
            try {
                processNotificationWithNewTransaction(notification);
                log.info("Successfully processed notification ID: {}", notification.getId());
            } catch (Exception e) {
                log.error("Failed to process notification ID {}: {}", notification.getId(), e.getMessage());
                try {
                    // 에러 처리도 별도 트랜잭션으로 처리
                    handleNotificationErrorWithNewTransaction(notification, e);
                } catch (Exception ex) {
                    log.error("Failed to handle error for notification ID {}: {}", notification.getId(),
                            ex.getMessage());
                }
            }
        }
    }

    // 새로운 트랜잭션으로 알림 처리
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processNotificationWithNewTransaction(NotificationQueue notification) {
        processNotification(notification);
    }

    // 새로운 트랜잭션으로 에러 처리
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handleNotificationErrorWithNewTransaction(NotificationQueue notification, Exception e) {
        handleNotificationError(notification, e);
    }

    // 회원의 FCM토큰 조회하고 알림 전송, 성공시 알림 상태 SENT로 업데이트하고 로그 기록
    private void processNotification(NotificationQueue notification) {
        try {
            // FCM 토큰 조회
            String fcmToken = fcmTokenService.getFcmToken(notification.getMemberId());

            if (fcmToken == null || fcmToken.trim().isEmpty()) {
                log.warn("FCM token not found for member ID: {}", notification.getMemberId());
                updateNotificationStatus(notification, NotificationStatus.FAILED);
                saveNotificationLog(notification, false, "FCM token not found");
                return;
            }

            // 예약 정보 조회 시 예외 처리 - reservationId가 null이 아닌 경우에만 조회
            Reservation reservation = null;
            if (notification.getReservationId() != null) {
                try {
                    reservation = reservationRepository.getReservationById(notification.getReservationId());
                    if (reservation == null) {
                        log.warn("Reservation not found for ID: {}, but continuing with notification",
                                notification.getReservationId());
                    }
                } catch (Exception e) {
                    log.warn("Failed to retrieve reservation ID {}: {}, but continuing with notification",
                            notification.getReservationId(), e.getMessage());
                }
            }

            // FCM 메시지 전송 시도
            try {
                String response = sendFcmNotification(notification, fcmToken, reservation);

                // FCM 메시지 전송 성공 시
                if (response != null) {
                    updateNotificationStatus(notification, NotificationStatus.SENT);
                    saveNotificationLog(notification, true, null);
                } else {
                    // FCM 메시지 전송 실패 시 (토큰 문제)
                    fcmTokenService.updateFcmTokenToNull(notification.getMemberId());
                    updateNotificationStatus(notification, NotificationStatus.FAILED);
                    saveNotificationLog(notification, false, "FCM token invalid or message sending failed");
                }
            } catch (Exception e) {
                // FCM 메시지 전송 중 예외 발생
                log.error("Error sending FCM message for notification ID {}: {}", notification.getId(), e.getMessage());
                updateNotificationStatus(notification, NotificationStatus.FAILED);
                saveNotificationLog(notification, false, "Error sending FCM message: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("Unexpected error processing notification ID {}: {}", notification.getId(), e.getMessage());
            try {
                handleNotificationError(notification, e);
            } catch (Exception ex) {
                log.error("Failed to handle error for notification ID {}: {}", notification.getId(), ex.getMessage());
                // 최종적으로 상태 업데이트 시도
                try {
                    updateNotificationStatus(notification, NotificationStatus.FAILED);
                    saveNotificationLog(notification, false, "Critical error: " + e.getMessage());
                } catch (Exception finalEx) {
                    log.error("Critical failure updating notification status for ID {}: {}",
                            notification.getId(), finalEx.getMessage());
                }
            }
        }
    }

    // FCM 메시지 전송
    private String sendFcmNotification(NotificationQueue notification, String fcmToken, Reservation reservation) {
        NotificationTypes type = notification.getNotificationTypes();

        // 파라미터 맵 생성
        Map<String, String> parameters = new HashMap<>();

        // reservationId가 있는 경우에만 관련 정보 추가
        if (notification.getReservationId() != null) {
            parameters.put("id", notification.getReservationId().toString());

            // reservation 객체가 있는 경우에만 레스토랑 이름 추가
            if (reservation != null && reservation.getReservationSlot() != null &&
                    reservation.getReservationSlot().getRestaurant() != null) {
                parameters.put("restaurantName", reservation.getReservationSlot().getRestaurant().getName());
            } else {
                parameters.put("restaurantName", "알 수 없음");
            }
        } else {
            // reservationId가 없는 경우 기본값 설정
            parameters.put("id", "0");
            parameters.put("restaurantName", "테스트 알림");
        }

        // 플레이스홀더 치환
        String formattedBody = type.getFormattedBody(parameters);
        String formattedUrl = type.getFormattedUrl(parameters);

        // 알림 데이터 생성
        Map<String, String> data = createNotificationData(notification, type);
        data.put("url", formattedUrl); // 포맷된 URL로 업데이트

        // FCM 메시지 전송 및 응답 반환
        return fcmService.sendMessage(fcmToken, type.getTitle(), formattedBody, data);
    }

    // 알림 데이터 생성
    private Map<String, String> createNotificationData(NotificationQueue notification, NotificationTypes type) {
        Map<String, String> data = new HashMap<>();
        // reservationId가 null이 아닌 경우에만 추가
        if (notification.getReservationId() != null) {
            data.put("reservationId", notification.getReservationId().toString());
        } else {
            data.put("reservationId", "0"); // 테스트용 기본값
        }
        data.put("type", type.getTitle());
        data.put("url", type.getUrl() != null ? type.getUrl() : "");
        return data;
    }

    // 알림 오류 처리
    private void handleNotificationError(NotificationQueue notification, Exception e) {
        if (isInvalidTokenError(e)) {
            fcmTokenService.updateFcmTokenToNull(notification.getMemberId());
            updateNotificationStatus(notification, NotificationStatus.FAILED);
            saveNotificationLog(notification, false, "FCM token 에러입니다");
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
        saveNotificationLog(notification, false, "재시도된 스케줄링 #" + notification.getRetryCount());
    }

    // 오류 코드 확인하여 FCM 토큰 오류인지 확인
    private boolean isInvalidTokenError(Exception e) {
        if (e instanceof FirebaseMessagingException) {
            FirebaseMessagingException fme = (FirebaseMessagingException) e;
            com.google.firebase.messaging.MessagingErrorCode errorCode = fme.getMessagingErrorCode();

            // 토큰 관련 오류 코드 확인
            return errorCode == com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT ||
                    errorCode == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED ||
                    errorCode == com.google.firebase.messaging.MessagingErrorCode.SENDER_ID_MISMATCH ||
                    "invalid-argument".equals(fme.getErrorCode()) ||
                    "registration-token-not-registered".equals(fme.getErrorCode());
        }

        // FCM 토큰 관련 메시지 확인
        String errorMessage = e.getMessage();
        if (errorMessage != null) {
            return errorMessage.contains("token") ||
                    errorMessage.contains("Token") ||
                    errorMessage.contains("FCM") ||
                    errorMessage.contains("Firebase");
        }

        return false;
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
        // 알림 로그에서 sentAt 정보 조회
        LocalDateTime sentAt = null;
        List<NotificationLog> logs = notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(queue.getId());
        if (!logs.isEmpty() && logs.get(0).getIsSuccess() != null && logs.get(0).getIsSuccess()) {
            sentAt = logs.get(0).getSentAt();
        }
        
        return NotificationResponse.builder()
                .id(queue.getId())
                .status(queue.getStatus())
                .scheduledAt(queue.getScheduledAt())
                .sentAt(sentAt)
                .memberId(queue.getMemberId())
                .reservationId(queue.getReservationId())
                .type(queue.getNotificationTypes() != null ? queue.getNotificationTypes().getType() : null)
                .title(queue.getNotificationTypes() != null ? queue.getNotificationTypes().getTitle() : null)
                .body(queue.getNotificationTypes() != null ? queue.getNotificationTypes().getBody() : null)
                .build();
    }

    // 알림 상태 조회
    // 알림 ID로 특정 알림의 상태를 조회
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationStatus(Long id) {
        NotificationQueue notification = notificationQueueRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification not found", "NOTIFICATION_NOT_FOUND"));

        return createNotificationResponse(notification);
    }

    // 특정 회원 알림 목록 조회
    // 회원 ID로 알림 목록을 조회
    @Override
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
