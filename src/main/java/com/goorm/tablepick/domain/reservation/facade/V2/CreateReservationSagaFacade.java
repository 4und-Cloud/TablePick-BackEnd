package com.goorm.tablepick.domain.reservation.facade.V2;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.payment.dto.PaymentResponseDto;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.event.model.PaymentRequestEvent;
import com.goorm.tablepick.domain.reservation.event.producer.KafkaPaymentProducer;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.reservation.service.ReservationExternalUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateReservationSagaFacade {
    private final ReservationExternalUpdateService reservationExternalUpdateService;
    private final ReservationSlotRepository reservationSlotRepository;
    private final MemberRepository memberRepository;
    private final KafkaPaymentProducer kafkaPaymentProducer;

    public void createReservationOptimistic(Long memberId, ReservationRequestDto request) {
        // 1. 내부 트랜잭션으로 예약 생성
        Reservation reservation = reservationExternalUpdateService.createReservationWithOptimisticTransaction(
                memberId,
                request
        );

        // 2. Kafka를 통해 결제 요청 이벤트 발행
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEvent.builder()
                .reservationId(reservation.getId())
                .memberId(memberId)
                .amount(request.getPartySize() * 5000L) // 예: 1명당 5,000원
                .build();

        kafkaPaymentProducer.sendPaymentRequest(paymentRequestEvent);

        log.info("결제 요청 이벤트 발행 완료. 예약 ID: {}", reservation.getId());

        // 클라이언트에게 임시 예약이 생성되었음을 알리고, 결제 페이지로 리다이렉션할 정보를 반환
        // 결제 URL은 Kafka 이벤트를 통해 예약 도메인으로 전달

    }

}