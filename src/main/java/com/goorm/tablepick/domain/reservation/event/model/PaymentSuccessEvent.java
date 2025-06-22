package com.goorm.tablepick.domain.reservation.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private Long reservationId;
    private Long memberId;
    private Long amount;
    private String paymentId;
}