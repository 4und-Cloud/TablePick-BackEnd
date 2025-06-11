package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.external.PaymentApi;
import com.goorm.tablepick.domain.reservation.external.model.PaymentResponse;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentUpdateRecoveryService {
    private final ReservationRepository reservationRepository;
    private final PaymentApi paymentApi;
    private final ReservationExternalUpdateService reservationExternalUpdateService;

    /**
     * 5분마다 실행되어 결제 ID가 없는 또는 PENDING 상태인 예약을 동기화
     */
    @Scheduled(fixedDelay = 300000) // 5분
    public void recoverMissingPaymentIds() {
        // 1. paymentStatus가 PENDING인 예약 조회
        List<Reservation> reservationsWithoutPaymentId = reservationRepository.findByPaymentStatusEquals(
                "PENDING", LocalDateTime.now()
        );

        log.info("결제 ID 미할당 또는 PENDING 상태 예약 발견: {}건", reservationsWithoutPaymentId.size());

        // 2. 각 예약별로 결제 정보 동기화
        for (Reservation reservation : reservationsWithoutPaymentId) {
            syncPaymentId(reservation);
        }
    }

    /**
     * 개별 예약의 결제 ID 동기화
     */
    public void syncPaymentId(Reservation reservation) {
        // 1. 외부 시스템에서 결제 정보 조회 (트랜잭션 밖에서)
        PaymentResponse response = paymentApi.getPaymentInfo(
                reservation.getId(),
                reservation.getMember().getId()
        );

        if (!response.isSuccess() || response.getPaymentId() == null) {
            log.warn("예약 ID: {}의 결제 정보 없음 (응답: {})",
                    reservation.getId(), response);
            return;
        }

        try {
            // 2. 결제 ID 및 상태 업데이트
            reservationExternalUpdateService.updateReservationPayment(reservation.getId(), response.getPaymentId());
            reservation.setPaymentStatus("CONFIRMED"); // 상태 동기화
            reservationRepository.save(reservation);
        } catch (ReservationException e) {
            log.error("예약 ID: {}의 결제 업데이트 실패: {}", reservation.getId(), e.getMessage());
        }
    }
}