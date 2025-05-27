package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import java.time.LocalDate;
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
// 예약 시간 기준으로 1일 전, 3시간 전, 1시간 전, 3시간 후에 알림 예약
public class ReservationNotificationScheduler {

    private final ReservationRepository reservationRepository;
    private final NotificationTypesRepository notificationTypesRepository;
    private final NotificationService notificationService;
    private final NotificationQueueRepository notificationQueueRepository;

    // 알림 타입 ID 상수
    private static final String TYPE_RESERVATION_COMPLETED = NotificationTypes.RESERVATION_COMPLETED;
    private static final String TYPE_1DAY_BEFORE = NotificationTypes.RESERVATION_1DAY_BEFORE;
    private static final String TYPE_3HOURS_BEFORE = NotificationTypes.RESERVATION_3HOURS_BEFORE;
    private static final String TYPE_1HOUR_BEFORE = NotificationTypes.RESERVATION_1HOUR_BEFORE;
    private static final String TYPE_3HOURS_AFTER = NotificationTypes.RESERVATION_3HOURS_AFTER;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    // 2일 이내의 예약에 대한 알림을 스케줄링
    public void scheduleNotificationsDaily() {
        log.info("일일 알림 스케줄링 진행 중...");

        // 현재 날짜부터 2일 후까지의 예약을 조회
        LocalDate today = LocalDate.now();
        LocalDate twoDaysLater = today.plusDays(2);

        // 확정된 예약만 조회
        List<Reservation> upcomingReservations = reservationRepository.findPendingReservationsBetweenDates(
                today, twoDaysLater);
        log.info("Found {} upcoming confirmed reservations for notification scheduling", upcomingReservations.size());

        for (Reservation reservation : upcomingReservations) {
            try {
                scheduleReservationNotifications(reservation);
            } catch (Exception e) {
                log.error("Error scheduling notifications for reservation ID {}: {}",
                        reservation.getId(), e.getMessage(), e);
            }
        }

        log.info("일일 알림 스케줄링 완료 ");
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

        // 예약 일시 계산 - ReservationSlot의 getDateTime() 메서드 사용
        LocalDateTime reservationDateTime = slot.getDateTime();
        if (reservationDateTime == null) {
            log.warn("Cannot schedule notifications: reservation date/time is null for reservation ID: {}",
                    reservation.getId());
            return;
        }

        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 각 알림 시간 계산
        LocalDateTime oneDayBefore = reservationDateTime.minusDays(1);
        LocalDateTime threeHoursBefore = reservationDateTime.minusHours(3);
        LocalDateTime oneHourBefore = reservationDateTime.minusHours(1);
        LocalDateTime threeHoursAfter = reservationDateTime.plusHours(3);

        // 예약 완료 알림 즉시 생성 (현재 시간에 예약)
        createNotification(member.getId(), reservation.getId(), TYPE_RESERVATION_COMPLETED, now);

        // 1일 전 알림 생성 (미래 시간에 예약)
        createNotification(member.getId(), reservation.getId(), TYPE_1DAY_BEFORE, oneDayBefore);

        // 3시간 전 알림 생성 (미래 시간에 예약)
        createNotification(member.getId(), reservation.getId(), TYPE_3HOURS_BEFORE, threeHoursBefore);

        // 1시간 전 알림 생성 (미래 시간에 예약)
        createNotification(member.getId(), reservation.getId(), TYPE_1HOUR_BEFORE, oneHourBefore);

        // 3시간 후 알림 생성 (미래 시간에 예약)
        createNotification(member.getId(), reservation.getId(), TYPE_3HOURS_AFTER, threeHoursAfter);

        log.info("예약 ID: {}에 대한 모든 알림이 성공적으로 스케줄링되었습니다.", reservation.getId());
    }

    // 개별 알림을 생성
    private void createNotification(Long memberId, Long reservationId, String notificationTypeStr,
                                    LocalDateTime scheduledAt) {
        // 알림 타입 조회 - type 문자열로 조회
        Optional<NotificationTypes> notificationTypeOpt = notificationTypesRepository.findByType(notificationTypeStr);
        if (notificationTypeOpt.isEmpty()) {
            log.error("Notification type not found: {}", notificationTypeStr);
            return;
        }

        NotificationTypes notificationType = notificationTypeOpt.get();
        Long notificationTypeId = notificationType.getId(); // ID 추출

        // 이미 동일한 알림이 스케줄링되어 있는지 확인
        boolean alreadyScheduled = isNotificationAlreadyScheduled(memberId, reservationId, notificationTypeId);

        if (alreadyScheduled) {
            log.info("Notification already scheduled for member: {}, reservation: {}, type: {}",
                    memberId, reservationId, notificationTypeStr);
            return;
        }

        // 알림 요청 생성
        NotificationRequest request = NotificationRequest.builder()
                .memberId(memberId)
                .reservationId(reservationId)
                .notificationTypeId(notificationTypeId) // ID 사용
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
        return notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                memberId,
                reservationId,
                notificationTypeId,
                List.of(NotificationStatus.PENDING.name(), NotificationStatus.SENT.name())
        );
    }
}
