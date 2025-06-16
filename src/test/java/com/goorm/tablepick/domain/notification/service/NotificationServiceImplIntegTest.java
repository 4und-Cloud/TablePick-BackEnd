package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import com.goorm.tablepick.domain.member.enums.Gender;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationLog;
import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationLogRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

@ActiveProfiles("test")
@SpringBootTest
class NotificationServiceImplIntegTest {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationQueueRepository notificationQueueRepository;
    
    @Autowired
    private NotificationLogRepository notificationLogRepository;
    
    @Autowired
    private NotificationTypesRepository notificationTypesRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @MockBean
    private FCMService fcmService;
    
    @MockBean
    private FCMTokenService fcmTokenService;
    
    private Member member;
    private Restaurant restaurant;
    private ReservationSlot reservationSlot;
    private Reservation reservation;
    private NotificationTypes notificationType;
    
    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        notificationQueueRepository.deleteAll();
        notificationLogRepository.deleteAll();
        notificationTypesRepository.deleteAll();
        reservationRepository.deleteAll();
        reservationSlotRepository.deleteAll();
        restaurantRepository.deleteAll();
        memberRepository.deleteAll();
        
        // Member 생성
        member = Member.builder()
                .nickname("TestUser")
                .email("test@example.com")
                .gender(Gender.MALE)
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("010-1234-5678")
                .profileImage("profile.jpg")
                .isMemberDeleted(false)
                .roles(AccountRole.USER)
                .provider("GOOGLE")
                .providerId("prov_123")
                .fcmToken("valid-fcm-token")
                .build();
        member = memberRepository.save(member);
        
        // Restaurant 생성
        restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .restaurantPhoneNumber("010-1234-5678")
                .address("Test Address")
                .xcoordinate(37.5665)
                .ycoordinate(126.9780)
                .maxCapacity(50L)
                .build();
        restaurant = restaurantRepository.save(restaurant);
        
        // ReservationSlot 생성
        reservationSlot = ReservationSlot.builder()
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(12, 0))
                .count(10L)
                .restaurant(restaurant)
                .build();
        reservationSlot = reservationSlotRepository.save(reservationSlot);
        
        // Reservation 생성
        reservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(member)
                .reservationSlot(reservationSlot)
                .restaurant(restaurant)
                .paymentId("pay_123")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        reservation = reservationRepository.save(reservation);
        
        // NotificationTypes 생성
        notificationType = NotificationTypes.builder()
                .type("RESERVATION_CONFIRM")
                .title("예약 확인")
                .body("예약이 완료되었습니다.")
                .build();
        notificationType = notificationTypesRepository.save(notificationType);
    }
    
    @AfterEach
    void tearDown() {
        notificationQueueRepository.deleteAll();
        notificationLogRepository.deleteAll();
        notificationTypesRepository.deleteAll();
        reservationRepository.deleteAll();
        reservationSlotRepository.deleteAll();
        restaurantRepository.deleteAll();
        memberRepository.deleteAll();
        reset(fcmService, fcmTokenService);
    }
    
    @DisplayName("유효한 요청으로 알림을 성공적으로 예약하고 저장한다")
    @Test
    void scheduleNotification_success() {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .memberId(member.getId())
                .notificationTypeId(notificationType.getId())
                .reservationId(reservation.getId())
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();
        
        // when
        NotificationResponse response = notificationService.scheduleNotification(request);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.PENDING.name());
        assertThat(response.getMemberId()).isEqualTo(member.getId());
        assertThat(response.getReservationId()).isEqualTo(reservation.getId());
        assertThat(response.getType()).isEqualTo("RESERVATION_CONFIRM");
        
        NotificationQueue savedQueue = notificationQueueRepository.findById(response.getId()).orElseThrow();
        assertThat(savedQueue.getStatus()).isEqualTo(NotificationStatus.PENDING.name());
        assertThat(savedQueue.getScheduledAt()).isEqualTo(request.getScheduledAt());
    }
    
    @DisplayName("알림 타입이 존재하지 않을 때 예외를 던진다")
    @Test
    void scheduleNotification_typeNotFound() {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .memberId(member.getId())
                .notificationTypeId(999L)
                .reservationId(reservation.getId())
                .scheduledAt(LocalDateTime.now())
                .build();
        
        // when & then
        NotificationException exception = assertThrows(NotificationException.class,
                () -> notificationService.scheduleNotification(request));
        assertThat(exception.getMessage()).isEqualTo("Notification type not found");
        assertThat(exception.getErrorCode()).isEqualTo("TYPE_NOT_FOUND");
    }
    
    @DisplayName("유효한 알림을 성공적으로 처리하고 로그를 저장한다")
    @Test
    void processNotification_success() {
        // given
        NotificationQueue queue = NotificationQueue.builder()
                .notificationTypes(notificationType)
                .memberId(member.getId())
                .reservationId(reservation.getId())
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();
        NotificationQueue savedQueue = notificationQueueRepository.save(queue);
        
        when(fcmTokenService.getFcmToken(member.getId())).thenReturn("valid-fcm-token");
        when(fcmService.sendMessage(anyString(), anyString(), anyString(), any())).thenReturn("message_id_12345");
        
        // when
        notificationService.processNotificationWithNewTransaction(savedQueue);
        
        // then
        NotificationQueue updatedQueue = notificationQueueRepository.findById(savedQueue.getId()).orElseThrow();
        assertThat(updatedQueue.getStatus()).isEqualTo(NotificationStatus.SENT.name());
        
        List<NotificationLog> logs = notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(
                savedQueue.getId());
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getIsSuccess()).isTrue();
        assertThat(logs.get(0).getErrorMessage()).isNull();
        
        verify(fcmService, times(1)).sendMessage(anyString(), anyString(), anyString(), any());
    }
    
    @DisplayName("FCM 토큰이 없으면 알림이 실패로 처리되고 로그가 저장된다")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void processNotification_noFcmToken(CapturedOutput capturedOutput) {
        // given
        member.updateFcmToken(null);
        memberRepository.save(member);
        
        NotificationQueue queue = NotificationQueue.builder()
                .notificationTypes(notificationType)
                .memberId(member.getId())
                .reservationId(reservation.getId())
                .scheduledAt(LocalDateTime.now().minusSeconds(1)) // 과거 시간으로 설정
                .status(NotificationStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();
        NotificationQueue savedQueue = notificationQueueRepository.save(queue);
        
        when(fcmTokenService.getFcmToken(member.getId())).thenReturn(null);
        
        // when
        notificationService.processNotificationWithNewTransaction(savedQueue);
        
        // then
        NotificationQueue updatedQueue = notificationQueueRepository.findById(savedQueue.getId()).orElseThrow();
        assertThat(updatedQueue.getStatus()).isEqualTo(NotificationStatus.FAILED.name());
        
        List<NotificationLog> logs = notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(
                savedQueue.getId());
//        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getIsSuccess()).isFalse();
        assertThat(logs.get(0).getErrorMessage()).contains("FCM token not found");
        
        assertThat(capturedOutput.getOut()).contains("FCM token not found for member ID: " + member.getId());
        
        verify(fcmService, times(0)).sendMessage(anyString(), anyString(), anyString(), any());
    }
    
    @DisplayName("유효한 알림 ID로 알림 상태를 조회한다")
    @Test
    void getNotificationStatus_success() {
        // given
        NotificationQueue queue = NotificationQueue.builder()
                .notificationTypes(notificationType)
                .memberId(member.getId())
                .reservationId(reservation.getId())
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.SENT.name())
                .createdAt(LocalDateTime.now())
                .build();
        NotificationQueue savedQueue = notificationQueueRepository.save(queue);
        
        // when
        NotificationResponse response = notificationService.getNotificationStatus(savedQueue.getId());
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(savedQueue.getId());
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT.name());
        assertThat(response.getMemberId()).isEqualTo(member.getId());
        assertThat(response.getType()).isEqualTo("RESERVATION_CONFIRM");
    }
    
    @DisplayName("알림 ID가 존재하지 않을 때 예외를 던진다")
    @Test
    void getNotificationStatus_notFound() {
        // given
        Long invalidId = 999L;
        
        // when & then
        NotificationException exception = assertThrows(NotificationException.class,
                () -> notificationService.getNotificationStatus(invalidId));
        assertThat(exception.getMessage()).isEqualTo("Notification not found");
        assertThat(exception.getErrorCode()).isEqualTo("NOTIFICATION_NOT_FOUND");
    }
    
    @DisplayName("회원의 알림 목록을 상태 필터링하여 조회한다")
    @Test
    void getMemberNotifications_withStatus() {
        // given
        NotificationQueue queue = NotificationQueue.builder()
                .notificationTypes(notificationType)
                .memberId(member.getId())
                .reservationId(reservation.getId())
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.SENT.name())
                .createdAt(LocalDateTime.now())
                .build();
        NotificationQueue savedQueue = notificationQueueRepository.save(queue);
        
        // when
        List<NotificationResponse> responses = notificationService.getMemberNotifications(member.getId(),
                NotificationStatus.SENT.name());
        
        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(savedQueue.getId());
        assertThat(responses.get(0).getStatus()).isEqualTo(NotificationStatus.SENT.name());
        assertThat(responses.get(0).getMemberId()).isEqualTo(member.getId());
    }
    
    @DisplayName("PENDING 상태의 알림 큐를 처리하여 SENT 또는 FAILED 상태로 업데이트한다")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void processNotificationQueue_successAndFailure(CapturedOutput capturedOutput) {
        // given
        // 성공 알림
        NotificationQueue successQueue = NotificationQueue.builder()
                .notificationTypes(notificationType)
                .memberId(member.getId())
                .reservationId(reservation.getId())
                .scheduledAt(LocalDateTime.now().minusSeconds(1))
                .status(NotificationStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();
        notificationQueueRepository.save(successQueue);
        
        // 실패 알림 (FCM 토큰 없음)
        Member noFcmMember = Member.builder()
                .nickname("NoFcmUser")
                .email("nofcm@example.com")
                .gender(Gender.FEMALE)
                .birthdate(LocalDate.of(1995, 1, 1))
                .phoneNumber("010-9876-5432")
                .isMemberDeleted(false)
                .roles(AccountRole.USER)
                .provider("GOOGLE")
                .providerId("prov_456")
                .fcmToken(null)
                .build();
        noFcmMember = memberRepository.save(noFcmMember);
        
        Reservation noFcmReservation = Reservation.builder()
                .partySize(2L)
                .reservationStatus(ReservationStatus.PENDING)
                .member(noFcmMember)
                .reservationSlot(reservationSlot)
                .restaurant(restaurant)
                .paymentId("pay_456")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        noFcmReservation = reservationRepository.save(noFcmReservation);
        
        NotificationQueue failureQueue = NotificationQueue.builder()
                .notificationTypes(notificationType)
                .memberId(noFcmMember.getId())
                .reservationId(noFcmReservation.getId())
                .scheduledAt(LocalDateTime.now().minusSeconds(1))
                .status(NotificationStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();
        notificationQueueRepository.save(failureQueue);
        
        when(fcmTokenService.getFcmToken(member.getId())).thenReturn("valid-fcm-token");
        when(fcmTokenService.getFcmToken(noFcmMember.getId())).thenReturn(null);
        when(fcmService.sendMessage(anyString(), anyString(), anyString(), any())).thenReturn("message_id_12345");
        
        // when
        notificationService.processNotificationQueue();
        
        // then
        // 성공 알림 확인
        NotificationQueue updatedSuccessQueue = notificationQueueRepository.findById(successQueue.getId())
                .orElseThrow();
        assertThat(updatedSuccessQueue.getStatus()).isEqualTo(NotificationStatus.SENT.name());
        
        List<NotificationLog> successLogs = notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(
                successQueue.getId());
        assertThat(successLogs).hasSize(1);
        assertThat(successLogs.get(0).getIsSuccess()).isTrue();
        assertThat(successLogs.get(0).getErrorMessage()).isNull();
        
        // 실패 알림 확인
        NotificationQueue updatedFailureQueue = notificationQueueRepository.findById(failureQueue.getId())
                .orElseThrow();
        assertThat(updatedFailureQueue.getStatus()).isEqualTo(NotificationStatus.FAILED.name());
        
        List<NotificationLog> failureLogs = notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(
                failureQueue.getId());
        assertThat(failureLogs).hasSize(1);
        assertThat(failureLogs.get(0).getIsSuccess()).isFalse();
        assertThat(failureLogs.get(0).getErrorMessage()).contains("FCM token not found");
        
        assertThat(capturedOutput.getOut()).contains("FCM token not found for member ID: " + noFcmMember.getId());
        
        verify(fcmService, times(1)).sendMessage(anyString(), anyString(), anyString(), any());
    }
}