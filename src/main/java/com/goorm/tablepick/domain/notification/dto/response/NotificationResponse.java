package com.goorm.tablepick.domain.notification.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 알림 전송 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
//public class NotificationResponse {
//    private Long id;            // 알림 ID
//    private String type;        // 알림 유형
//    private String title;       // 알림 제목
//    private String body;        // 알림 내용
//    private String url;         // 알림 클릭 시 이동할 URL
//    private Long reservationId; // 예약 ID
//    private Long memberId;      // 회원 ID
//    private LocalDateTime sentAt; // 전송 시간
//    private boolean successful;   // 전송 성공 여부
//    private String message;       // 응답 메시지
//
//    // 성공 응답 생성자
//    public NotificationResponse(Long id, String type, String title, String body, String url,
//                                Long reservationId, Long memberId, LocalDateTime sentAt,
//                                boolean successful) {
//        this.id = id;
//        this.type = type;
//        this.title = title;
//        this.body = body;
//        this.url = url;
//        this.reservationId = reservationId;
//        this.memberId = memberId;
//        this.sentAt = sentAt;
//        this.successful = successful;
//        this.message = successful ? "알림이 성공적으로 전송되었습니다." : "알림 전송에 실패했습니다.";
//    }
//
//    // 오류 응답 생성자
//    public NotificationResponse(String errorMessage) {
//        this.successful = false;
//        this.message = errorMessage;
//    }
//}

public class NotificationResponse {
    private Long id;
    private String title;
    private String body;
    private String token;
    private LocalDateTime sentAt;
    private boolean successful;
    private String message;

    public NotificationResponse(Long id, String title, String body, String token, LocalDateTime sentAt,
                                boolean successful) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.token = token;
        this.sentAt = sentAt;
        this.successful = successful;
        this.message = successful ? "알림이 성공적으로 전송되었습니다." : "알림 전송에 실패했습니다.";
    }

    public NotificationResponse(String errorMessage) {
        this.successful = false;
        this.message = errorMessage;
    }
}
