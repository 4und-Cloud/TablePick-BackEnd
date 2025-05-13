package com.goorm.tablepick.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FCMTokenRequest {
    private Long memberId;
    private String token;
}
