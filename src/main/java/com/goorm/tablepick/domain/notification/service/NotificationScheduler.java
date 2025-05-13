package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    /**
     * 예약일 1일 전 알림을 전송합니다. (매시 정각에 실행)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendOneDayBeforeNotifications() {
        LocalDateTime targetTime = LocalDateTime.now().plusDays(1);
        LocalDateTime startTime = targetTime.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = targetTime.withMinute(59).withSecond(59).withNano(999999999);

        List<Reservation> reservations = reservationRepository.findByReservationDateTimeBetween(startTime, endTime);

        for (Reservation reservation : reservations) {
            try {
                NotificationRequest request = new NotificationRequest();
//                request.setType("ONE_DAY_BEFORE");
                request.setTitle("식당 예약일 하루 전입니다!");
                request.setBody(reservation.getRestaurant().getName() + "에 내일 " +
                        reservation.getReservationDateTime().toLocalTime() + "에 예약이 있습니다.");
//                request.setUrl("/reservations/" + reservation.getId());
//                request.setReservationId(reservation.getId());
//                request.setMemberId(reservation.getMember().getId());
//                request.setFcmToken(reservation.getMember().getFcmToken());

                notificationService.sendNotification(request);
            } catch (Exception e) {
                log.error("예약일 1일 전 알림 전송 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * 예약시간 3시간 전 알림을 전송합니다. (10분마다 실행)
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendThreeHoursBeforeNotifications() {
        LocalDateTime targetTime = LocalDateTime.now().plusHours(3);
        LocalDateTime startTime = targetTime.withMinute(targetTime.getMinute() / 10 * 10).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(9).withSecond(59).withNano(999999999);

        List<Reservation> reservations = reservationRepository.findByReservationDateTimeBetween(startTime, endTime);

        for (Reservation reservation : reservations) {
            try {
                NotificationRequest request = new NotificationRequest();
//                request.setType("THREE_HOURS_BEFORE");
                request.setTitle("예약 시간까지 3시간 남았습니다!");
                request.setBody(reservation.getRestaurant().getName() + "에 " +
                        reservation.getReservationDateTime().toLocalTime() + "에 예약이 있습니다.");
//                request.setUrl("/reservations/" + reservation.getId());
//                request.setReservationId(reservation.getId());
//                request.setMemberId(reservation.getMember().getId());
//                request.setFcmToken(reservation.getMember().getFcmToken());

                notificationService.sendNotification(request);
            } catch (Exception e) {
                log.error("예약시간 3시간 전 알림 전송 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * 예약시간 1시간 전 알림을 전송합니다. (10분마다 실행)
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendOneHourBeforeNotifications() {
        LocalDateTime targetTime = LocalDateTime.now().plusHours(1);
        LocalDateTime startTime = targetTime.withMinute(targetTime.getMinute() / 10 * 10).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(9).withSecond(59).withNano(999999999);

        List<Reservation> reservations = reservationRepository.findByReservationDateTimeBetween(startTime, endTime);

        for (Reservation reservation : reservations) {
            try {
                NotificationRequest request = new NotificationRequest();
//                request.setType("ONE_HOUR_BEFORE");
                request.setTitle("예약 시간이 곧입니다!");
                request.setBody(reservation.getRestaurant().getName() + "에 " +
                        reservation.getReservationDateTime().toLocalTime() + "에 예약이 있습니다.");
//                request.setUrl("/reservations/" + reservation.getId());
//                request.setReservationId(reservation.getId());
//                request.setMemberId(reservation.getMember().getId());
//                request.setFcmToken(reservation.getMember().getFcmToken());

                notificationService.sendNotification(request);
            } catch (Exception e) {
                log.error("예약시간 1시간 전 알림 전송 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * 예약시간 3시간 후 리뷰 요청 알림을 전송합니다. (10분마다 실행)
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendReviewRequestNotifications() {
        LocalDateTime targetTime = LocalDateTime.now().minusHours(3);
        LocalDateTime startTime = targetTime.withMinute(targetTime.getMinute() / 10 * 10).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(9).withSecond(59).withNano(999999999);

        List<Reservation> reservations = reservationRepository.findByReservationDateTimeBetween(startTime, endTime);

        for (Reservation reservation : reservations) {
            try {
                NotificationRequest request = new NotificationRequest();
//                request.setType("REVIEW_REQUEST");
                request.setTitle("식사 맛있게 하셨나요? 리뷰 부탁드립니다!");
                request.setBody(reservation.getRestaurant().getName() + "에서의 경험을 공유해주세요.");
//                request.setUrl("/reviews/create?reservationId=" + reservation.getId());
//                request.setReservationId(reservation.getId());
//                request.setMemberId(reservation.getMember().getId());
//                request.setFcmToken(reservation.getMember().getFcmToken());

                notificationService.sendNotification(request);
            } catch (Exception e) {
                log.error("리뷰 요청 알림 전송 실패: {}", e.getMessage());
            }
        }
    }
}
