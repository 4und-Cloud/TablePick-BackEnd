package com.goorm.tablepick.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.goorm.tablepick.domain.reservation.event.model.PaymentRequestEvent;
import com.goorm.tablepick.domain.userevent.dto.UserActionEventDto;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Value("${KAFKA_HOST}")
    private String kafkaHost;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId; // consumer group ID를 주입받음

    // ObjectMapper 빈 설정: 날짜/시간 객체 직렬화/역직렬화 문제 해결
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public ProducerFactory<String, UserActionEventDto> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UserActionEventDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // --- Producer Configuration for PaymentRequestEvent (SAGA 패턴용) ---
    @Bean
    public ProducerFactory<String, PaymentRequestEvent> paymentRequestProducerFactory() { // 빈 이름 변경
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // ADD_TYPE_INFO_HEADERS는 JsonDeserializer가 역직렬화 시 클래스 정보를 파악하는 데 유용합니다.
        // Payment 관련 이벤트들은 명확히 클래스 정보를 전달하는 것이 좋습니다.
        JsonSerializer<PaymentRequestEvent> jsonSerializer = new JsonSerializer<>(objectMapper());
        jsonSerializer.setAddTypeInfo(true); // 타입 정보 헤더에 추가

        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), jsonSerializer);
    }

    @Bean
    public KafkaTemplate<String, PaymentRequestEvent> paymentRequestKafkaTemplate() { // 빈 이름 변경
        return new KafkaTemplate<>(paymentRequestProducerFactory());
    }

    // --- Consumer Configuration for Payment Events (SAGA 패턴용) ---
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId); // application.yml에서 주입받은 group ID 사용
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 또는 "earliest"

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(objectMapper());
        // 역직렬화할 신뢰할 수 있는 패키지 설정.
        // "com.goorm.tablepick.payment.event.model.*"만 지정해도 되지만, 와일드카드로 넓게 잡는 것도 가능
        jsonDeserializer.addTrustedPackages("com.goorm.tablepick.*"); // application.yml과 일치

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // factory.setConcurrency(3); // 필요에 따라 컨슈머 스레드 수 설정
        return factory;
    }
}
