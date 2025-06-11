package com.goorm.tablepick.domain.reservation.util;

import com.goorm.tablepick.domain.member.entity.Member;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrentTestUtil {

    @FunctionalInterface
    public interface ReservationTask {
        void join(Long slotId, Long memberId) throws Exception;
    }

    public static void executeConcurrentReservations(
            Long slotId,
            List<Member> members,
            long delayMillis,
            ReservationTask joinTask
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(members.size());

        for (int i = 0; i < members.size(); i++) {
            final int index = i;
            Thread.sleep(delayMillis); // 요청 사이 간격
            executorService.submit(() -> {
                try {
                    joinTask.join(slotId, members.get(index).getId());
                } catch (Exception e) {
                    log.error("예약 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
    }

    /**
     * 확장: 일정 수(batchSize)만큼 병렬 요청 후 딜레이
     */
    public static void executeBatchConcurrentReservations(
            Long slotId,
            List<Member> members,
            int batchSize,
            long delayMillisBetweenBatch,
            ReservationTask joinTask
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(batchSize);
        CountDownLatch latch = new CountDownLatch(members.size());

        for (int i = 0; i < members.size(); i += batchSize) {
            int end = Math.min(i + batchSize, members.size());
            List<Member> batch = members.subList(i, end);

            for (Member member : batch) {
                executorService.submit(() -> {
                    try {
                        joinTask.join(slotId, member.getId());
                    } catch (Exception e) {
                        log.error("예약 실패: {}", e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            if (end < members.size()) {
                Thread.sleep(delayMillisBetweenBatch);
            }
        }

        latch.await(5, TimeUnit.MINUTES);
        executorService.shutdown();
    }
}