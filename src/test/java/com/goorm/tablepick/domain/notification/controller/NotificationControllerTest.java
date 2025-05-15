package com.goorm.tablepick.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.tablepick.domain.notification.dto.request.FcmTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.service.FCMTokenService;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import com.goorm.tablepick.global.exception.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FCMTokenService fcmTokenService;

    private NotificationRequest notificationRequest;
    private NotificationResponse notificationResponse;
    private FcmTokenRequest fcmTokenRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        notificationRequest = NotificationRequest.builder()
                .memberId(1L)
                .reservationId(1L)
                .notificationTypeId(1L)
                .scheduledAt(now.plusHours(1))
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(1L)
                .status("PENDING")
                .scheduledAt(now.plusHours(1))
                .build();

        fcmTokenRequest = FcmTokenRequest.builder()
                .token("test-fcm-token")
                .build();
    }

    @Test
    @DisplayName("알림 예약 테스트")
    @WithMockUser(roles = "USER")
    void scheduleNotification_ShouldReturnCreatedNotification() throws Exception {
        // Given
        when(notificationService.scheduleNotification(any(NotificationRequest.class)))
                .thenReturn(notificationResponse);

        // When & Then
        mockMvc.perform(post("/api/notifications/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(notificationService, times(1)).scheduleNotification(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("알림 상태 조회 테스트")
    @WithMockUser(roles = "USER")
    void getNotificationStatus_ShouldReturnNotification() throws Exception {
        // Given
        when(notificationService.getNotificationStatus(anyLong())).thenReturn(notificationResponse);

        // When & Then
        mockMvc.perform(get("/api/notifications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(notificationService, times(1)).getNotificationStatus(1L);
    }

    @Test
    @DisplayName("존재하지 않는 알림 상태 조회 테스트")
    @WithMockUser(roles = "USER")
    void getNotificationStatus_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        when(notificationService.getNotificationStatus(anyLong()))
                .thenThrow(new NotificationException("Notification not found", "NOTIFICATION_NOT_FOUND"));

        // When & Then
        mockMvc.perform(get("/api/notifications/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(notificationService, times(1)).getNotificationStatus(999L);
    }

    @Test
    @DisplayName("회원 알림 목록 조회 테스트")
    @WithMockUser(roles = "USER")
    void getMemberNotifications_ShouldReturnNotificationList() throws Exception {
        // Given
        List<NotificationResponse> notifications = Arrays.asList(
                notificationResponse,
                NotificationResponse.builder().id(2L).status("SENT").build()
        );

        when(notificationService.getMemberNotifications(anyLong(), anyString())).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications/member/1")
                        .param("status", "SENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("SENT"));

        verify(notificationService, times(1)).getMemberNotifications(1L, "SENT");
    }

    @Test
    @DisplayName("FCM 토큰 업데이트 테스트")
    @WithMockUser(roles = "USER")
    void updateFcmToken_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(fcmTokenService).updateFcmToken(anyLong(), anyString());

        // When & Then
        mockMvc.perform(put("/api/notifications/fcm-token")
                        .param("memberId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fcmTokenRequest)))
                .andExpect(status().isOk());

        verify(fcmTokenService, times(1)).updateFcmToken(1L, "test-fcm-token");
    }

    @Test
    @DisplayName("FCM 토큰 삭제 테스트")
    @WithMockUser(roles = "USER")
    void deleteFcmToken_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(fcmTokenService).deleteFcmToken(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/notifications/fcm-token")
                        .param("memberId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(fcmTokenService, times(1)).deleteFcmToken(1L);
    }
}
