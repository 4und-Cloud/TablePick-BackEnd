package com.goorm.tablepick.domain.payment;

import com.goorm.tablepick.domain.payment.dto.PaymentResponseDto;

public interface PaymentApi {
    PaymentResponseDto registerPayment(Long reservationId, Long userId, int amount);
}

