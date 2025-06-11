package com.goorm.tablepick.domain.reservation.external;

import com.goorm.tablepick.domain.reservation.external.model.PaymentResponse;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentApi {
    private static final Random random = new Random();

    public PaymentResponse registerPayment(Long reservationId, Long memberId, String paymentAmount) {
        try {
            Thread.sleep(random.nextInt(1000, 1500));
            return PaymentResponse.builder()
                    .success(true)
                    .paymentId(UUID.randomUUID().toString())
                    .errorMessage(null)
                    .build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public PaymentResponse getPaymentInfo(Long reservationId, Long memberId) {
        try {
            Thread.sleep(100);
            return PaymentResponse.builder()
                    .success(true)
                    .paymentId(UUID.randomUUID().toString())
                    .errorMessage(null)
                    .build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}