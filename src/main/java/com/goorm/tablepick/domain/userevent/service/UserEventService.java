package com.goorm.tablepick.domain.userevent.service;

import com.goorm.tablepick.domain.userevent.dto.UserActionEventDto;

public interface UserEventService {
    void sendClickEvent(UserActionEventDto dto);
}
