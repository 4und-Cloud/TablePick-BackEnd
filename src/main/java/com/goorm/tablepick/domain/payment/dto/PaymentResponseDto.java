package com.goorm.tablepick.domain.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private boolean success;
    private String paymentId;
    private String errorMessage;
}