package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationExternalUpdateService {
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Reservation createReservationWithOptimisticTransaction(Long memberId, Long slotId, int partySize) {
        // 1. 회원 및 예약 슬롯 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        ReservationSlot reservationSlot = reservationSlotRepository.findByIdWithOptimisticLock(slotId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));

        // 2. 예약 슬롯 용량 검증 및 카운트 증가
        if (reservationSlot.getCount() >= reservationSlot.getRestaurant().getMaxCapacity()) {
            throw new ReservationException(ReservationErrorCode.EXCEED_RESERVATION_LIMIT);
        }

        // 3. 중복 예약 확인
        boolean hasDuplicate = reservationRepository.findByReservationSlot(reservationSlot).stream()
                .anyMatch(r -> r.getMember().equals(member) && (r.getReservationStatus() == ReservationStatus.CONFIRMED || r.getReservationStatus() == ReservationStatus.PENDING));
        if (hasDuplicate) {
            throw new ReservationException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }

        reservationSlot.setCount(reservationSlot.getCount() + 1);


        // 3. 예약 생성
        Reservation reservation = Reservation.builder()
                .member(member)
                .reservationSlot(reservationSlot)
                .partySize(partySize)
                .reservationStatus(ReservationStatus.PENDING)
                .paymentStatus("PENDING")
                .restaurant(reservationSlot.getRestaurant())
                .createdAt(LocalDateTime.now())
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation createReservationWithPessimisticTransaction(Long memberId, ReservationRequestDto request) {
        // 0. 예약 슬롯 조회 (읽기 전용)
        ReservationSlot reservationSlot = reservationSlotRepository.findByRestaurantIdAndDateAndTimeWithPessimisticLock(
                request.getRestaurantId(), request.getReservationDate(), request.getReservationTime()
        ).orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));

        // 1. 회원 및 예약 슬롯 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));


        // 2. 예약 슬롯 용량 검증 및 카운트 증가
        if (reservationSlot.getCount() >= reservationSlot.getRestaurant().getMaxCapacity()) {
            throw new ReservationException(ReservationErrorCode.EXCEED_RESERVATION_LIMIT);
        }

        // 3. 중복 예약 확인
        boolean hasDuplicate = reservationRepository.findByReservationSlot(reservationSlot).stream()
                .anyMatch(r -> r.getMember().equals(member) && (r.getReservationStatus() == ReservationStatus.CONFIRMED || r.getReservationStatus() == ReservationStatus.PENDING));
        if (hasDuplicate) {
            throw new ReservationException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }

        reservationSlot.setCount(reservationSlot.getCount() + 1);
        reservationSlotRepository.save(reservationSlot);

        // 3. 예약 생성
        Reservation reservation = Reservation.builder()
                .member(member)
                .reservationSlot(reservationSlot)
                .partySize(request.getPartySize())
                .reservationStatus(ReservationStatus.PENDING)
                .paymentStatus("PENDING")
                .restaurant(reservationSlot.getRestaurant())
                .createdAt(LocalDateTime.now())
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void updateReservationPayment(Long reservationId, String paymentId) {
        // 1. 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND));

        // 2. 결제 ID 및 상태 갱신
        reservation.setPaymentId(paymentId);
        reservation.setPaymentStatus("CONFIRMED");
        reservationRepository.save(reservation);
    }
}
