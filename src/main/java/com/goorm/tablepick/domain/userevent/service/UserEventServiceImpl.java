package com.goorm.tablepick.domain.userevent.service;


import com.goorm.tablepick.domain.userevent.dto.UserActionEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventServiceImpl implements UserEventService {
    private final KafkaTemplate<String, UserActionEventDto> kafkaTemplate;
    private static final String TOPIC_NAME = "user-action-events";

    @Override
    public void sendClickEvent(UserActionEventDto dto) {
        kafkaTemplate.send(TOPIC_NAME, dto);
    }
}