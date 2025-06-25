package com.goorm.tablepick.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private boolean success;
    private String paymentUrl; // 사용자가 리다이렉션될 PG사 결제 페이지 URL
    private String tid; // PG사 거래 ID
    private String paymentId; // PG사 내부에서 발급한 최종 결제 ID
    private String errorMessage;
}