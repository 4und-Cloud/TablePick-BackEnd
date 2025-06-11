package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.monitoring.BatchContext;
import com.goorm.tablepick.domain.reservation.monitoring.BatchContextHolder;
import com.goorm.tablepick.domain.reservation.monitoring.BatchName;
import com.goorm.tablepick.domain.reservation.service.ImprovedReservationSlotService.ReservationSlotServiceV2;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Slf4j
public class BatchApplication {

    //private final ReservationSlotService reservationSlotService;
    //private final ReservationSlotServiceV1 reservationSlotService;
    private final ReservationSlotServiceV2 reservationSlotService;


    @Transactional
    public void bulkInsertWithMonitoring(List<ReservationSlot> reservationSlots) {
        // Batch 시작 시, 수동으로 BatchContext를 초기화
        BatchContextHolder.initContext(new BatchContext(BatchName.BULK_INSERT));

        reservationSlotService.bulkInsert(reservationSlots);

        BatchContext context = BatchContextHolder.getContext();

        if (context != null) {
            context.log();
        }

        // 메모리 누수 방지를 위해 반드시 호출 필요
        BatchContextHolder.clear();
    }

    @Transactional
    public void bulkDeleteWithMonitoring() {
        // Batch 시작 시, 수동으로 BatchContext를 초기화
        BatchContextHolder.initContext(new BatchContext(BatchName.BULK_DELETE));

        reservationSlotService.bulkDelete();

        BatchContext context = BatchContextHolder.getContext();

        if (context != null) {
            context.log();
        }

        // 메모리 누수 방지를 위해 반드시 호출 필요
        BatchContextHolder.clear();
    }
}