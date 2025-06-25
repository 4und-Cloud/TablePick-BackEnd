package com.goorm.tablepick.domain.payment;

import com.goorm.tablepick.domain.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.domain.payment.dto.PaymentResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Component
public class RestPaymentApi{

    private final WebClient.Builder webClientBuilder; // 예약 서버에서 결제 서버 호출

    // 결제 서버 분리 후
    public PaymentResponseDto registerPaymentV1(Long reservationId, Long userId, Long amount) {
        WebClient webClient = webClientBuilder.build();
        return webClient.post()
                .uri("http://localhost:8082/api/payments")
                .bodyValue(new PaymentRequestDto(reservationId, userId, amount))
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .block(); // 동기 처리
    }

}
