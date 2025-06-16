package com.goorm.tablepick.domain.userevent.service;

import com.goorm.tablepick.domain.userevent.dto.UserClickEventDto;

public interface UserEventService {
    void sendClickEvent(UserClickEventDto dto);
}
