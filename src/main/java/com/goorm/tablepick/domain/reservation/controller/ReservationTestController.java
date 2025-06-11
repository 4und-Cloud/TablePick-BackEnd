package com.goorm.tablepick.domain.reservation.controller;

import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.facade.CreateReservationTestFacade;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationTestController {
    private final CreateReservationTestFacade createReservationTestFacade;

    @PostMapping("/test/{memberId}")
    @Operation(summary = "예약 생성", description = "식당, 유저, 예약 시간 정보를 기반으로 예약을 생성합니다.")
    public ResponseEntity<Void> createReservationCurrent(@PathVariable Long memberId,
                                                         @RequestBody @Valid ReservationRequestDto request) {

        createReservationTestFacade.createReservation(memberId, request);
        return ResponseEntity.ok().build();
    }

}
