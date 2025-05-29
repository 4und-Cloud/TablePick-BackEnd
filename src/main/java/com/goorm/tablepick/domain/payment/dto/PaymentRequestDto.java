package com.goorm.tablepick.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequestDto {
    private String paymentId;
    private Long restaurantId;
    private Long memberId;
    private Long amount;
    private String status; // REQUEST, CANCEL
}