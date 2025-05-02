package com.goorm.tablepick.domain.reservation.controller;

import com.goorm.tablepick.domain.reservation.service.ReservationService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/available-times")
    public ResponseEntity<List<LocalTime>> getAvailableReservationTimes(
            @RequestParam Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<LocalTime> availableTimes = reservationService.getAvailableReservationTimes(restaurantId, date);
        return ResponseEntity.ok(availableTimes);
    }
}
