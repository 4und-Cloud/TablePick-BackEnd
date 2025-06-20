package com.goorm.tablepick.domain.notification.service;

import com.google.api.core.ApiFuture;
import java.util.Map;

public interface FCMService {
    String sendMessage(String token, String title, String body, Map<String, String> data);
    
    String sendMessageWithLogo(String token, String title, String body, Map<String, String> data);
    
    ApiFuture<String> sendMessageAsync(String token, String title, String body, Map<String, String> data,
                                       boolean dryRun);
}
