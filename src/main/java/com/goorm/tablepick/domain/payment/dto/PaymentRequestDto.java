package com.goorm.tablepick.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private Long reservationId;
    private Long memberId;
    private Long amount;
}