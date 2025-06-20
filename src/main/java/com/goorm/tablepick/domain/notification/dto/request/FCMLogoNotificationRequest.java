package com.goorm.tablepick.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "FCMLogoNotificationRequest", description = "로고가 포함된 FCM 알림 전송 요청")
public class FCMLogoNotificationRequest {
    
    @Schema(description = "FCM 토큰", example = "dGhpcyBpcyBhIGZha2UgdG9rZW4", required = true)
    private String token;
    
    @Schema(description = "알림 제목", example = "테이블픽 알림", required = true)
    private String title;
    
    @Schema(description = "알림 내용", example = "새로운 예약이 있습니다.", required = true)
    private String body;
    
    @Schema(description = "추가 데이터 (선택사항)", example = "{\"reservationId\": \"123\", \"restaurantName\": \"맛있는 식당\"}")
    private Map<String, String> data;
}