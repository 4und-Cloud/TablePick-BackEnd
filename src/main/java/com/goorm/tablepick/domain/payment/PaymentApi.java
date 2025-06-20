package com.goorm.tablepick.domain.payment;

import com.goorm.tablepick.domain.payment.dto.PaymentResponseDto;

public interface PaymentApi {
    PaymentResponseDto registerPaymentV0(Long reservationId, Long userId, int amount);
    PaymentResponseDto registerPaymentV1(Long reservationId, Long userId, int amount);

}

