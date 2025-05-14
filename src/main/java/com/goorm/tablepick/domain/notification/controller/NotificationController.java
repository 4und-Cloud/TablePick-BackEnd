package com.goorm.tablepick.domain.notification.controller;

import com.goorm.tablepick.domain.notification.dto.request.FcmTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.service.FCMTokenService;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final FCMTokenService fcmTokenService;

    @PostMapping("/schedule")
    public ResponseEntity<NotificationResponse> scheduleNotification(@RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.scheduleNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationStatus(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<NotificationResponse>> getMemberNotifications(
            @PathVariable Long memberId,
            @RequestParam(required = false) String status) {
        List<NotificationResponse> notifications = notificationService.getMemberNotifications(memberId, status);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @RequestParam Long memberId,
            @RequestBody FcmTokenRequest request) {
        fcmTokenService.updateFcmToken(memberId, request.getToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/fcm-token")
    public ResponseEntity<Void> deleteFcmToken(@RequestParam Long memberId) {
        fcmTokenService.deleteFcmToken(memberId);
        return ResponseEntity.ok().build();
    }
}
