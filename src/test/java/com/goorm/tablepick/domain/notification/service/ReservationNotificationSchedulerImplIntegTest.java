package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
@Transactional
class ReservationNotificationSchedulerImplIntegTest {
    
    @Autowired
    private ReservationNotificationScheduler scheduler;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    
    @Autowired
    private NotificationTypesRepository notificationTypesRepository;
    
    @MockBean
    private NotificationQueueRepository notificationQueueRepository;
    
    @MockBean
    private NotificationService notificationService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        // 필수 NotificationTypes 데이터 삽입
        if (!notificationTypesRepository.existsByType(NotificationTypes.RESERVATION_COMPLETED)) {
            NotificationTypes type = NotificationTypes.builder()
                    .type(NotificationTypes.RESERVATION_COMPLETED)
                    .title("Reservation Confirmation")
                    .body("Your reservation at {restaurantName} is confirmed for {date} at {time}.")
                    .build();
            notificationTypesRepository.save(type);
        }
    }
    
    @AfterEach
    void tearDown() {
        reset(notificationService);
    }
    
    @DisplayName("2일 이내 예약에 대해 알림을 성공적으로 스케줄링한다.")
    @Test
    void scheduleNotificationsDailySuccessfully() {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .maxCapacity(50L)
                .build();
        entityManager.persist(restaurant);
        
        Member member = Member.builder()
                .email("test@example.com")
                .build();
        entityManager.persist(member);
        
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(12, 0))
                .count(10L)
                .restaurant(restaurant)
                .build();
        reservationSlotRepository.save(slot);
        
        Reservation reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(member)
                .reservationSlot(slot)
                .restaurant(restaurant)
                .build();
        reservationRepository.save(reservation);
        
        when(notificationQueueRepository.existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
                nullable(Long.class), nullable(Long.class), nullable(Long.class), anyList())).thenReturn(false);
        
        // when
        scheduler.scheduleNotificationsDaily();
        
        // then
        verify(notificationService, times(5)).scheduleNotification(any());
    }
    
    @DisplayName("예약에 멤버 또는 슬롯이 없으면 알림 스케줄링을 건너뛴다.")
    @Test
    void skipSchedulingWhenMemberOrSlotIsNull(CapturedOutput capturedOutput) {
        // given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .maxCapacity(50L)
                .build();
        entityManager.persist(restaurant);
        
        // Member와 ReservationSlot이 null인 경우, 저장 시도하지 않음
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
        entityManager.persist(restaurant);
        
        Member member = Member.builder()
                .email("test@example.com")
                .build();
        entityManager.persist(member);
        
        ReservationSlot slot = ReservationSlot.builder()
                .date(null)
                .time(null)
                .count(10L)
                .restaurant(restaurant)
                .build();
        reservationSlotRepository.save(slot);
        
        Reservation reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(member)
                .reservationSlot(slot)
                .restaurant(restaurant)
                .build();
        reservationRepository.save(reservation);
        
        // when
        scheduler.scheduleReservationNotifications(reservation);
        
        // then
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("reservation date/time is null");
    }
    
    @DisplayName("예약이 없으면 알림 스케줄링이 진행되지 않는다.")
    @Test
    void skipSchedulingWhenNoReservationsFound(CapturedOutput capturedOutput) {
        // given
        // No reservations saved in repository
        
        // when
        scheduler.scheduleNotificationsDaily();
        
        // then
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("Found 0 upcoming reservations");
    }
}