package com.goorm.tablepick.domain.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResultDto {
    private String paymentId;
    private String status; // COMPLETED, FAILED, CANCELLED
}
