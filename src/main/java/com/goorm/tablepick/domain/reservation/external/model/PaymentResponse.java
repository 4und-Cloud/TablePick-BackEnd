package com.goorm.tablepick.domain.reservation.external.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private boolean success;
    private String paymentId;
    private String message;
    private String errorMessage;
}