package com.goorm.tablepick.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 알림 전송 요청 DTO
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class NotificationRequest {
//    private String type;        // 알림 유형
//    private String title;       // 알림 제목
//    private String body;        // 알림 내용
//    private String url;         // 알림 클릭 시 이동할 URL
//    private Long reservationId; // 예약 ID
//    private Long memberId;      // 회원 ID
//    private String fcmToken;    // FCM 토큰 (Member 엔티티에 없으므로 요청에서 직접 받음)
//}

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String title;
    private String body;
    private String token;
}
