package com.goorm.tablepick.global.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration defaultKafkaStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tablepick-stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // 기본 값 Serde를 JsonSerde로 바꿔줌
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerde.class);
        // trusted packages
        props.put("spring.json.trusted.packages", "*");
        // default value type 명시 (String key로)
        props.put("spring.json.value.default.type", "com.goorm.tablepick.domain.userevent.dto.UserActionEventDto");
        return new KafkaStreamsConfiguration(props);
    }



}
