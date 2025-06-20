package com.goorm.tablepick.domain.userevent.controller;

import com.goorm.tablepick.domain.userevent.dto.UserActionEventDto;
import com.goorm.tablepick.domain.userevent.service.UserEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-event")
@RequiredArgsConstructor
public class UserEventController {

    private final UserEventService userEventService;

    @PostMapping
    public ResponseEntity<String> logClickEvent(@RequestBody UserActionEventDto dto) {
        userEventService.sendClickEvent(dto);
        return ResponseEntity.ok("Event sent successfully");
    }
}