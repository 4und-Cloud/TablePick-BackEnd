package com.goorm.tablepick.domain.notification.controller;

import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.Notification;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 알림 관련 API 컨트롤러
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification API", description = "FCM 알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    @Operation(summary = "알림 전송", description = "FCM을 통해 알림을 전송합니다.")
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "전송된 모든 알림 목록을 조회합니다.")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "회원별 알림 목록 조회", description = "특정 회원에게 전송된 알림 목록을 조회합니다.")
    public ResponseEntity<List<Notification>> getNotificationsByMemberId(@PathVariable Long memberId) {
        List<Notification> notifications = notificationService.getNotificationsByMemberId(memberId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "예약별 알림 목록 조회", description = "특정 예약과 관련된 알림 목록을 조회합니다.")
    public ResponseEntity<List<Notification>> getNotificationsByReservationId(@PathVariable Long reservationId) {
        List<Notification> notifications = notificationService.getNotificationsByReservationId(reservationId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "유형별 알림 목록 조회", description = "특정 유형의 알림 목록을 조회합니다.")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable String type) {
        List<Notification> notifications = notificationService.getNotificationsByType(type);
        return ResponseEntity.ok(notifications);
    }
}
