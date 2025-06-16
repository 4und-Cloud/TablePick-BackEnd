package com.goorm.tablepick.domain.notification.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import com.goorm.tablepick.domain.member.enums.Gender;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.notification.dto.request.FCMTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class NotificationControllerIntegTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MemberRepository memberRepository;
    
    private Member member;
    
    @Setter
    @Getter
    @Autowired
    private NotificationTypesRepository notificationTypesRepository;
    
    private NotificationRequest notificationRequest;
    private FCMTokenRequest fcmTokenRequest;
    
    @Setter
    @Getter
    private NotificationTypes notificationType;
    
    @Setter
    @Getter
    private NotificationResponse notificationResponse;
    
    @BeforeEach
    void setUp() {
        notificationRequest = NotificationRequest.builder()
                .memberId(1L)
                .notificationTypeId(1L)
                .scheduledAt(LocalDateTime.now())
                .build();
        
        notificationResponse = NotificationResponse.builder()
                .id(1L)
                .memberId(1L)
                .scheduledAt(LocalDateTime.now())
                .status("PENDING")
                .build();
        
        fcmTokenRequest = new FCMTokenRequest("test-token");
        
        notificationType = NotificationTypes.builder()
                .id(1L)
                .type("TEST_NOTIFICATION")
                .title("테스트 알림")
                .body("테스트 알림 내용")
                .url("https://tablepick.com")
                .build();
        
        member = Member.builder()
                .nickname("testuser")
                .email("testuser@example.com")
                .gender(Gender.MALE)   // Gender enum 값 예시
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("010-1234-5678")
                .profileImage(null)
                .isMemberDeleted(false)
                .memberTags(new ArrayList<>())
                .roles(AccountRole.USER)  // AccountRole enum 값 예시
                .provider("local")
                .providerId("testproviderid")
                .fcmToken(null)
                .build();
        
        member = memberRepository.save(member);
    }
    
    @Test
    @DisplayName("유효한 알림 요청으로 알림 예약이 성공적으로 처리된다")
    void scheduleNotification_withValidRequest_succeeds() throws Exception {
        // given 준비
        // 알림 타입이 이미 등록되어 있다고 가정
        
        // when 실행
        ResultActions result = mockMvc.perform(post("/api/notifications/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificationRequest))); //  알림 예약 API 호출
        
        // then 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(notificationRequest.getMemberId()));
//                .andExpect(jsonPath("$.notificationTypeId").value(notificationRequest.getNotificationTypeId()));
    }
    
    @Test
    @DisplayName("존재하지 않는 알림 ID로 조회 시 NULL을 반환한다")
    void getNotificationStatus_withInvalidId_returnsNotFound() throws Exception {
        // given 준비
        Long invalidId = 999999L;
        
        // when 실행
        ResultActions result = mockMvc.perform(get("/api/notifications/" + invalidId)); // 알림 상태 조회 API 호출
        
        // then 검증
        result.andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("유효한 회원 ID로 회원 알림 목록 조회가 성공적으로 처리된다")
    void getMemberNotifications_withValidMemberId_succeeds() throws Exception {
        // given 준비
        Long memberId = 1L;
        
        // when 실행
        ResultActions result = mockMvc.perform(get("/api/notifications/member/" + memberId)
                .param("status", "SENT")); // 회원 알림 목록 조회 API 호출
        
        // then 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    @DisplayName("유효한 FCM 토큰으로 회원의 FCM 토큰 업데이트가 성공적으로 처리된다")
    void updateFcmToken_withValidToken_succeeds() throws Exception {
        // given 준비
        Long memberId = member.getId();
        
        // when 실행
        ResultActions result = mockMvc.perform(patch("/api/notifications/fcm-token")
                .param("memberId", memberId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fcmTokenRequest)));
        
        // then 검증
        result.andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("유효한 회원 ID로 FCM 토큰을 NULL로 변경이 성공적으로 처리된다")
    void removeFcmToken_withValidMemberId_succeeds() throws Exception {
        // given 준비
        Long memberId = member.getId();
        
        // when 실행
        ResultActions result = mockMvc.perform(patch("/api/notifications/fcm-token/remove")
                .param("memberId", memberId.toString()));
        
        // then 검증
        result.andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("유효한 회원 ID와 알림 타입 ID로 테스트 알림 전송이 성공적으로 처리된다")
    void sendTestNotification_withValidIds_succeeds() throws Exception {
        // given 준비
        Long memberId = 1L;
        Long notificationTypeId = 1L;
        
        // when 실행
        ResultActions result = mockMvc.perform(post("/api/notifications/test")
                .param("memberId", memberId.toString())
                .param("notificationTypeId", notificationTypeId.toString()));
        
        // then 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId));
//                .andExpect(jsonPath("$.notificationTypeId").value(notificationTypeId));
    }
    
    @Test
    @DisplayName("알림 타입 목록 조회가 성공적으로 처리된다")
    void getNotificationTypes_succeeds() throws Exception {
        // given 준비
        // 데이터베이스에 알림 타입이 존재한다고 가정
        
        // when 실행
        ResultActions result = mockMvc.perform(get("/api/notifications/notification-types"));
        
        // then 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
}