package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {

    void createReservation(ReservationRequestDto request);

    void cancelReservation(Long reservationId);

    List<LocalTime> getAvailableReservationTimes(Long restaurantId, LocalDate date);
}
