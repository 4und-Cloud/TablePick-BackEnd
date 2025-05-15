package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
// 예약 알림 스케줄링 담당 서비스
// 예약 시간 기준으로 1일 전, 3시간 전, 1시간 전에 알림 예약
public class ReservationNotificationScheduler {

    private final ReservationRepository reservationRepository;
    private final NotificationTypesRepository notificationTypesRepository;
    private final NotificationService notificationService;
    private final NotificationQueueRepository notificationQueueRepository;

    // 알림 타입 ID 상수 (실제 DB에 저장된 ID로 변경 필요)
    private static final String TYPE_1DAY_BEFORE = "RESERVATION_1DAY_BEFORE";  // 1일 전 알림 타입 ID
    private static final String TYPE_3HOURS_BEFORE = "RESERVATION_3HOURS_BEFORE"; // 3시간 전 알림 타입 ID
    private static final String TYPE_1HOUR_BEFORE = "RESERVATION_1HOURS_BEFORE"; // 1시간 전 알림 타입 ID

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    // 2일 이내의 예약에 대한 알림을 스케줄링
    public void scheduleNotificationsDaily() {
        log.info("Starting daily notification scheduling...");

        // 현재 시간부터 2일 후까지의 예약을 조회
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = now.plusDays(2);

        // 예약 조회 - 확정된 예약만 대상으로 함
        List<Reservation> upcomingReservations = reservationRepository.findByReservationDateTimeBetween(now,
                twoDaysLater);
        log.info("Found {} upcoming reservations for notification scheduling", upcomingReservations.size());

        for (Reservation reservation : upcomingReservations) {
            // 확정된 예약만 알림 스케줄링
            if (reservation.getReservationStatus() == ReservationStatus.CONFIRMED) {
                try {
                    scheduleReservationNotifications(reservation);
                } catch (Exception e) {
                    log.error("Error scheduling notifications for reservation ID {}: {}",
                            reservation.getId(), e.getMessage(), e);
                }
            }
        }

        log.info("Completed daily notification scheduling");
    }

    @Transactional
    // 특정 예약에 대한 알림을 스케줄링
    public void scheduleReservationNotifications(Reservation reservation) {
        Member member = reservation.getMember();
        ReservationSlot slot = reservation.getReservationSlot();

        if (member == null || slot == null) {
            log.warn("Cannot schedule notifications: member or reservation slot is null for reservation ID: {}",
                    reservation.getId());
            return;
        }

        // FCM 토큰이 없는 회원은 알림을 스케줄링하지 않음
        if (member.getFcmToken() == null || member.getFcmToken().isEmpty()) {
            log.info("Skipping notification scheduling for member ID {} - no FCM token", member.getId());
            return;
        }

        // 예약 일시 계산 - ReservationSlot의 getDateTime() 메서드 사용
        LocalDateTime reservationDateTime = slot.getDateTime();
        if (reservationDateTime == null) {
            log.warn("Cannot schedule notifications: reservation date/time is null for reservation ID: {}",
                    reservation.getId());
            return;
        }

        // 각 알림 시간 계산
        LocalDateTime oneDayBefore = reservationDateTime.minusDays(1);
        LocalDateTime threeHoursBefore = reservationDateTime.minusHours(3);
        LocalDateTime oneHourBefore = reservationDateTime.minusHours(1);

        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 1일 전 알림 스케줄링 (아직 시간이 지나지 않았을 경우에만)
        if (now.isBefore(oneDayBefore)) {
            scheduleNotification(member.getId(), reservation.getId(), TYPE_1DAY_BEFORE, oneDayBefore);
        }

        // 3시간 전 알림 스케줄링 (아직 시간이 지나지 않았을 경우에만)
        if (now.isBefore(threeHoursBefore)) {
            scheduleNotification(member.getId(), reservation.getId(), TYPE_3HOURS_BEFORE, threeHoursBefore);
        }

        // 1시간 전 알림 스케줄링 (아직 시간이 지나지 않았을 경우에만)
        if (now.isBefore(oneHourBefore)) {
            scheduleNotification(member.getId(), reservation.getId(), TYPE_1HOUR_BEFORE, oneHourBefore);
        }
    }

    // 개별 알림을 스케줄링
    private void scheduleNotification(Long memberId, Long reservationId, String notificationTypeStr,
                                      LocalDateTime scheduledAt) {
        // 알림 타입 조회
        Optional<NotificationTypes> notificationTypeOpt = notificationTypesRepository.findByType(notificationTypeStr);
        if (notificationTypeOpt.isEmpty()) {
            log.error("Notification type not found: {}", notificationTypeStr);
            return;
        }

        NotificationTypes notificationType = notificationTypeOpt.get();

        // 이미 동일한 알림이 스케줄링되어 있는지 확인
        boolean alreadyScheduled = isNotificationAlreadyScheduled(memberId, reservationId, notificationType.getId());

        if (alreadyScheduled) {
            log.info("Notification already scheduled for member: {}, reservation: {}, type: {}",
                    memberId, reservationId, notificationTypeStr);
            return;
        }

        // 알림 요청 생성
        NotificationRequest request = NotificationRequest.builder()
                .memberId(memberId)
                .reservationId(reservationId)
                .notificationTypeId(notificationType.getId())
                .scheduledAt(scheduledAt)
                .build();

        // 알림 서비스를 통해 알림 예약
        try {
            notificationService.scheduleNotification(request);
            log.info("Scheduled notification for member: {}, reservation: {}, type: {}, time: {}",
                    memberId, reservationId, notificationTypeStr, scheduledAt);
        } catch (Exception e) {
            log.error("Failed to schedule notification: {}", e.getMessage(), e);
        }
    }

    private boolean isNotificationAlreadyScheduled(Long memberId, Long reservationId, Long notificationTypeId) {
        // 현재 NotificationQueueRepository에는 이 메서드가 없으므로 직접 구현
        // 모든 알림을 가져와서 필터링하는 방식으로 구현 (성능 최적화를 위해 나중에 리포지토리 메서드 추가 필요)
        return notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                memberId,
                reservationId,
                notificationTypeId,
                List.of(NotificationStatus.PENDING.name(), NotificationStatus.SENT.name())
        );
    }

    // 날짜와 시간을 결합하여 LocalDateTime 객체를 생성
//    private LocalDateTime combineDateTime(LocalDate date, LocalTime time) {
//        return LocalDateTime.of(date, time);
//    }
}
