package com.goorm.tablepick.domain.notification.controller;

import com.goorm.tablepick.domain.notification.service.ReservationNotificationScheduler;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/schedule")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "알림 스케줄링 API", description = "알림 스케줄링 관련 API")
public class NotificationScheduleController {

    private final ReservationNotificationScheduler scheduler;
    private final ReservationRepository reservationRepository;

    @Operation(
            summary = "일일 알림 스케줄링 실행",
            description = "향후 2일 이내의 모든 예약에 대한 알림을 스케줄링합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스케줄링 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/run-daily")
    public ResponseEntity<Map<String, String>> runDailyScheduling() {
        scheduler.scheduleNotificationsDaily();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Daily notification scheduling completed");

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "특정 예약에 대한 알림 스케줄링",
            description = "지정된 예약 ID에 대한 알림을 스케줄링합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스케줄링 성공"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/reservation/{reservationId}")
    public ResponseEntity<Map<String, String>> scheduleForReservation(
            @Parameter(description = "예약 ID", required = true)
            @PathVariable Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        scheduler.scheduleReservationNotifications(reservation);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification scheduling completed for reservation ID: " + reservationId);

        return ResponseEntity.ok(response);
    }
}
