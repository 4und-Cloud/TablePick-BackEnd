package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import com.goorm.tablepick.domain.restaurant.enums.DayOfWeek;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantOperatingHourRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
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
    private final RestaurantRepository restaurantRepository;

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

    public List<ReservationSlot> generateSlotsForTest(int count, RestaurantRepository restaurantRepository) {
        List<ReservationSlot> slots = new ArrayList<>();
        LocalDate startDate = LocalDate.now().plusDays(1); // 내일 날짜부터 시작
        int slotsPerDay = 10; // 9:00 ~ 18:00, 1시간 간격
        int totalDays = 30;   // 테스트용으로 30일 사용
        int slotsPerRestaurant = slotsPerDay * totalDays; // 식당당 슬롯 수: 10 * 30 = 300

        // 총 슬롯 개수: count * 30 * 10
        int totalSlotsToGenerate = count * slotsPerDay * totalDays;

        // 테스트용 Restaurant 생성 (count 개수만큼)
        List<Restaurant> restaurants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Restaurant restaurant = Restaurant.builder()
                    .name("Test Restaurant " + (i + 1))
                    .restaurantPhoneNumber("010-0000-000" + i)
                    .address("Seoul")
                    .xcoordinate(127.0 + (i * 0.01))
                    .ycoordinate(37.5 + (i * 0.01))
                    .maxCapacity(100L)
                    .build();
            restaurants.add(restaurantRepository.save(restaurant));
        }

        // 각 식당에 대해 슬롯 생성
        for (Restaurant restaurant : restaurants) {
            for (int day = 0; day < totalDays; day++) {
                LocalDate date = startDate.plusDays(day);
                for (int hourIndex = 0; hourIndex < slotsPerDay; hourIndex++) {
                    LocalTime time = LocalTime.of(9 + hourIndex, 0); // 9:00 ~ 18:00
                    slots.add(ReservationSlot.builder()
                            .date(date)
                            .time(time)
                            .count(0L)
                            .restaurant(restaurant)
                            .build());
                }
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