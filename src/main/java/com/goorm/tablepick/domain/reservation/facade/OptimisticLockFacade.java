package com.goorm.tablepick.domain.reservation.facade;

import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.service.ImprovedReservationService.ReservationServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptimisticLockFacade {
    private final ReservationServiceV2 reservationServiceV2;

    private static final long RETRY_DELAY_MS = 50;
    private static final int MAX_RETRY_COUNT = 5; // 재시도 최대 횟수 추가

    public void createReservationWithOptimisticLock(Long memberId, ReservationRequestDto request)
            throws InterruptedException {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                reservationServiceV2.createReservationOptimistic(memberId, request);
                log.info("예약 생성 성공 - username: {}, 총 시도횟수: {}",
                        memberId, retryCount + 1);
                return; // 성공 시 메서드 종료

            } catch (ReservationException e) {
                retryCount++;
                log.warn("예약 생성 재시도 - username: {}, request: {}, 현재 시도횟수: {}, error: {}",
                        memberId, request, retryCount, e.getMessage());
                if (retryCount == MAX_RETRY_COUNT) {
                    throw new ReservationException(ReservationErrorCode.OPTIMISTIC_LOCK_RETRY_EXCEEDED);
                }
                Thread.sleep(RETRY_DELAY_MS);
            } catch (Exception e) {
                retryCount++;
                log.warn("예약 생성 실패 - username: {}, request: {}, 현재 시도횟수: {}, error: {}",
                        memberId, request, retryCount, e.getMessage());
                if (retryCount == MAX_RETRY_COUNT) {
                    throw new ReservationException(ReservationErrorCode.INTERNAL_SERVER_ERROR);
                }
                Thread.sleep(RETRY_DELAY_MS);
            }
        }
        throw new ReservationException(ReservationErrorCode.OPTIMISTIC_LOCK_RETRY_EXCEEDED);
    }
}
