package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.event.model.PaymentFailedEvent;
import com.goorm.tablepick.domain.reservation.event.model.PaymentRedirectEvent;
import com.goorm.tablepick.domain.reservation.event.model.PaymentSuccessEvent;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Kafka Listener (결제 도메인에서 결제 완료 이벤트를 받아서 예약 상태 업데이트)
 //이 부분은 Reservation 도메인 내의 별도의 서비스 레이어에 위치해야 합니다.
 @Service
 @RequiredArgsConstructor
 @Slf4j
 public class ReservationPaymentEventListener {
     private final ReservationRepository reservationRepository;
     private final ReservationSlotRepository reservationSlotRepository;

     @KafkaListener(topics = "payment-success-topic", groupId = "reservation-group")
     @Transactional
     public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
         log.info("결제 성공 이벤트 수신: {}", event);
         Reservation reservation = reservationRepository.findById(event.getReservationId())
                 .orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND));

         // 예약 상태를 '확정'으로 변경하고 결제 정보 업데이트
         reservation.completePayment(event.getPaymentId(), event.getAmount());
         reservationRepository.save(reservation);
         log.info("예약 ID {} 의 결제 상태를 완료로 업데이트", reservation.getId());
     }

     @KafkaListener(topics = "payment-failed-topic", groupId = "reservation-group")
     @Transactional
     public void handlePaymentFailedEvent(PaymentFailedEvent event) {
         log.warn("결제 실패 이벤트 수신: {}", event);
         Reservation reservation = reservationRepository.findById(event.getReservationId())
                 .orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND));

         ReservationSlot reservationSlot = reservation.getReservationSlot();

         // 예약 상태를 '실패'로 변경하고 보상 로직 수행
         reservation.failPayment();
         reservationSlot.setCount(reservationSlot.getCount() - 1);

         reservationRepository.save(reservation);
         log.info("예약 ID {} 의 결제 상태를 실패로 업데이트", reservation.getId());
     }

    @KafkaListener(topics = "payment-redirect-topic", groupId = "reservation-group")
    public void handlePaymentRedirectEvent(PaymentRedirectEvent event) {
        log.info("[Reservation Domain] 결제 리다이렉트 이벤트 수신: {}", event);
        // 이벤트를 통해 받은 paymentUrl을 클라이언트에게 전달해야 합니다.
        // 이 방법은 크게 두 가지로 나눌 수 있습니다:

        // 1. WebSocket을 사용하여 클라이언트에게 실시간 푸시:
        //    가장 이상적인 방법입니다. 클라이언트가 예약 요청 후 WebSocket 연결을 유지하고 있다가,
        //    예약 도메인이 이 이벤트를 받으면 WebSocket을 통해 클라이언트에게 paymentUrl을 푸시합니다.
        //    클라이언트는 이 URL을 받아 PG사 결제 페이지로 리다이렉션합니다.
        //    예: webSocketService.sendPaymentRedirectInfo(event.getReservationId(), event.getPaymentUrl());

        // 2. 예약 상태 조회 API를 통해 클라이언트가 주기적으로 폴링:
        //    클라이언트가 예약 요청 후, 주기적으로 예약 상태 조회 API (예: GET /api/reservations/{id})를 호출합니다.
        //    예약 도메인은 PaymentRedirectEvent를 받으면, 해당 예약 엔티티에 paymentUrl과 같은 정보를 저장합니다.
        //    클라이언트가 API를 호출했을 때, 이 URL 정보가 있다면 클라이언트에게 반환하고, 클라이언트는 리다이렉션합니다.
        //    단점: 폴링은 불필요한 요청을 발생시킬 수 있습니다.

        // 현재는 임시 예약을 생성하고 바로 응답을 주는 방식이므로,
        // 클라이언트에게 "결제 대기 중이며, 곧 결제 페이지로 리다이렉트됩니다."와 같은 메시지를 전달하고,
        // 이후 결제 도메인에서 발행한 redirect 이벤트를 통해 웹소켓 등으로 클라이언트에 최종 URL을 푸시하는 것이 좋습니다.
        // 또는 프론트엔드에서 결제창 팝업을 띄우고, 해당 팝업이 PG사로부터 리다이렉트 URL을 받아 처리하는 방식도 있습니다.

        // 여기서는 예시로 로깅만 합니다. 실제로는 클라이언트 통신 로직이 필요합니다.
        log.info("[Reservation Domain] 예약 ID {} 에 대한 결제 URL: {}", event.getReservationId(), event.getPaymentUrl());
    }
 }