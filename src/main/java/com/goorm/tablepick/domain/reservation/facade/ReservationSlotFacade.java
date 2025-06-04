package com.goorm.tablepick.domain.reservation.facade;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.service.ReservationSlotGenerator;
import com.goorm.tablepick.domain.reservation.service.ReservationSlotPersister;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationSlotFacade {

    private static final Logger logger = LoggerFactory.getLogger(ReservationSlotFacade.class);

    private final ReservationSlotGenerator slotGenerator;
    private final ReservationSlotPersister slotPersister;

    @Scheduled(cron = "0 17 14 * * *")
    //@Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 자정(00:00)에 실행
    public void generateAndPersistWeeklySlots() {
        logger.info("Starting weekly slot generation at {}", LocalDateTime.now());

        try {
            // 슬롯 생성
            List<ReservationSlot> slots = slotGenerator.generateSlotsForWeek();

            // 슬롯 저장
            slotPersister.persistSlots(slots);

            logger.info("Completed weekly slot generation at {}. Generated {} slots.", LocalDateTime.now(),
                    slots.size());
        } catch (Exception e) {
            logger.error("Error during weekly slot generation: {}", e.getMessage(), e);
        }
    }
}