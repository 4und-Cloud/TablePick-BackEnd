package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.ReservationTime;
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
    private final ReservationTimeRepository reservationTimeRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantOperatingHourRepository restaurantOperatingHourRepository;


    @Override
    @Transactional
    public void createReservation(ReservationRequestDto request) {

    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {

    }

    @Override
    @Transactional
    public List<LocalTime> getAvailableReservationTimes(Long restaurantId, LocalDate date) {
        //해당 식당 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        //해당 식당의 영업 시간 확인
        RestaurantOperatingHour restaurantOperatingHour = restaurantOperatingHourRepository.findByRestaurantId(
                        restaurantId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_OPERATING_HOUR));

        //해당 날짜의 예약 가능 시간 확인
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAvailableTimes(
                restaurantOperatingHour.getId());

        // LocalTime만 추출
        List<LocalTime> availableTimes = reservationTimes.stream()
                .map(ReservationTime::getTime)
                .toList();

        return availableTimes;
    }
}

