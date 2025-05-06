package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationTime;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationTimeRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantOperatingHourRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantOperatingHourRepository restaurantOperatingHourRepository;


    @Override
    @Transactional
    public void createReservation(ReservationRequestDto request) {
        // 식당 검증
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        // 멤버 검증 (임시 로그인용)
        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 해당 날짜의 운영 시간 조회
        RestaurantOperatingHour operatingHour = restaurantOperatingHourRepository.findByRestaurantIdAndDate(
                        restaurant.getId(), request.getReservationDate())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_OPERATING_HOUR));

        // 예약 가능 시간 조회 및 검증 (예약 총 횟수가 3 미만인지)
        ReservationTime reservationTime = reservationTimeRepository.findByRestaurantOperatingHourIdAndTime(
                        operatingHour.getId(), request.getReservationTime())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_TIME));

        Long count = reservationTime.getCount();

        if (count >= 3) {
            throw new ReservationException(ReservationErrorCode.EXCEED_RESERVATION_LIMIT);
        }

        // 예약 시간 count 증가
        reservationTime.setCount(reservationTime.getCount() + 1);
        reservationTimeRepository.save(reservationTime);

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .restaurant(restaurant)
                .member(member)
                .reservationDate(request.getReservationDate())
                .reservationTime(request.getReservationTime())
                .reservationPeopleCount(request.getReservationPeopleCount())
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();

        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        // 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND));

        // 멤버 검증 (임시 로그인용)
        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (!reservation.getMember().equals(member)) {
            throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_CANCEL);
        }

        // 이미 취소된 예약인지 확인
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationException(ReservationErrorCode.ALREADY_CANCELLED);
        }

        // 예약 상태 변경
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // 해당 운영 시간 조회
        RestaurantOperatingHour operatingHour = restaurantOperatingHourRepository.findByRestaurantIdAndDate(
                reservation.getRestaurant().getId(),
                reservation.getReservationDate()
        ).orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_OPERATING_HOUR));

        // 예약 시간 조회
        ReservationTime reservationTime = reservationTimeRepository.findByRestaurantOperatingHourIdAndTime(
                operatingHour.getId(),
                reservation.getReservationTime()
        ).orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_TIME));

        // count 감소 (최소 0)
        long currentCount = reservationTime.getCount();
        reservationTime.setCount(Math.max(0, currentCount - 1));
        reservationTimeRepository.save(reservationTime);
    }

    @Override
    @Transactional
    public List<LocalTime> getAvailableReservationTimes(Long restaurantId, LocalDate date) {
        //해당 식당 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        //해당 식당의 영업 시간 확인
        RestaurantOperatingHour restaurantOperatingHour = restaurantOperatingHourRepository.findByRestaurantIdAndDate(
                        restaurantId, date)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_OPERATING_HOUR));

        //해당 날짜의 예약 가능 시간 확인
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAvailableTimes(
                restaurantOperatingHour.getId());

        //LocalTime만 추출
        List<LocalTime> availableTimes = reservationTimes.stream()
                .map(ReservationTime::getTime)
                .toList();

        return availableTimes;
    }
}

