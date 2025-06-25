package com.goorm.tablepick.domain.reservation.facade.V0;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.payment.PgClient;
import com.goorm.tablepick.domain.payment.RestPaymentApi;
import com.goorm.tablepick.domain.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.payment.dto.PaymentResponseDto;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.reservation.service.ReservationExternalUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateReservationTestFacadeV0 {
    private final ReservationExternalUpdateService reservationExternalUpdateService;
    private final ReservationSlotRepository reservationSlotRepository;
    private final MemberRepository memberRepository;
    private final PgClient pgClient; // Fake PG 서버로 직접 호출하는 클라이언트

    public void createReservationOptimistic(Long memberId, ReservationRequestDto request) {

        // 1. 내부 트랜잭션으로 예약 생성
        Reservation reservation = reservationExternalUpdateService.createReservationWithOptimisticTransaction(
                memberId,
                request
        );

        // 2. 외부 결제 API 호출
        PaymentResponseDto paymentResponse = pgClient.callPgApi( // PgClient를 통해 Fake PG 서버 호출
                PaymentRequestDto.builder()
                        .reservationId(reservation.getId())
                        .memberId(memberId)
                        .amount(request.getPartySize() * 5000L)
                        .build()
        );

        if (!paymentResponse.isSuccess()) {
            log.error("외부 결제 API 호출 실패. 예약 ID: {}, 오류: {}", reservation.getId(), paymentResponse.getErrorMessage());
        }

        // 3. 외부 API 응답으로 참가자 정보 업데이트
        reservationExternalUpdateService.updateReservationPayment(reservation.getId(), paymentResponse.getPaymentId());
    }

    public void createReservationPessimistic(Long memberId, ReservationRequestDto request) {
        // 1. 내부 트랜잭션으로 예약 생성
        Reservation reservation = reservationExternalUpdateService.createReservationWithPessimisticTransaction(
                memberId,
                request
        );

        // 2. 외부 결제 API 호출
        PaymentResponseDto paymentResponse = pgClient.callPgApi( // PgClient를 통해 Fake PG 서버 호출
                PaymentRequestDto.builder()
                        .reservationId(reservation.getId())
                        .memberId(memberId)
                        .amount(request.getPartySize() * 5000L)
                        .build()
        );

        if (!paymentResponse.isSuccess()) {
            log.error("외부 결제 API 호출 실패. 예약 ID: {}, 오류: {}", reservation.getId(), paymentResponse.getErrorMessage());
        }

        // 3. 외부 API 응답으로 참가자 정보 업데이트
        reservationExternalUpdateService.updateReservationPayment(reservation.getId(), paymentResponse.getPaymentId());
    }
}