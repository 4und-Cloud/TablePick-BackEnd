package com.goorm.tablepick.domain.reservation.facade.V0;

import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.service.ImprovedReservationService.ReservationServiceV2;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptimisticLockFacadeV0 {
    private final CreateReservationTestFacadeV0 createReservationTestFacadeV0;

    private static final long RETRY_DELAY_MS = 50;
    private static final int MAX_RETRY_COUNT = 5; // 재시도 최대 횟수 추가

    public void createReservationWithOptimisticLock(Long memberId, ReservationRequestDto request)
            throws InterruptedException {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                createReservationTestFacadeV0.createReservationOptimistic(memberId, request);
                log.info("예약 생성 성공 - username: {}, paymentId: {}, 총 시도횟수: {}",
                        memberId, retryCount + 1);

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
