package com.goorm.tablepick.domain.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;

@Getter
@Schema(description = "예약 요청 시 정보")
public class ReservationRequestDto {
    @Schema(description = "예약 식당 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "예약 인원 수", example = "3")
    private Long reservationPeopleCount;

    @Schema(description = "예약 날짜", example = "2025-05-08")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @Schema(description = "예약 시간", example = "09:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservationTime;
}
