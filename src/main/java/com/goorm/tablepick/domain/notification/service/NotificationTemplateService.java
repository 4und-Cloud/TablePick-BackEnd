package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.constant.NotificationType;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import org.springframework.stereotype.Service;

/**
 * 알림 템플릿 서비스
 * 각 알림 유형별로 표준화된 메시지를 생성합니다.
 */
@Service
public class NotificationTemplateService {

    /**
     * 회원가입 환영 알림 템플릿을 생성합니다.
     *
     * @param member 회원 정보
     * @return 알림 요청 객체
     */
    public NotificationRequest createWelcomeNotification(Member member) {
        NotificationRequest request = new NotificationRequest();
//        request.setType(NotificationType.WELCOME);
        request.setTitle("환영합니다!");
        request.setBody(member.getNickname() + "님, 테이블픽에 가입하신 것을 환영합니다.");
//        request.setUrl("/home");
//        request.setMemberId(member.getId());
//        request.setFcmToken(member.getFcmToken());

        return request;
    }

    /**
     * 예약 확인 알림 템플릿을 생성합니다.
     *
     * @param reservation 예약 정보
     * @return 알림 요청 객체
     */
    public NotificationRequest createReservationConfirmationNotification(Reservation reservation) {
        Member member = reservation.getMember();

        NotificationRequest request = new NotificationRequest();
//        request.setType(NotificationType.RESERVATION_CONFIRMATION);
        request.setTitle("식당을 예약하셨습니다!");
        request.setBody(reservation.getRestaurant().getName() + "에 " +
                formatDateTime(reservation.getReservationDateTime()) + "에 예약되었습니다.");
//        request.setUrl("/reservations/" + reservation.getId());
//        request.setReservationId(reservation.getId());
//        request.setMemberId(member.getId());
//        request.setFcmToken(member.getFcmToken());

        return request;
    }

    /**
     * 예약일 1일 전 알림 템플릿을 생성합니다.
     *
     * @param reservation 예약 정보
     * @return 알림 요청 객체
     */
    public NotificationRequest createOneDayBeforeNotification(Reservation reservation) {
        Member member = reservation.getMember();

        NotificationRequest request = new NotificationRequest();
//        request.setType(NotificationType.ONE_DAY_BEFORE);
        request.setTitle("식당 예약일 하루 전입니다!");
        request.setBody(reservation.getRestaurant().getName() + "에 내일 " +
                formatTime(reservation.getReservationDateTime()) + "에 예약이 있습니다.");
//        request.setUrl("/reservations/" + reservation.getId());
//        request.setReservationId(reservation.getId());
//        request.setMemberId(member.getId());
//        request.setFcmToken(member.getFcmToken());

        return request;
    }

    /**
     * 예약시간 3시간 전 알림 템플릿을 생성합니다.
     *
     * @param reservation 예약 정보
     * @return 알림 요청 객체
     */
    public NotificationRequest createThreeHoursBeforeNotification(Reservation reservation) {
        Member member = reservation.getMember();

        NotificationRequest request = new NotificationRequest();
//        request.setType(NotificationType.THREE_HOURS_BEFORE);
        request.setTitle("예약 시간까지 3시간 남았습니다!");
        request.setBody(reservation.getRestaurant().getName() + "에 " +
                formatTime(reservation.getReservationDateTime()) + "에 예약이 있습니다.");
//        request.setUrl("/reservations/" + reservation.getId());
//        request.setReservationId(reservation.getId());
//        request.setMemberId(member.getId());
//        request.setFcmToken(member.getFcmToken());

        return request;
    }

    /**
     * 예약시간 1시간 전 알림 템플릿을 생성합니다.
     *
     * @param reservation 예약 정보
     * @return 알림 요청 객체
     */
    public NotificationRequest createOneHourBeforeNotification(Reservation reservation) {
        Member member = reservation.getMember();

        NotificationRequest request = new NotificationRequest();
//        request.setType(NotificationType.ONE_HOUR_BEFORE);
        request.setTitle("예약 시간이 곧입니다!");
        request.setBody(reservation.getRestaurant().getName() + "에 " +
                formatTime(reservation.getReservationDateTime()) + "에 예약이 있습니다.");
//        request.setUrl("/reservations/" + reservation.getId());
//        request.setReservationId(reservation.getId());
//        request.setMemberId(member.getId());
//        request.setFcmToken(member.getFcmToken());

        return request;
    }

    /**
     * 리뷰 요청 알림 템플릿을 생성합니다.
     *
     * @param reservation 예약 정보
     * @return 알림 요청 객체
     */
    public NotificationRequest createReviewRequestNotification(Reservation reservation) {
        Member member = reservation.getMember();

        NotificationRequest request = new NotificationRequest();
//        request.setType(NotificationType.REVIEW_REQUEST);
        request.setTitle("식사 맛있게 하셨나요? 리뷰 부탁드립니다!");
        request.setBody(reservation.getRestaurant().getName() + "에서의 경험을 공유해주세요.");
//        request.setUrl("/reviews/create?reservationId=" + reservation.getId());
//        request.setReservationId(reservation.getId());
//        request.setMemberId(member.getId());
//        request.setFcmToken(member.getFcmToken());

        return request;
    }

    /**
     * 날짜와 시간을 포맷팅합니다.
     *
     * @param dateTime 날짜와 시간
     * @return 포맷팅된 문자열
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime.getYear() + "년 " +
                dateTime.getMonthValue() + "월 " +
                dateTime.getDayOfMonth() + "일 " +
                formatTime(dateTime);
    }

    /**
     * 시간을 포맷팅합니다.
     *
     * @param dateTime 날짜와 시간
     * @return 포맷팅된 시간 문자열
     */
    private String formatTime(java.time.LocalDateTime dateTime) {
        return String.format("%02d:%02d", dateTime.getHour(), dateTime.getMinute());
    }
}
