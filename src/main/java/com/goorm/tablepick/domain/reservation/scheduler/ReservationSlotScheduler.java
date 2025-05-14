package com.goorm.tablepick.domain.reservation.scheduler;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationSlotScheduler {

    private final JdbcTemplate jdbcTemplate;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    //@Scheduled(cron = "0 31 11 * * *")
    public void createReservationSlots() {
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().toString(); // 예: "MONDAY"

        String selectSql = """
                SELECT r.id AS restaurant_id, r.max_capacity, oh.open_time, oh.close_time
                FROM restaurant r
                JOIN restaurant_operating_hour oh ON r.id = oh.restaurant_id
                WHERE oh.day_of_week = ? AND oh.is_holiday = FALSE
                """;

        List<Map<String, Object>> restaurants = jdbcTemplate.queryForList(selectSql, dayOfWeek);

        for (Map<String, Object> restaurant : restaurants) {
            Long restaurantId = ((Number) restaurant.get("restaurant_id")).longValue();
            LocalTime openTime = ((Time) restaurant.get("open_time")).toLocalTime();
            LocalTime closeTime = ((Time) restaurant.get("close_time")).toLocalTime();

            createSlotsForRestaurant(restaurantId, today, openTime, closeTime);
        }
    }

    private void createSlotsForRestaurant(Long restaurantId, LocalDate date, LocalTime openTime, LocalTime closeTime) {
        // 자정 넘는 시간은 강제로 22:00으로 제한
        if (closeTime.isBefore(openTime)) {
            closeTime = LocalTime.of(22, 0);
        }

        // 분 조정 (정각으로 맞춤)
        LocalTime adjustedOpenTime = (openTime.getMinute() == 0) ? openTime : openTime.plusHours(1).withMinute(0);
        LocalTime adjustedCloseTime =
                (closeTime.getMinute() == 0) ? closeTime : closeTime.minusMinutes(closeTime.getMinute());

        String checkSql = """
                SELECT COUNT(*) FROM reservation_slot 
                WHERE restaurant_id = ? AND date = ? AND time = ?
                """;

        String insertSql = """
                INSERT INTO reservation_slot (restaurant_id, date, time, count)
                VALUES (?, ?, ?, 0)
                """;

        LocalTime slotTime = adjustedOpenTime;
        while (!slotTime.isAfter(adjustedCloseTime)) {
            Integer count = jdbcTemplate.queryForObject(
                    checkSql, Integer.class, restaurantId, date, slotTime);

            if (count != null && count == 0) {
                jdbcTemplate.update(insertSql, restaurantId, date, slotTime);
            }
            slotTime = slotTime.plusHours(1);
        }

        log.info("✅ 슬롯 생성 완료: restaurant_id = " + restaurantId + ", date = " + date);
        //System.out.println("✅ 슬롯 생성 완료: restaurant_id = " + restaurantId + ", date = " + date);
    }

}
