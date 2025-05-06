package com.goorm.tablepick.domain.reservation.controller;

import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "예약 생성", description = "식당, 유저, 예약 시간 정보를 기반으로 예약을 생성합니다.")
    public ResponseEntity<Void> createReservation(@RequestBody @Valid ReservationRequestDto request) {
        reservationService.createReservation(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "예약 취소", description = "예약 ID를 기반으로 예약을 취소합니다.")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available-times")
    @Operation(
            summary = "예약 가능 시간 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "예약 가능한 시간 목록",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(type = "string", format = "time", example = "21:00:00")
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<List<LocalTime>> getAvailableReservationTimes(
            @RequestParam Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<LocalTime> availableTimes = reservationService.getAvailableReservationTimes(restaurantId, date);
        return ResponseEntity.ok(availableTimes);
    }
}
