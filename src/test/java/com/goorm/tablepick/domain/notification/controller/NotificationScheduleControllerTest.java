package com.goorm.tablepick.domain.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.service.ReservationNotificationScheduler;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotificationScheduleControllerTest {
    
    @Mock
    private ReservationNotificationScheduler scheduler;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @InjectMocks
    private NotificationScheduleController controller;
    
    @Test
    @DisplayName("일일 알림 스케줄링 API를 호출하면 200 OK와 성공 메시지를 반환한다.")
    void runDailyScheduling_Success() {
        // given 준비
        doNothing().when(scheduler).scheduleNotificationsDaily();
        
        // when 실행
        ResponseEntity<Map<String, String>> response = controller.runDailyScheduling();
        
        // then 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("message")).isEqualTo("일일 알림 스케줄링 실행 성공 ^^");
        
        verify(scheduler).scheduleNotificationsDaily();
    }
    
    @Test
    @DisplayName("존재하지 않는 예약 ID로 개별 알림 스케줄링 API를 호출하면 404 NOT_FOUND 예외를 발생시킨다.")
    void scheduleForReservation_Success() {
        // given 준비
        Long reservationId = 1L;
        Reservation mockReservation = createMockReservation(reservationId);
        
        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.of(mockReservation));
        doNothing().when(scheduler).scheduleReservationNotifications(any(Reservation.class));
        
        // when 실행
        ResponseEntity<Map<String, String>> response = controller.scheduleForReservation(reservationId);
        
        // then 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("message"))
                .isEqualTo("지정된 예약 ID에 대한 알림을 스케줄링 진행 중~~: " + reservationId);
        
        verify(reservationRepository).findById(reservationId);
        verify(scheduler).scheduleReservationNotifications(mockReservation);
    }
    
    @Test
    @DisplayName("예약이 없는 예약을 스케줄링하면 404 에러와 에러 메시지를 반환한다.")
    void scheduleForReservation_ReservationNotFound() {
        // given 준비
        Long reservationId = 999L;
        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.empty());
        
        // when & then 실행 및 검증
        // ResponseStatusException을 사용하는 경우
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.scheduleForReservation(reservationId));
        
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Reservation not found: " + reservationId);
        
        verify(reservationRepository).findById(reservationId);
    }
    
    private Reservation createMockReservation(Long id) {
        Member mockMember = Member.builder()
                .nickname("테스트 사용자")
                .email("test@test.com")
                .build();
        
        Restaurant mockRestaurant = Restaurant.builder()
                .name("테스트 레스토랑")
                .address("서울시 강남구")
                .build();
        
        ReservationSlot mockSlot = ReservationSlot.builder()
                .date(LocalDateTime.now().plusDays(1).toLocalDate())
                .time(LocalDateTime.now().plusDays(1).toLocalTime())
                .count(10L)
                .restaurant(mockRestaurant)
                .build();
        
        return Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .member(mockMember)
                .reservationSlot(mockSlot)
                .restaurant(mockRestaurant)
                .paymentId("test-payment-id")
                .paymentStatus("COMPLETED")
                .createdAt(LocalDateTime.now())
                .build();
    }
}