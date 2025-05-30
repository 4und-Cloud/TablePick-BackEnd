package com.goorm.tablepick.domain.payment.event;

import lombok.Getter;

@Getter
public class PaymentResultEvent {
    private final Object source;
    private final String paymentId;
    private final String status;

    public PaymentResultEvent(Object source, String paymentId, String status) {
        this.source = source;
        this.paymentId = paymentId;
        this.status = status;
    }
}