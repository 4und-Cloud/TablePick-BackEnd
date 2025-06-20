package com.goorm.tablepick.domain.payment;

import com.goorm.tablepick.domain.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.domain.payment.dto.PaymentResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Component
public class RestPaymentApi implements PaymentApi {

    private final WebClient webClient; // 예약 서버에서 결제 서버 호출

    @Override
    public PaymentResponseDto registerPaymentV0(Long reservationId, Long userId, int amount) {
        return webClient.post()
                .uri("http://localhost:8083/api/pg/approve")
                .bodyValue(new PaymentRequestDto(reservationId, userId, amount))
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .block(); // 동기 처리
    }

    @Override
    public PaymentResponseDto registerPaymentV1(Long reservationId, Long userId, int amount) {
        return webClient.post()
                .uri("http://localhost:8082/api/payments")
                .bodyValue(new PaymentRequestDto(reservationId, userId, amount))
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .block(); // 동기 처리 (v0이므로 우선 이렇게)
    }

//    public PaymentResponse getPaymentInfo(Long reservationId, Long memberId) {
//        try {
//            Thread.sleep(100);
//            return PaymentResponse.builder()
//                    .success(true)
//                    .paymentId(UUID.randomUUID().toString())
//                    .errorMessage(null)
//                    .build();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
