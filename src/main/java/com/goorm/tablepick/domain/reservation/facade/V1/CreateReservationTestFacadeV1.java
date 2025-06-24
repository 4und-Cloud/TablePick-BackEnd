package com.goorm.tablepick.domain.reservation.facade.V1;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.payment.RestPaymentApi;
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
public class CreateReservationTestFacadeV1 {
    private final ReservationExternalUpdateService reservationExternalUpdateService;
    private final ReservationSlotRepository reservationSlotRepository;
    private final MemberRepository memberRepository;
    private final RestPaymentApi paymentApi;

    public void createReservationOptimistic(Long memberId, ReservationRequestDto request) {
        // 0. 멤버 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 0. 예약 슬롯 조회 (읽기 전용)
        ReservationSlot reservationSlot = reservationSlotRepository.findByRestaurantIdAndDateAndTime(
                request.getRestaurantId(), request.getReservationDate(), request.getReservationTime()
        ).orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));

        // 1. 내부 트랜잭션으로 예약 생성
        Reservation reservation = reservationExternalUpdateService.createReservationWithOptimisticTransaction(
                member.getId(),
                reservationSlot.getId(),
                request.getPartySize()
        );

        // 2. 외부 결제 API 호출
        PaymentResponseDto paymentResponse = paymentApi.registerPaymentV1(
                reservation.getId(),
                member.getId(),
                request.getPartySize() * 5000L // 예: 1명당 5,000원
        );

        if (!paymentResponse.isSuccess()) {
            log.error("외부 결제 API 호출 실패. 예약 ID: {}, 오류: {}", reservation.getId(), paymentResponse.getErrorMessage());
        }

        // 3. 외부 API 응답으로 참가자 정보 업데이트
        reservationExternalUpdateService.updateReservationPayment(reservation.getId(), paymentResponse.getPaymentId());
    }


}