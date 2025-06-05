package com.goorm.tablepick.domain.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateReservationResponseDto {
    Long reservationId;
}
