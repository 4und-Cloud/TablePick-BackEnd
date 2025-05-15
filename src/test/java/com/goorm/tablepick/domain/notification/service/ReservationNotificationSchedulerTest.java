package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationNotificationSchedulerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotificationTypesRepository notificationTypesRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationQueueRepository notificationQueueRepository;

    @InjectMocks
    private ReservationNotificationScheduler scheduler;

    private Reservation testReservation;
    private Member testMember;
    private ReservationSlot testSlot;
    private NotificationTypes type1Day;
    private NotificationTypes type3Hours;
    private NotificationTypes type1Hour;
    private LocalDateTime reservationDateTime;

    @BeforeEach
    void setUp() {
        reservationDateTime = LocalDateTime.now().plusDays(1).plusHours(2);

        testMember = mock(Member.class);
        when(testMember.getId()).thenReturn(1L);
        when(testMember.getFcmToken()).thenReturn("test-fcm-token");

        testSlot = mock(ReservationSlot.class);
        when(testSlot.getDateTime()).thenReturn(reservationDateTime);

        testReservation = mock(Reservation.class);
        when(testReservation.getId()).thenReturn(1L);
        when(testReservation.getMember()).thenReturn(testMember);
        when(testReservation.getReservationSlot()).thenReturn(testSlot);
        when(testReservation.getReservationStatus()).thenReturn(ReservationStatus.CONFIRMED);

        type1Day = mock(NotificationTypes.class);
        when(type1Day.getId()).thenReturn(1L);
        when(type1Day.getType()).thenReturn("RESERVATION_1DAY_BEFORE");

        type3Hours = mock(NotificationTypes.class);
        when(type3Hours.getId()).thenReturn(2L);
        when(type3Hours.getType()).thenReturn("RESERVATION_3HOURS_BEFORE");

        type1Hour = mock(NotificationTypes.class);
        when(type1Hour.getId()).thenReturn(3L);
        when(type1Hour.getType()).thenReturn("RESERVATION_1HOURS_BEFORE");
    }

    @Test
    @DisplayName("일일 알림 스케줄링 테스트")
    void scheduleNotificationsDaily_ShouldScheduleForUpcomingReservations() {
        // Given
        List<Reservation> upcomingReservations = Collections.singletonList(testReservation);
        when(reservationRepository.findByReservationDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(upcomingReservations);

        // When
        scheduler.scheduleNotificationsDaily();

        // Then
        verify(reservationRepository, times(1))
                .findByReservationDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(testReservation, times(1)).getReservationStatus();
        verify(testReservation, times(1)).getMember();
        verify(testReservation, times(1)).getReservationSlot();
    }

    @Test
    @DisplayName("특정 예약에 대한 알림 스케줄링 테스트")
    void scheduleReservationNotifications_ShouldScheduleAllNotificationTypes() {
        // Given
        when(notificationTypesRepository.findByType("RESERVATION_1DAY_BEFORE")).thenReturn(Optional.of(type1Day));
        when(notificationTypesRepository.findByType("RESERVATION_3HOURS_BEFORE")).thenReturn(Optional.of(type3Hours));
        when(notificationTypesRepository.findByType("RESERVATION_1HOURS_BEFORE")).thenReturn(Optional.of(type1Hour));

        when(notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                anyLong(), anyLong(), anyLong(), anyList())).thenReturn(false);

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .status(NotificationStatus.PENDING.name())
                .build();
        when(notificationService.scheduleNotification(any(NotificationRequest.class))).thenReturn(response);

        // When
        scheduler.scheduleReservationNotifications(testReservation);

        // Then
        verify(notificationTypesRepository, times(1)).findByType("RESERVATION_1DAY_BEFORE");
        verify(notificationTypesRepository, times(1)).findByType("RESERVATION_3HOURS_BEFORE");
        verify(notificationTypesRepository, times(1)).findByType("RESERVATION_1HOURS_BEFORE");

        // Verify that we check for existing notifications
        verify(notificationQueueRepository, times(3))
                .existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                        eq(1L), eq(1L), anyLong(), anyList());

        // Verify that we schedule notifications
        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService, times(3)).scheduleNotification(requestCaptor.capture());

        List<NotificationRequest> capturedRequests = requestCaptor.getAllValues();
        assertEquals(3, capturedRequests.size());

        // Verify the first request (1 day before)
        assertEquals(1L, capturedRequests.get(0).getMemberId());
        assertEquals(1L, capturedRequests.get(0).getReservationId());
        assertEquals(1L, capturedRequests.get(0).getNotificationTypeId());

        // Verify the second request (3 hours before)
        assertEquals(1L, capturedRequests.get(1).getMemberId());
        assertEquals(1L, capturedRequests.get(1).getReservationId());
        assertEquals(2L, capturedRequests.get(1).getNotificationTypeId());

        // Verify the third request (1 hour before)
        assertEquals(1L, capturedRequests.get(2).getMemberId());
        assertEquals(1L, capturedRequests.get(2).getReservationId());
        assertEquals(3L, capturedRequests.get(2).getNotificationTypeId());
    }

    @Test
    @DisplayName("이미 스케줄링된 알림은 중복 스케줄링하지 않는 테스트")
    void scheduleReservationNotifications_ShouldNotScheduleDuplicateNotifications() {
        // Given
        when(notificationTypesRepository.findByType("RESERVATION_1DAY_BEFORE")).thenReturn(Optional.of(type1Day));
        when(notificationTypesRepository.findByType("RESERVATION_3HOURS_BEFORE")).thenReturn(Optional.of(type3Hours));
        when(notificationTypesRepository.findByType("RESERVATION_1HOURS_BEFORE")).thenReturn(Optional.of(type1Hour));

        // First notification is already scheduled
        when(notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                eq(1L), eq(1L), eq(1L), anyList())).thenReturn(true);

        // Others are not scheduled
        when(notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                eq(1L), eq(1L), eq(2L), anyList())).thenReturn(false);
        when(notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                eq(1L), eq(1L), eq(3L), anyList())).thenReturn(false);

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .status(NotificationStatus.PENDING.name())
                .build();
        when(notificationService.scheduleNotification(any(NotificationRequest.class))).thenReturn(response);

        // When
        scheduler.scheduleReservationNotifications(testReservation);

        // Then
        verify(notificationTypesRepository, times(1)).findByType("RESERVATION_1DAY_BEFORE");
        verify(notificationTypesRepository, times(1)).findByType("RESERVATION_3HOURS_BEFORE");
        verify(notificationTypesRepository, times(1)).findByType("RESERVATION_1HOURS_BEFORE");

        // Verify that we check for existing notifications
        verify(notificationQueueRepository, times(3))
                .existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                        eq(1L), eq(1L), anyLong(), anyList());

        // Verify that we only schedule 2 notifications (not the already scheduled one)
        verify(notificationService, times(2)).scheduleNotification(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("FCM 토큰이 없는 회원은 알림을 스케줄링하지 않는 테스트")
    void scheduleReservationNotifications_WithNoFcmToken_ShouldNotScheduleNotifications() {
        // Given
        when(testMember.getFcmToken()).thenReturn(null);

        // When
        scheduler.scheduleReservationNotifications(testReservation);

        // Then
        verify(notificationTypesRepository, never()).findByType(anyString());
        verify(notificationQueueRepository, never())
                .existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                        anyLong(), anyLong(), anyLong(), anyList());
        verify(notificationService, never()).scheduleNotification(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("예약 시간이 null인 경우 알림을 스케줄링하지 않는 테스트")
    void scheduleReservationNotifications_WithNullReservationDateTime_ShouldNotScheduleNotifications() {
        // Given
        when(testSlot.getDateTime()).thenReturn(null);

        // When
        scheduler.scheduleReservationNotifications(testReservation);

        // Then
        verify(notificationTypesRepository, never()).findByType(anyString());
        verify(notificationQueueRepository, never())
                .existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                        anyLong(), anyLong(), anyLong(), anyList());
        verify(notificationService, never()).scheduleNotification(any(NotificationRequest.class));
    }
}
