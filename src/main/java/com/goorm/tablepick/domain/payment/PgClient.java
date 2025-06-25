package com.goorm.tablepick.domain.payment;

import com.goorm.tablepick.domain.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.domain.payment.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PgClient {

    private final WebClient.Builder webClientBuilder;

    public PaymentResponseDto callPgApi(PaymentRequestDto request) {
        WebClient webClient = webClientBuilder.build();
        // PG 연동을 "동기"로 가정했으므로 block() 사용
        // 실제 서비스에서는 비동기 WebClient의 장점을 살려 subscribe() 등으로 처리
        log.info("[PgClient] 외부 PG사 API 호출: {}", request);
        try {
            PaymentResponseDto response = webClient.post()
                    //.uri("http://localhost:8083/api/pg/approve") // 실제 PG사 승인 API 엔드포인트
                    .uri("http://localhost:8083/api/pg/approve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResponseDto.class)
                    .block(); // 동기 처리
            log.info("[PgClient] 외부 PG사 API 응답: {}", response);
            return response;
        } catch (Exception e) {
            log.error("[PgClient] 외부 PG사 API 호출 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 실패 응답 DTO 반환
            return PaymentResponseDto.builder()
                    .success(false)
                    .errorMessage("PG사 통신 오류: " + e.getMessage())
                    .build();
        }
    }
}