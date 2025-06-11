package com.goorm.tablepick.domain.reservation.external.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentRequest {
    private Long reservationId;
    private Long memberId;
    private Long paymentAmount;
}