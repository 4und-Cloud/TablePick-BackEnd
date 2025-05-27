package com.goorm.tablepick.domain.notification.service;

import java.util.Map;

public interface FCMService {
    String sendMessage(String token, String title, String body, Map<String, String> data);
}
