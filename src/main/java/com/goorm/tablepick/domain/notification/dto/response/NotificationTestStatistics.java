package com.goorm.tablepick.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 테스트 통계 정보")
public class NotificationTestStatistics {
    
    @Schema(description = "총 요청된 알림 개수", example = "10")
    private Integer totalRequested;
    
    @Schema(description = "발송 성공한 알림 개수", example = "9")
    private Integer sendSuccessCount;
    
    @Schema(description = "발송 실패한 알림 개수", example = "1")
    private Integer sendFailureCount;
    
    @Schema(description = "발송 성공률 (%)", example = "90.0")
    private Double sendSuccessRate;
    
    @Schema(description = "총 발송 소요 시간 (밀리초)", example = "1500")
    private Long totalSendDuration;
    
    @Schema(description = "평균 발송 소요 시간 (밀리초)", example = "150")
    private Long averageSendDuration;
    
    @Schema(description = "수신 성공한 알림 개수", example = "8")
    private Integer receiveSuccessCount;
    
    @Schema(description = "수신 실패한 알림 개수", example = "1")
    private Integer receiveFailureCount;
    
    @Schema(description = "수신 성공률 (%)", example = "88.9")
    private Double receiveSuccessRate;
    
    @Schema(description = "총 수신 소요 시간 (밀리초)", example = "2000")
    private Long totalReceiveDuration;
    
    @Schema(description = "평균 수신 소요 시간 (밀리초)", example = "250")
    private Long averageReceiveDuration;
    
    @Schema(description = "테스트 시작 시간", example = "2024-01-01T10:00:00")
    private String testStartTime;
    
    @Schema(description = "테스트 완료 시간", example = "2024-01-01T10:00:05")
    private String testEndTime;
    
    @Schema(description = "전체 테스트 소요 시간 (밀리초)", example = "5000")
    private Long totalTestDuration;
}