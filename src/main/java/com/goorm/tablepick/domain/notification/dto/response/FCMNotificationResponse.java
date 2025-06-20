package com.goorm.tablepick.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "FCMNotificationResponse", description = "FCM 알림 전송 응답")
public class FCMNotificationResponse {
    
    @Schema(description = "전송 성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "Firebase 응답 메시지 ID (성공 시)", example = "projects/myproject/messages/0:1234567890123456%31bd1c9431bd1c94")
    private String messageId;
    
    @Schema(description = "응답 메시지", example = "알림이 성공적으로 전송되었습니다.")
    private String message;
    
    @Schema(description = "오류 코드 (실패 시)", example = "INVALID_TOKEN")
    private String errorCode;
    
    @Schema(description = "전송 시작 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime sentAt;
    
    @Schema(description = "전송 소요 시간 (밀리초)", example = "1250")
    private Long durationMs;
    
    public static FCMNotificationResponse success(String messageId, LocalDateTime sentAt, Long durationMs) {
        return FCMNotificationResponse.builder()
                .success(true)
                .messageId(messageId)
                .message("알림이 성공적으로 전송되었습니다.")
                .sentAt(sentAt)
                .durationMs(durationMs)
                .build();
    }
    
    public static FCMNotificationResponse success(String messageId) {
        return success(messageId, null, null);
    }
    
    public static FCMNotificationResponse failure(String message, String errorCode, LocalDateTime sentAt,
                                                  Long durationMs) {
        return FCMNotificationResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .sentAt(sentAt)
                .durationMs(durationMs)
                .build();
    }
    
    
    public static FCMNotificationResponse failure(String message, String errorCode) {
        return failure(message, errorCode, null, null);
    }
}