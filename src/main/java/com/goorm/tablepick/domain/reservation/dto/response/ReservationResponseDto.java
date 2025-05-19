package com.goorm.tablepick.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ReservationResponseDto {
    @Schema(description = "예약 ID", example = "3")
    private Long id;

    @Schema(description = "예약 인원 수", example = "3")
    private Long partySize;

    @Schema(description = "예약 날짜", example = "2025-05-08")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @Schema(description = "예약 시간", example = "09:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservationTime;

    @Schema(description = "예약 상태", example = "PENDING, CONFIRMED, CANCELLED")
    private ReservationStatus reservationStatus;

    @Builder
    public ReservationResponseDto(Reservation reservation) {
        this.id = reservation.getId();
        this.partySize = reservation.getPartySize();
        this.reservationDate = reservation.getReservationSlot().getDate();
        this.reservationTime = reservation.getReservationSlot().getTime();
        this.reservationStatus = reservation.getReservationStatus();
    }
}
