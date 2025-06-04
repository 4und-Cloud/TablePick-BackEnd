package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import com.goorm.tablepick.domain.restaurant.enums.DayOfWeek;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantOperatingHourRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationSlotGenerator {

    private final RestaurantOperatingHourRepository operatingHourRepository;

    public List<ReservationSlot> generateSlotsForWeek() {
        List<ReservationSlot> slots = new ArrayList<>();

        // 오늘 날짜 (2025-06-04) 기준 내일부터 일주일 뒤까지
        LocalDate startDate = LocalDate.now().plusDays(1); // 2025-06-05
        LocalDate endDate = startDate.plusDays(6);        // 2025-06-11

        // 모든 restaurant_operating_hour 조회
        List<RestaurantOperatingHour> operatingHours = operatingHourRepository.findAll();

        for (RestaurantOperatingHour hour : operatingHours) {
            if (hour.isHoliday()) {
                continue; // 휴일은 건너뜀
            }

            LocalTime openTime = hour.getOpenTime();
            LocalTime closeTime = hour.getCloseTime();
            Restaurant restaurant = hour.getRestaurant();
            DayOfWeek dayOfWeek = hour.getDayOfWeek();

            if (openTime == null || closeTime == null) {
                continue; // 시간 정보가 없으면 건너뜀
            }

            // 해당 요일의 날짜 범위 계산
            List<LocalDate> targetDates = getDatesForDayOfWeek(startDate, endDate, dayOfWeek);

            for (LocalDate date : targetDates) {
                slots.addAll(generateSlotsForDate(date, openTime, closeTime, restaurant));
            }
        }

        return slots;
    }

    private List<LocalDate> getDatesForDayOfWeek(LocalDate startDate, LocalDate endDate, DayOfWeek targetDay) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (DayOfWeek.fromJavaDayOfWeek(date.getDayOfWeek()) == targetDay) {
                dates.add(date);
            }
            date = date.plusDays(1);
        }
        return dates;
    }

    private List<ReservationSlot> generateSlotsForDate(LocalDate date, LocalTime openTime, LocalTime closeTime,
                                                       Restaurant restaurant) {
        List<ReservationSlot> slots = new ArrayList<>();

        LocalDateTime start = date.atTime(openTime);
        LocalDateTime end;

        // close_time이 open_time보다 이전일 경우 다음 날로 간주
        LocalDate slotDate = date;
        if (closeTime.isBefore(openTime)) {
            end = date.plusDays(1).atTime(closeTime);
        } else {
            end = date.atTime(closeTime);
        }

        LocalDateTime current = start.truncatedTo(ChronoUnit.HOURS);

        // current가 end 이전이거나 같은 시간일 때까지 슬롯 생성
        while (current.isBefore(end)) {
            System.out.println("Current: " + current + ", End: " + end + ", SlotDate: " + slotDate);
            if (current.toLocalDate().isAfter(date)) {
                slotDate = current.toLocalDate();
            }
            slots.add(ReservationSlot.builder()
                    .date(slotDate)
                    .time(current.toLocalTime())
                    .count(0L)
                    .restaurant(restaurant)
                    .build());
            current = current.plusHours(1);
        }

        return slots;
    }
}