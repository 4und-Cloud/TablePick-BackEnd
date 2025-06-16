package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class ReservationNotificationSchedulerImplTest {
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private NotificationTypesRepository notificationTypesRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private NotificationQueueRepository notificationQueueRepository;
    
    @InjectMocks
    private ReservationNotificationSchedulerImpl scheduler;
    
    @AfterEach
    void tearDown() {
        reset(reservationRepository, notificationTypesRepository, notificationService, notificationQueueRepository);
    }
    
    @DisplayName("2일 이내 예약에 대해 알림을 성공적으로 스케줄링한다.")
    @Test
    void scheduleNotificationsDailySuccessfully() throws NoSuchFieldException, IllegalAccessException {
        // given
        LocalDate today = LocalDate.now();
        LocalDate twoDaysLater = today.plusDays(2);
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .maxCapacity(50L)
                .build();
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(12, 0))
                .count(10L)
                .restaurant(restaurant)
                .build();
        Reservation reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(Member.builder().id(1L).build())
                .reservationSlot(slot)
                .restaurant(restaurant)
                .build();
        
        // Reservation id 설정 (리플렉션 사용)
        setId(reservation, 1L);
        
        when(reservationRepository.findPendingReservationsBetweenDates(today, twoDaysLater))
                .thenReturn(List.of(reservation));
        when(notificationTypesRepository.findByType(anyString()))
                .thenReturn(Optional.of(NotificationTypes.builder().id(1L).type("TEST_TYPE").build()));
        when(notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                anyLong(), nullable(Long.class), anyLong(), anyList())).thenReturn(false);
        
        // when
        scheduler.scheduleNotificationsDaily();
        
        // then
        verify(reservationRepository, times(1)).findPendingReservationsBetweenDates(today, twoDaysLater);
        verify(notificationTypesRepository, times(5)).findByType(anyString());
        verify(notificationService, times(5)).scheduleNotification(any(NotificationRequest.class));
    }
    
    @DisplayName("예약에 멤버 또는 슬롯이 없으면 알림 스케줄링을 건너뛴다.")
    @Test
    void skipSchedulingWhenMemberOrSlotIsNull(CapturedOutput capturedOutput) {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .maxCapacity(50L)
                .build();
        Reservation reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(null)
                .reservationSlot(null)
                .restaurant(restaurant)
                .build();
        
        // when
        scheduler.scheduleReservationNotifications(reservation);
        
        // then
        verify(notificationTypesRepository, never()).findByType(any());
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("member or reservation slot is null");
    }
    
    @DisplayName("예약 날짜/시간이 null이면 알림 스케줄링을 건너뛴다.")
    @Test
    void skipSchedulingWhenReservationDateTimeIsNull(CapturedOutput capturedOutput) {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .maxCapacity(50L)
                .build();
        ReservationSlot slot = ReservationSlot.builder()
                .date(null)
                .time(null)
                .count(10L)
                .restaurant(restaurant)
                .build();
        Reservation reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(Member.builder().id(1L).build())
                .reservationSlot(slot)
                .restaurant(restaurant)
                .build();
        
        // when
        scheduler.scheduleReservationNotifications(reservation);
        
        // then
        verify(notificationTypesRepository, never()).findByType(any());
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("reservation date/time is null");
    }
    
    @DisplayName("알림 타입이 없으면 알림을 스케줄링하지 않는다.")
    @Test
    void skipSchedulingWhenNotificationTypeNotFound(CapturedOutput capturedOutput)
            throws NoSuchFieldException, IllegalAccessException {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .maxCapacity(50L)
                .build();
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(12, 0))
                .count(10L)
                .restaurant(restaurant)
                .build();
        Reservation reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(Member.builder().id(1L).build())
                .reservationSlot(slot)
                .restaurant(restaurant)
                .build();
        
        // Reservation id 설정
        setId(reservation, 1L);
        
        when(notificationTypesRepository.findByType(anyString())).thenReturn(Optional.empty());
        
        // when
        scheduler.scheduleReservationNotifications(reservation);
        
        // then
        verify(notificationTypesRepository, times(5)).findByType(anyString());
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("Notification type not found");
    }
    
    @DisplayName("이미 스케줄된 알림은 중복으로 생성되지 않는다.")
    @Test
    void skipSchedulingWhenNotificationAlreadyScheduled() throws NoSuchFieldException, IllegalAccessException {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .maxCapacity(50L)
                .build();
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(12, 0))
                .count(10L)
                .restaurant(restaurant)
                .build();
        Reservation reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(Member.builder().id(1L).build())
                .reservationSlot(slot)
                .restaurant(restaurant)
                .build();
        
        // Reservation id 설정
        setId(reservation, 1L);
        
        when(notificationTypesRepository.findByType(anyString()))
                .thenReturn(Optional.of(NotificationTypes.builder().id(1L).type("TEST_TYPE").build()));
        when(notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                anyLong(), nullable(Long.class), anyLong(), anyList())).thenReturn(true);
        
        // when
        scheduler.scheduleReservationNotifications(reservation);
        
        // then
        verify(notificationQueueRepository,
                times(5)).existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                anyLong(), nullable(Long.class), anyLong(), anyList());
        verify(notificationService, never()).scheduleNotification(any());
    }
    
    @DisplayName("예약이 없으면 알림 스케줄링이 진행되지 않는다.")
    @Test
    void skipSchedulingWhenNoReservationsFound(CapturedOutput capturedOutput) {
        // given
        LocalDate today = LocalDate.now();
        LocalDate twoDaysLater = today.plusDays(2);
        when(reservationRepository.findPendingReservationsBetweenDates(today, twoDaysLater))
                .thenReturn(Collections.emptyList());
        
        // when
        scheduler.scheduleNotificationsDaily();
        
        // then
        verify(reservationRepository, times(1)).findPendingReservationsBetweenDates(today, twoDaysLater);
        verify(notificationTypesRepository, never()).findByType(any());
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("Found 0 upcoming reservations");
    }
    
    // Reservation id를 설정하기 위한 헬퍼 메서드
    private void setId(Reservation reservation, Long id) throws NoSuchFieldException, IllegalAccessException {
        Field idField = Reservation.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(reservation, id);
    }
}