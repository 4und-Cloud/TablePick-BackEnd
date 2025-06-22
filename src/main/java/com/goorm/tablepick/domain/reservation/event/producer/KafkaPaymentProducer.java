package com.goorm.tablepick.domain.reservation.event.producer;

import com.goorm.tablepick.domain.reservation.event.model.PaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPaymentProducer {
    private final KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate;

    public void sendPaymentRequest(PaymentRequestEvent event) {
        kafkaTemplate.send("payment-request-topic", event.getReservationId().toString(), event);
    }
}
