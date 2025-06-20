package com.goorm.tablepick.domain.userevent.service;

import com.goorm.tablepick.domain.userevent.dto.UserActionEventDto;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Configuration
public class UserActionStreamProcessor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Bean
    public KStream<String, UserActionEventDto> userActionStream(StreamsBuilder streamsBuilder) {
        JsonSerde<UserActionEventDto> userActionSerde = new JsonSerde<>(UserActionEventDto.class);

        KStream<String, UserActionEventDto> stream = streamsBuilder.stream(
                "user-action-events",
                Consumed.with(Serdes.String(), userActionSerde)
        );

        // BOARD_VIEW, BOARD_CLICK 처리 (원래 있던 로직)
        KStream<String, Long> boardViews = stream
                .filter((k, v) -> "BOARD_VIEW".equals(v.getActionEventType()))
                .map((k, v) -> new KeyValue<>(String.format("%d_%d", v.getUserId(), v.getTargetId()), 1L));

        KStream<String, Long> boardClicks = stream
                .filter((k, v) -> "BOARD_CLICK".equals(v.getActionEventType()))
                .map((k, v) -> new KeyValue<>(String.format("%d_%d", v.getUserId(), v.getTargetId()), 1L));

        // RESTAURANT_VIEW, RESTAURANT_CLICK 처리 (추가)
        KStream<String, Long> restaurantViews = stream
                .filter((k, v) -> "RESTAURANT_VIEW".equals(v.getActionEventType()))
                .map((k, v) -> new KeyValue<>(String.format("%d_%d", v.getUserId(), v.getTargetId()), 1L));

        KStream<String, Long> restaurantClicks = stream
                .filter((k, v) -> "RESTAURANT_CLICK".equals(v.getActionEventType()))
                .map((k, v) -> new KeyValue<>(String.format("%d_%d", v.getUserId(), v.getTargetId()), 1L));

        // BOARD_VIEW, BOARD_CLICK 집계 및 Redis 저장
        processAndStoreToRedis(boardViews, "board-view-aggregate-store", "board", "view");
        processAndStoreToRedis(boardClicks, "board-click-aggregate-store", "board", "click");

        // RESTAURANT_VIEW, RESTAURANT_CLICK 집계 및 Redis 저장
        processAndStoreToRedis(restaurantViews, "restaurant-view-aggregate-store", "restaurant", "view");
        processAndStoreToRedis(restaurantClicks, "restaurant-click-aggregate-store", "restaurant", "click");

        return stream;
    }

    private void processAndStoreToRedis(KStream<String, Long> stream, String storeName, String type, String action) {
        stream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .aggregate(
                        () -> 0L,
                        (key, value, aggregate) -> aggregate + value,
                        Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(storeName)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long())
                )
                .toStream()
                .foreach((windowedKey, count) -> {
                    String[] parts = windowedKey.key().split("_");
                    String redisKey = parts[0] + ":" + type + ":" + parts[1] + ":" + action;
                    redisTemplate.opsForValue().set(redisKey, String.valueOf(count));

                    // CTR 계산 (click action에서만 동작)
                    if ("click".equalsIgnoreCase(action)) {
                        String viewKey = redisKey.replace(":click", ":view");
                        String viewCountStr = redisTemplate.opsForValue().get(viewKey);
                        if (viewCountStr != null) {
                            try {
                                long viewCount = Long.parseLong(viewCountStr);
                                double ctr = (viewCount == 0) ? 0 : (double) count / viewCount;
                                String ctrKey = redisKey.replace(":click", ":ctr");
                                redisTemplate.opsForValue().set(ctrKey, String.valueOf(ctr));
                            } catch (NumberFormatException e) {
                                System.err.println("View count 값 파싱 실패: " + viewCountStr);
                            }
                        }
                    }
                });
    }
}