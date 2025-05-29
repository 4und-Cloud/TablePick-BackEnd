package com.goorm.tablepick.domain.payment.event;

import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    @EventListener
    @Transactional
    public void handlePaymentResult(PaymentResultEvent event) {
        Reservation reservation = reservationRepository.findByPaymentId(event.getPaymentId())
                .orElseThrow(() -> new RuntimeException("예약 정보 없음"));

        reservation.setPaymentStatus(event.getStatus());
        if ("COMPLETED".equals(event.getStatus())) {
            reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        } else {
            reservation.setReservationStatus(ReservationStatus.CANCELLED);
            ReservationSlot reservationSlot = reservationSlotRepository.findById(
                            reservation.getReservationSlot().getId())
                    .orElseThrow(() -> new RuntimeException("슬롯 정보 없음"));
            reservationSlot.setCount(Math.max(0, reservationSlot.getCount() - 1));
            reservationSlotRepository.save(reservationSlot);
        }
        reservationRepository.save(reservation);
    }
}