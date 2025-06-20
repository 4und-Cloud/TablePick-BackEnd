package com.goorm.tablepick.domain.reservation.service.ImprovedReservationService;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.payment.PaymentApi;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.reservation.service.ReservationExternalUpdateService;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceV0 {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final RestaurantRepository restaurantRepository;
    private final PaymentApi paymentApi;
    private final ReservationExternalUpdateService reservationExternalUpdateService;


    @Transactional
    public String createReservation(String username, ReservationRequestDto request) {
        // 식당 검증
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        // 멤버 검증
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 예약 가능 시간 확인
        ReservationSlot reservationSlot = reservationSlotRepository.findByRestaurantIdAndDateAndTime(
                        request.getRestaurantId(), request.getReservationDate(), request.getReservationTime())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));

        // 중복 예약 확인
        boolean hasDuplicate = reservationRepository.findByReservationSlot(reservationSlot).stream()
                .anyMatch(r -> r.getMember().equals(member) && r.getReservationStatus() == ReservationStatus.CONFIRMED);

        if (hasDuplicate) {
            throw new ReservationException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }

        // 슬롯 카운트 검증
        Long count = reservationSlot.getCount();
        Long maxCapacity = restaurant.getMaxCapacity();
        if (count >= maxCapacity) {
            throw new ReservationException(ReservationErrorCode.EXCEED_RESERVATION_LIMIT);
        }

        // 슬롯 카운트 증가
        reservationSlot.setCount(count + 1);
        reservationSlotRepository.save(reservationSlot);

        // 예약 생성 (PENDING)
        String paymentId = UUID.randomUUID().toString();
        Reservation reservation = Reservation.builder()
                .member(member)
                .reservationSlot(reservationSlot)
                .partySize(request.getPartySize())
                .reservationStatus(ReservationStatus.CONFIRMED)
                .restaurant(restaurant)
                .paymentId(paymentId)
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);

        return paymentId;
    }
}