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
@Builder
@AllArgsConstructor
public class ReservationResponseDto {
    @Schema(description = "예약 ID", example = "3")
    private Long id;

    @Schema(description = "예약 인원 수", example = "3")
    private int partySize;

    @Schema(description = "예약 날짜", example = "2025-05-08")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @Schema(description = "예약 시간", example = "09:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservationTime;

    @Schema(description = "예약 상태", example = "PENDING, CONFIRMED, CANCELLED")
    private ReservationStatus reservationStatus;

    @Schema(description = "식당 아이디", example = "1")
    private Long restaurantId;

    @Schema(description = "식당 이름", example = "골목식당")
    private String restaurantName;

    @Schema(description = "식당 주소", example = "서울특별시 강남구 강남대로")
    private String restaurantAddress;

    @Schema(description = "식당 썸네일", example = "url")
    private String restaurantImage;


    public static ReservationResponseDto toDto(Reservation reservation) {
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .partySize(reservation.getPartySize())
                .reservationDate(reservation.getReservationDateTime().toLocalDate())
                .reservationTime(reservation.getReservationDateTime().toLocalTime())
                .reservationStatus(reservation.getReservationStatus())
                .restaurantId(reservation.getRestaurant().getId())
                .restaurantAddress(reservation.getRestaurant().getAddress())
                .restaurantName(reservation.getRestaurant().getName())
                .restaurantImage(
                        reservation.getRestaurant().getRestaurantImages().get(0) != null ? reservation.getRestaurant()
                                .getRestaurantImages().get(0).getImageUrl() : null)
                .build();
    }
}
