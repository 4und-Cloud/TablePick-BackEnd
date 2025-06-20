package com.goorm.tablepick.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentRequestDto {
    private Long reservationId;
    private Long memberId;
    private int amount;
}