package com.goorm.tablepick.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.tablepick.domain.notification.dto.request.FcmTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.service.FCMTokenService;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    @DisplayName("알림 예약 API")
    void scheduleNotification() throws Exception {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .memberId(1L)
                .notificationTypeId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();

        // when & then
        mockMvc.perform(post("/api/notifications/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService).scheduleNotification(any());
    }

    @Test
    @DisplayName("FCM 토큰 업데이트 API")
    void updateFcmToken() throws Exception {
        // given
        FcmTokenRequest request = FcmTokenRequest.builder()
                .token("test-fcm-token")
                .build();

        // when & then
        mockMvc.perform(put("/api/notifications/fcm-token")
                        .param("memberId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(fcmTokenService).updateFcmToken(any(), any());
    }
}
