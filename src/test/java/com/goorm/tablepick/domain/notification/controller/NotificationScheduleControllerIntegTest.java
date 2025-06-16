package com.goorm.tablepick.domain.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class NotificationScheduleControllerIntegTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    
    @Test
    @DisplayName("일일 알림 스케줄링 API를 POST로 호출하면 200 OK 상태코드와 성공 응답을 반환한다.")
    void runDailyScheduling_IntegrationTest() throws Exception {
        // when & then 실행 및 검증
        mockMvc.perform(post("/api/notifications/schedule/run-daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("일일 알림 스케줄링 실행 성공 ^^"));
    }
    
    @Test
    @DisplayName("실제 데이터베이스에 저장된 예약 ID로 개별 알림 스케줄링 API를 호출하면 200 OK와 성공 메시지를 반환한다.")
    void scheduleForReservation_IntegrationTest_Success() throws Exception {
        // given - 테스트 데이터 생성 및 저장
        Reservation savedReservation = createAndSaveTestReservation();
        
        // when & then
        mockMvc.perform(post("/api/notifications/schedule/reservation/{reservationId}",
                        savedReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("지정된 예약 ID에 대한 알림을 스케줄링 진행 중~~: " + savedReservation.getId()));
    }
    
    @Test
    @DisplayName("데이터베이스에 존재하지 않는 예약 ID로 개별 알림 스케줄링 API를 호출하면 404 NOT_FOUND와 에러 메시지를 반환한다.")
    void scheduleForReservation_IntegrationTest_NotFound_DetailedCheck() throws Exception {
        // given - 존재하지 않는 예약 ID
        Long nonExistentReservationId = 888888L;
        
        // when & then
        mockMvc.perform(post("/api/notifications/schedule/reservation/{reservationId}",
                        nonExistentReservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    // HTTP 응답의 에러 메시지 검증
                    String errorMessage = result.getResponse().getErrorMessage();
                    assertThat(errorMessage).isNotNull();
                    assertThat(errorMessage).contains("Reservation not found: " + nonExistentReservationId);
                });
    }
    
    @Test
    @DisplayName("존재하지 않는 API 경로로 POST 요청을 보내면 404 NOT_FOUND를 반환한다.")
    void invalidPath_ShouldReturn404() throws Exception {
        // when & then
        mockMvc.perform(post("/api/notifications/schedule/invalid-path")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("일일 알림 스케줄링 API를 GET 메서드로 호출하면 405 METHOD_NOT_ALLOWED를 반환한다.")
    void wrongHttpMethod_ShouldReturn405() throws Exception {
        // when & then
        mockMvc.perform(get("/api/notifications/schedule/run-daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }
    
    @Test
    @DisplayName("PENDING 상태의 예약 ID로 개별 알림 스케줄링 API를 호출하면 200 OK와 성공 메시지를 반환한다.")
    void scheduleForReservation_DifferentStatus_Success() throws Exception {
        // given - 다른 상태의 예약 생성
        Reservation pendingReservation = createAndSaveTestReservationWithStatus(ReservationStatus.PENDING);
        
        // when & then
        mockMvc.perform(post("/api/notifications/schedule/reservation/{reservationId}",
                        pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("지정된 예약 ID에 대한 알림을 스케줄링 진행 중~~: " + pendingReservation.getId()));
    }
    
    // 테스트 헬퍼 메서드들
    private Reservation createAndSaveTestReservation() {
        return createAndSaveTestReservationWithStatus(ReservationStatus.CONFIRMED);
    }
    
    private Reservation createAndSaveTestReservationWithStatus(ReservationStatus status) {
        // Member 생성 및 저장
        Member member = Member.builder()
                .nickname("테스트 사용자")
                .email("test@example.com")
                .build();
        Member savedMember = memberRepository.save(member);
        
        // Restaurant 생성 및 저장
        Restaurant testRestaurant = Restaurant.builder()
                .name("테스트 레스토랑")
                .address("서울시 강남구 테스트로 123")
                .restaurantPhoneNumber("02-123-4567")
                .maxCapacity(50L)
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(testRestaurant);
        
        // ReservationSlot 생성 및 저장
        ReservationSlot testSlot = ReservationSlot.builder()
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(18, 0))
                .count(10L)
                .restaurant(savedRestaurant)
                .build();
        ReservationSlot savedSlot = reservationSlotRepository.save(testSlot);
        
        // Reservation 생성 및 저장
        Reservation testReservation = Reservation.builder()
                .partySize(4L)
                .reservationStatus(status)
                .member(savedMember)
                .reservationSlot(savedSlot)
                .restaurant(savedRestaurant)
                .paymentId("test-payment-id-" + System.currentTimeMillis())
                .paymentStatus("COMPLETED")
                .createdAt(LocalDateTime.now())
                .build();
        
        return reservationRepository.save(testReservation);
    }
}