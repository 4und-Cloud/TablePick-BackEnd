package com.goorm.tablepick.domain.reservation.controller;

import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.facade.CreateReservationFacade;
import com.goorm.tablepick.domain.reservation.facade.OptimisticLockFacade;
import com.goorm.tablepick.domain.reservation.service.ImprovedReservationService.ReservationServiceV2;
import com.goorm.tablepick.domain.reservation.service.ImprovedReservationSlotService.ReservationSlotServiceV2;
import com.goorm.tablepick.domain.reservation.service.ReservationService;
import com.goorm.tablepick.domain.reservation.service.ReservationSlotGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final CreateReservationFacade createReservationFacade;
    private final OptimisticLockFacade optimisticLockFacade;
    private final ReservationServiceV2 reservationServiceV2;
    private final ReservationSlotGenerator reservationSlotGenerator;
    private final ReservationSlotServiceV2 reservationSlotServiceV2;

    @PostMapping
    @Operation(summary = "예약 생성", description = "식당, 유저, 예약 시간 정보를 기반으로 예약을 생성합니다.")
    public ResponseEntity<CreateReservationResponseDto> createReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ReservationRequestDto request) {
        CreateReservationResponseDto dto = reservationService.createReservation(userDetails.getUsername(), request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/pessimistic")
    @Operation(summary = "예약 생성", description = "식당, 유저, 예약 시간 정보를 기반으로 예약을 생성합니다.")
    public ResponseEntity<Void> createReservationPessimistic(@AuthenticationPrincipal UserDetails userDetails,
                                                             @RequestBody @Valid ReservationRequestDto request) {
        reservationServiceV2.createReservationPessimistic(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/optimistic")
    @Operation(summary = "예약 생성", description = "식당, 유저, 예약 시간 정보를 기반으로 예약을 생성합니다.")
    public ResponseEntity<Void> createReservationOptimistic(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestBody @Valid ReservationRequestDto request) {
        try {
            optimisticLockFacade.createReservationWithOptimisticLock(userDetails.getUsername(), request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            throw new RuntimeException();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/current")
    @Operation(summary = "예약 생성", description = "식당, 유저, 예약 시간 정보를 기반으로 예약을 생성합니다.")
    public ResponseEntity<Void> createReservationCurrent(@AuthenticationPrincipal UserDetails userDetails,
                                                         @RequestBody @Valid ReservationRequestDto request) {

        createReservationFacade.createReservation(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{reservationId}")
    @Operation(summary = "예약 취소", description = "예약 ID를 기반으로 예약을 취소합니다.")
    public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Long reservationId) {
        reservationService.cancelReservation(userDetails.getUsername(), reservationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available-times")
    @Operation(
            summary = "예약 가능 시간 조회",
            description = "특정 식당과 날짜에 대해 예약 가능한 시간 목록을 반환합니다.",
            parameters = {
                    @Parameter(
                            name = "restaurantId",
                            description = "조회할 식당 ID",
                            required = true,
                            schema = @Schema(type = "integer", format = "int64", example = "1")
                    ),
                    @Parameter(
                            name = "date",
                            description = "조회할 날짜 (ISO 형식: YYYY-MM-DD)",
                            required = true,
                            schema = @Schema(type = "string", format = "date", example = "2023-12-25")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "예약 가능한 시간 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(type = "string", format = "time", example = "21:00")
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (유효하지 않은 식당 ID 또는 날짜 형식)"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "식당을 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<List<String>> getAvailableReservationTimes(
            @RequestParam Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<String> availableTimes = reservationService.getAvailableReservationTimes(restaurantId, date);
        return ResponseEntity.ok(availableTimes);
    }

    @PostMapping("/generate-slots")
    public ResponseEntity<Void> generateAndPersistSlots() {
        List<ReservationSlot> slots = reservationSlotGenerator.generateSlotsForWeek();
        reservationSlotServiceV2.bulkInsert(slots);
        return ResponseEntity.ok().build();
    }

}
