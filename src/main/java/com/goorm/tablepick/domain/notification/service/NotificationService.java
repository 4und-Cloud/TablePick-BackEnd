//package com.goorm.tablepick.domain.notification.service;
//
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.Message;
//import com.goorm.tablepick.domain.member.entity.Member;
//import com.goorm.tablepick.domain.member.repository.MemberRepository;
//import com.goorm.tablepick.domain.notification.constant.NotificationMessage;
//import com.goorm.tablepick.domain.notification.constant.NotificationType;
//import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
//import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
//import com.goorm.tablepick.domain.notification.entity.Notification;
//import com.goorm.tablepick.domain.notification.repository.NotificationRepository;
//import com.goorm.tablepick.domain.reservation.entity.Reservation;
//import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
//import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
//import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//
//// 알림 관련 서비스
//@Slf4j
//@Service
//public class NotificationService {
//
//    private final NotificationRepository notificationRepository;
//    private final MemberRepository memberRepository;
//    private final ReservationRepository reservationRepository;
//    private final FCMService fcmService;
//
//    @Autowired
//    public NotificationService(NotificationRepository notificationRepository,
//                               MemberRepository memberRepository,
//                               ReservationRepository reservationRepository,
//                               FCMService fcmService) {
//        this.notificationRepository = notificationRepository;
//        this.memberRepository = memberRepository;
//        this.reservationRepository = reservationRepository;
//        this.fcmService = fcmService;
//    }
//
//    // FCM을 통해 알림을 전송
//    @Transactional
//    public NotificationResponse sendNotification(NotificationRequest request) {
//        try {
//            // 알림 엔티티 생성
//            Notification notification = new Notification(
//                    request.getTitle(),
//                    request.getBody(),
//                    request.getToken()
//            );
//
//            // FCM 메시지 생성 - 수정된 부분
//            Message message = Message.builder()
//                    .setToken(request.getToken())
//                    .setNotification(com.google.firebase.messaging.Notification.builder()
//                            .setTitle(request.getTitle())
//                            .setBody(request.getBody())
//                            .build())
//                    .build();
//
//            // FCM으로 메시지 전송
//            String messageId = FirebaseMessaging.getInstance().send(message);
//
//            // 전송 성공 설정
//            notification.setSuccessful(true);
//
//            // 데이터베이스에 저장
//            notification = notificationRepository.save(notification);
//
//            return new NotificationResponse(
//                    notification.getId(),
//                    notification.getTitle(),
//                    notification.getBody(),
//                    notification.getToken(),
//                    notification.getSentAt(),
//                    notification.isSuccessful()
//            );
//        } catch (Exception e) {
//            // 오류 발생 시 실패 응답 반환
//            return new NotificationResponse("알림 전송 중 오류 발생: " + e.getMessage());
//        }
////        try {
////            // 회원 및 예약 정보 조회
////            Optional<Member> memberOpt = memberRepository.findById(request.getMemberId());
////            Optional<Reservation> reservationOpt = reservationRepository.findById(request.getReservationId());
////
////            if (memberOpt.isEmpty()) {
////                return new NotificationResponse("존재하지 않는 회원입니다.");
////            }
////
////            if (reservationOpt.isEmpty()) {
////                return new NotificationResponse("존재하지 않는 예약입니다.");
////            }
////
////            Member member = memberOpt.get();
////            Reservation reservation = reservationOpt.get();
////
////            // FCM 토큰 가져오기
////            String fcmToken = request.getFcmToken();
////            if (fcmToken == null || fcmToken.isEmpty()) {
////                fcmToken = member.getFcmToken();
////                if (fcmToken == null || fcmToken.isEmpty()) {
////                    return new NotificationResponse("FCM 토큰이 없습니다.");
////                }
////            }
////
////            // 알림 데이터 설정
////            Map<String, String> data = new HashMap<>();
////            data.put("type", request.getType());
////            data.put("url", request.getUrl());
////            data.put("reservationId", String.valueOf(request.getReservationId()));
////
////            // FCM 메시지 전송
////            String messageId = fcmService.sendMessage(
////                    fcmToken,
////                    request.getTitle(),
////                    request.getBody(),
////                    data
////            );
////
////            if (messageId == null) {
////                return new NotificationResponse("알림 전송에 실패했습니다.");
////            }
////
////            // 알림 엔티티 생성 및 저장
////            Notification notification = Notification.builder()
////                    .type(request.getType())
////                    .title(request.getTitle())
////                    .body(request.getBody())
////                    .url(request.getUrl())
////                    .reservation(reservation)
////                    .member(member)
////                    .sentAt(LocalDateTime.now())
////                    .build();
////
////            notification = notificationRepository.save(notification);
////
////            // 응답 생성
////            return new NotificationResponse(
////                    notification.getId(),
////                    notification.getType(),
////                    notification.getTitle(),
////                    notification.getBody(),
////                    notification.getUrl(),
////                    reservation.getId(),
////                    member.getId(),
////                    notification.getSentAt(),
////                    true
////            );
////        } catch (Exception e) {
////            log.error("알림 전송 중 오류 발생", e);
////            return new NotificationResponse("알림 전송 중 오류가 발생했습니다: " + e.getMessage());
////        }
//    }
//
//    // 모든 알림 목록을 조회
//    @Transactional(readOnly = true)
//    public List<Notification> getAllNotifications() {
//        return notificationRepository.findAll();
//    }
//
//    // 회원 ID로 알림 목록을 조회
//    @Transactional(readOnly = true)
//    public List<Notification> getNotificationsByMemberId(Long memberId) {
//        return notificationRepository.findByMemberId(memberId);
//    }
//
//    // 예약 ID로 알림 목록을 조회
//    @Transactional(readOnly = true)
//    public List<Notification> getNotificationsByReservationId(Long reservationId) {
//        return notificationRepository.findByReservationId(reservationId);
//    }
//
//    // 알림 유형으로 알림 목록을 조회
//    @Transactional(readOnly = true)
//    public List<Notification> getNotificationsByType(String type) {
//        return notificationRepository.findByType(type);
//    }
//
//    // 회원가입 환영 알림을 전송
//    @Transactional
//    public NotificationResponse sendWelcomeNotification(Member member) {
//        NotificationRequest request = new NotificationRequest();
////        request.setType("WELCOME");
//        request.setTitle("환영합니다!");
//        request.setBody(member.getNickname() + "님, 테이블픽에 가입하신 것을 환영합니다.");
////        request.setUrl("/home");
////        request.setMemberId(member.getId());
////        request.setFcmToken(member.getFcmToken());
//
//        // 예약 없이 알림을 보내는 경우 임시 예약 ID 설정
//        // 실제 구현에서는 예약 없는 알림을 위한 별도 로직 필요
////        request.setReservationId(1L);
//
//        return sendNotification(request);
//    }
//
//    // 예약 확인 알림을 전송
//    @Transactional
//    public NotificationResponse sendReservationConfirmationNotification(Reservation reservation) {
//        Member member = reservation.getMember();
//        ReservationSlot slot = reservation.getReservationSlot();
//        Restaurant restaurant = slot.getRestaurant();
//
//        // 날짜와 시간 정보 조합
//        String dateTimeStr = String.format("%s %s",
//                slot.getDate().toString(),
//                slot.getTime().toString());
//
//        NotificationRequest request = new NotificationRequest();
////        request.setType(NotificationType.RESERVATION_CONFIRMATION);
//        request.setTitle(NotificationMessage.NOTIFICATION_TITLE);
//        request.setBody(String.format("%s에 %s 예약이 완료되었습니다.",
//                restaurant.getName(),
//                dateTimeStr));
////        request.setUrl("/reservations/" + reservation.getId());
////        request.setReservationId(reservation.getId());
////        request.setMemberId(member.getId());
////        request.setFcmToken(member.getFcmToken());
//
//        return sendNotification(request);
//    }
//}
