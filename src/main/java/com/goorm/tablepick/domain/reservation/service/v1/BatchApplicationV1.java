package com.goorm.tablepick.domain.reservation.service.v1;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Slf4j
public class BatchApplicationV1 {

    private final ReservationSlotServiceV1 reservationSlotService;

    // 모니터링용 BatchContextHolder (간단한 구현)
    private static class BatchContext {
        private final String batchName;
        private final LocalDateTime startTime;
        private long duration;

        public BatchContext(String batchName) {
            this.batchName = batchName;
            this.startTime = LocalDateTime.now();
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public void log() {
            log.info("Batch {} completed. Start Time: {}, Duration: {}ms", batchName, startTime, duration);
        }
    }

    private static class BatchContextHolder {
        private static ThreadLocal<BatchContext> context = new ThreadLocal<>();

        public static void initContext(BatchContext ctx) {
            context.set(ctx);
        }

        public static BatchContext getContext() {
            return context.get();
        }

        public static void clear() {
            context.remove();
        }
    }

    @Transactional
    public void bulkInsertWithMonitoring() {
        long start = System.currentTimeMillis();
        BatchContextHolder.initContext(new BatchContext("BULK_INSERT"));

        try {
            List<ReservationSlot> slots = reservationSlotService.generateAndPersistSlots();
            log.info("Successfully inserted {} slots.", slots.size());
        } catch (Exception e) {
            log.error("Error during bulk insert: {}", e.getMessage(), e);
        } finally {
            long end = System.currentTimeMillis();
            long duration = end - start;
            BatchContext context = BatchContextHolder.getContext();
            if (context != null) {
                context.setDuration(duration);
                context.log();
            }
            BatchContextHolder.clear();
        }
    }

    @Transactional
    public void bulkDeleteWithMonitoring() {
        long start = System.currentTimeMillis();
        BatchContextHolder.initContext(new BatchContext("BULK_DELETE"));

        try {
            reservationSlotService.bulkDelete();
            log.info("Successfully deleted all reservation slots.");
        } catch (Exception e) {
            log.error("Error during bulk delete: {}", e.getMessage(), e);
        } finally {
            long end = System.currentTimeMillis();
            long duration = end - start;
            BatchContext context = BatchContextHolder.getContext();
            if (context != null) {
                context.setDuration(duration);
                context.log();
            }
            BatchContextHolder.clear();
        }
    }
}