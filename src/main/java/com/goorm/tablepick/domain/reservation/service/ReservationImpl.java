package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import com.goorm.tablepick.domain.notification.service.ReservationNotificationScheduler;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantOperatingHourRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantOperatingHourRepository restaurantOperatingHourRepository;
    private final ReservationNotificationScheduler notificationScheduler;
    private final NotificationService notificationService;
    private final NotificationTypesRepository notificationTypesRepository;


    @Override
    @Transactional
    public void createReservation(String username, ReservationRequestDto request) {
        // 식당 검증
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        // 멤버 검증 (임시 로그인용)
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 예약 가능 시간 조회
        ReservationSlot reservationSlot = reservationSlotRepository.findByRestaurantIdAndDateAndTime(
                        request.getRestaurantId(), request.getReservationDate(), request.getReservationTime())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));

        // 중복 예약 검증
        List<Reservation> reservations = reservationRepository.findByReservationSlot(reservationSlot);

        boolean hasDuplicate = reservations.stream()
                .anyMatch(r -> r.getMember().equals(member));

        if (hasDuplicate) {
            throw new ReservationException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }

        // 예약 총 횟수가 max_capacity 미만인지 검증
        Long count = reservationSlot.getCount();
        Long maxCapacity = restaurant.getMaxCapacity();

        if (count >= maxCapacity) {
            throw new ReservationException(ReservationErrorCode.EXCEED_RESERVATION_LIMIT);
        }

        // 예약 시간 count 증가
        reservationSlot.setCount(reservationSlot.getCount() + 1);
        reservationSlotRepository.save(reservationSlot);

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .member(member)
                .reservationSlot(reservationSlot)
                .partySize(request.getPartySize())
                .reservationStatus(ReservationStatus.CONFIRMED)
                .restaurant(restaurant) // 레스토랑 정보 추가
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        
        // 예약 완료 알림 및 예약 시간 기준 알림 예약
        try {
            // 예약 완료 알림 즉시 전송
            scheduleReservationCompletedNotification(savedReservation);
            
            // 예약 시간 기준 알림 예약 (1일 전, 3시간 전, 1시간 전, 3시간 후)
            scheduleAllReservationNotifications(savedReservation);
            
            log.info("예약 ID: {}에 대한 모든 알림이 성공적으로 예약되었습니다.", savedReservation.getId());
        } catch (Exception e) {
            log.error("예약 알림 스케줄링 중 오류 발생: {}", e.getMessage(), e);
            // 알림 예약 실패해도 예약 자체는 성공으로 처리
        }
    }

    @Override
    @Transactional
    public void cancelReservation(String username, Long reservationId) {
        // 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND));

        // 멤버 검증 (임시 로그인용)
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!reservation.getMember().equals(member)) {
            throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_CANCEL);
        }

        // 이미 취소된 예약인지 확인
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationException(ReservationErrorCode.ALREADY_CANCELLED);
        }

        // 예약 상태 변경
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        ReservationSlot reservationSlot = reservationSlotRepository.findById(reservation.getReservationSlot().getId())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));

        // 예약 슬롯 count 감소 (최소 0)
        long currentCount = reservationSlot.getCount();
        reservationSlot.setCount(Math.max(0, currentCount - 1));
        reservationSlotRepository.save(reservationSlot);
    }

    @Override
    @Transactional
    public List<LocalTime> getAvailableReservationTimes(Long restaurantId, LocalDate date) {
        //해당 식당 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        //해당 날짜의 예약 슬롯 확인
        List<ReservationSlot> reservationTimes = reservationSlotRepository.findAvailableTimes(restaurantId, date);

        //LocalTime만 추출
        List<LocalTime> availableTimes = reservationTimes.stream()
                .map(ReservationSlot::getTime)
                .toList();

        return availableTimes;
    }
    
    /**
     * 예약 완료 알림을 즉시 전송합니다.
     * 
     * @param reservation 예약 정보
     */
    private void scheduleReservationCompletedNotification(Reservation reservation) {
        try {
            // 예약 완료 알림 타입 조회
            notificationTypesRepository.findByType(NotificationTypes.RESERVATION_COMPLETED)
                    .ifPresent(type -> {
                        // 알림 요청 생성 (현재 시간으로 설정)
                        com.goorm.tablepick.domain.notification.dto.request.NotificationRequest request = 
                            com.goorm.tablepick.domain.notification.dto.request.NotificationRequest.builder()
                                .memberId(reservation.getMember().getId())
                                .notificationTypeId(type.getId())
                                .reservationId(reservation.getId())
                                .scheduledAt(LocalDateTime.now()) // 즉시 실행
                                .build();
                        
                        // 알림 예약
                        notificationService.scheduleNotification(request);
                        log.info("예약 완료 알림이 성공적으로 예약되었습니다. 예약 ID: {}", reservation.getId());
                    });
        } catch (Exception e) {
            log.error("예약 완료 알림 예약 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 예약 시간 기준으로 모든 알림을 예약합니다.
     * 
     * @param reservation 예약 정보
     */
    private void scheduleAllReservationNotifications(Reservation reservation) {
        try {
            // 예약 시간 기준 알림 예약 (1일 전, 3시간 전, 1시간 전, 3시간 후)
            LocalDateTime reservationDateTime = reservation.getReservationDateTime();
            if (reservationDateTime == null) {
                log.warn("예약 시간 정보가 없습니다. 알림을 예약할 수 없습니다. 예약 ID: {}", reservation.getId());
                return;
            }
            
            Long memberId = reservation.getMember().getId();
            Long reservationId = reservation.getId();
            
            // 1일 전 알림 예약
            scheduleNotificationByType(memberId, reservationId, NotificationTypes.RESERVATION_1DAY_BEFORE, 
                    reservationDateTime.minusDays(1));
            
            // 3시간 전 알림 예약
            scheduleNotificationByType(memberId, reservationId, NotificationTypes.RESERVATION_3HOURS_BEFORE, 
                    reservationDateTime.minusHours(3));
            
            // 1시간 전 알림 예약
            scheduleNotificationByType(memberId, reservationId, NotificationTypes.RESERVATION_1HOUR_BEFORE, 
                    reservationDateTime.minusHours(1));
            
            // 3시간 후 알림 예약
            scheduleNotificationByType(memberId, reservationId, NotificationTypes.RESERVATION_3HOURS_AFTER, 
                    reservationDateTime.plusHours(3));
            
            log.info("예약 ID: {}에 대한 모든 시간 기준 알림이 성공적으로 예약되었습니다.", reservation.getId());
        } catch (Exception e) {
            log.error("예약 시간 기준 알림 예약 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 알림 타입에 대한 알림을 예약합니다.
     * 
     * @param memberId 회원 ID
     * @param reservationId 예약 ID
     * @param notificationTypeStr 알림 타입 문자열
     * @param scheduledAt 예약 시간
     */
    private void scheduleNotificationByType(Long memberId, Long reservationId, String notificationTypeStr, 
            LocalDateTime scheduledAt) {
        try {
            notificationTypesRepository.findByType(notificationTypeStr)
                    .ifPresent(type -> {
                        // 알림 요청 생성
                        com.goorm.tablepick.domain.notification.dto.request.NotificationRequest request = 
                            com.goorm.tablepick.domain.notification.dto.request.NotificationRequest.builder()
                                .memberId(memberId)
                                .notificationTypeId(type.getId())
                                .reservationId(reservationId)
                                .scheduledAt(scheduledAt)
                                .build();
                        
                        // 알림 예약
                        notificationService.scheduleNotification(request);
                        log.info("알림이 성공적으로 예약되었습니다. 예약 ID: {}, 알림 타입: {}, 예약 시간: {}", 
                                reservationId, notificationTypeStr, scheduledAt);
                    });
        } catch (Exception e) {
            log.error("알림 예약 중 오류 발생: {}, 알림 타입: {}", e.getMessage(), notificationTypeStr, e);
        }
    }
}
