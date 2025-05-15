package com.goorm.tablepick.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.tablepick.domain.notification.service.ReservationNotificationScheduler;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationScheduleController.class)
class NotificationScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationNotificationScheduler scheduler;

    @MockBean
    private ReservationRepository reservationRepository;

    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testReservation = mock(Reservation.class);
        when(testReservation.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("일일 알림 스케줄링 실행 테스트")
    @WithMockUser(roles = "USER")
    void runDailyScheduling_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(scheduler).scheduleNotificationsDaily();

        // When & Then
        mockMvc.perform(post("/api/notifications/schedule/run-daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Daily notification scheduling completed"));

        verify(scheduler, times(1)).scheduleNotificationsDaily();
    }

    @Test
    @DisplayName("특정 예약에 대한 알림 스케줄링 테스트")
    @WithMockUser(roles = "USER")
    void scheduleForReservation_ShouldReturnSuccess() throws Exception {
        // Given
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(testReservation));
        doNothing().when(scheduler).scheduleReservationNotifications(any(Reservation.class));

        // When & Then
        mockMvc.perform(post("/api/notifications/schedule/reservation/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Notification scheduling completed for reservation ID: 1"));

        verify(reservationRepository, times(1)).findById(1L);
        verify(scheduler, times(1)).scheduleReservationNotifications(testReservation);
    }

    @Test
    @DisplayName("존재하지 않는 예약에 대한 알림 스케줄링 테스트")
    @WithMockUser(roles = "USER")
    void scheduleForReservation_WithNonExistingReservation_ShouldReturnNotFound() throws Exception {
        // Given
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/notifications/schedule/reservation/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(reservationRepository, times(1)).findById(999L);
        verify(scheduler, never()).scheduleReservationNotifications(any(Reservation.class));
    }

    @Test
    @DisplayName("권한 없는 사용자의 일일 알림 스케줄링 실행 테스트")
    void runDailyScheduling_WithoutAuth_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/notifications/schedule/run-daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(scheduler, never()).scheduleNotificationsDaily();
    }
}
