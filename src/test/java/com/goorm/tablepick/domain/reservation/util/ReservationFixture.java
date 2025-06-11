package com.goorm.tablepick.domain.reservation.util;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservationFixture {

    public static Restaurant createRestaurant(String name, Long maxCapacity) {
        return Restaurant.builder()
                .name(name != null ? name : "테스트 레스토랑")
                .restaurantPhoneNumber("010-0000-0000")
                .address("Seoul")
                .xcoordinate(127.0)
                .ycoordinate(37.5)
                .maxCapacity(maxCapacity != null ? maxCapacity : 100L)
                .build();
    }

    public static ReservationSlot createReservationSlot(LocalDate date, LocalTime time, Long count,
                                                        Restaurant restaurant) {
        return ReservationSlot.builder()
                .date(date != null ? date : LocalDate.now().plusDays(1))
                .time(time != null ? time : LocalTime.of(12, 0))
                .count(count != null ? count : 0L)
                .restaurant(restaurant)
                .build();
    }

    public static Member createTestMember(String nickname) {
        // 6자리 랜덤 숫자 사용으로 이메일 길이 제한
        Random random = new Random();
        String randomId = String.format("%06d", random.nextInt(1000000));
        return Member.builder()
                .email("test" + randomId + "@test.com") // 최대 17자 이내
                .nickname(nickname != null ? nickname : "테스트유저")
                .build();
    }

    public static List<Member> createTestMembers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createTestMember("테스트유저" + i))
                .collect(Collectors.toList());
    }
}