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
        long delay = RETRY_DELAY_MS;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                createReservationTestFacadeV0.createReservationOptimistic(memberId, request);
                log.info("예약 생성 성공 - username: {}, 총 시도횟수: {}", memberId, retryCount + 1);
                return;
            } catch (OptimisticLockException e) {
                retryCount++;
                log.warn("낙관적 락 충돌 - username: {}, 현재 시도횟수: {}, error: {}", memberId, retryCount, e.getMessage());
                if (retryCount == MAX_RETRY_COUNT) {
                    throw new ReservationException(ReservationErrorCode.OPTIMISTIC_LOCK_RETRY_EXCEEDED);
                }
                Thread.sleep(delay);
                delay *= 2; // 지수 백오프
            } catch (ReservationException e) {
                if (e.getErrorCode() == ReservationErrorCode.EXCEED_RESERVATION_LIMIT ||
                        e.getErrorCode() == ReservationErrorCode.DUPLICATE_RESERVATION) {
                    throw e; // 재시도 불필요한 예외는 즉시 throw
                }
                retryCount++;
                log.warn("예약 생성 재시도 - username: {}, 현재 시도횟수: {}, error: {}", memberId, retryCount, e.getMessage());
                if (retryCount == MAX_RETRY_COUNT) {
                    throw new ReservationException(ReservationErrorCode.OPTIMISTIC_LOCK_RETRY_EXCEEDED);
                }
                Thread.sleep(delay);
                delay *= 2;
            }
        }
        throw new ReservationException(ReservationErrorCode.OPTIMISTIC_LOCK_RETRY_EXCEEDED);
    }
}
