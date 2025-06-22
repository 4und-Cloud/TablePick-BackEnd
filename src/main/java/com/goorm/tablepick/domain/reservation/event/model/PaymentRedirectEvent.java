package com.goorm.tablepick.domain.reservation.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRedirectEvent {
    private Long reservationId;
    private String paymentUrl; // PG사에서 받은 결제 페이지 URL
    private String tid; // PG사 거래 ID (옵션)
    // 필요한 경우, 클라이언트가 다음 동작을 수행할 수 있도록 추가 정보 포함 가능
}