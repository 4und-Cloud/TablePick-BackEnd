package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import com.goorm.tablepick.domain.notification.service.ReservationNotificationScheduler;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.dto.response.CreateReservationResponseDto;
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
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final NotificationTypesRepository notificationTypesRepository;
    private final ReservationNotificationScheduler reservationNotificationScheduler;

    @Override
    @Transactional
    public CreateReservationResponseDto createReservation(String username, ReservationRequestDto request) {
        // 식당 검증
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        // 멤버 검증
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 예약 가능 시간 확인
        ReservationSlot reservationSlot = reservationSlotRepository.findByRestaurantIdAndDateAndTime(
                        request.getRestaurantId(), request.getReservationDate(), request.getReservationTime())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));

        // 중복 예약 확인
        boolean hasDuplicate = reservationRepository.findByReservationSlot(reservationSlot).stream()
                .anyMatch(r -> r.getMember().equals(member) && r.getReservationStatus() == ReservationStatus.CONFIRMED);
        if (hasDuplicate) {
            throw new ReservationException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }

        // 슬롯 카운트 검증
        Long count = reservationSlot.getCount();
        Long maxCapacity = restaurant.getMaxCapacity();
        if (count >= maxCapacity) {
            throw new ReservationException(ReservationErrorCode.EXCEED_RESERVATION_LIMIT);
        }

        // 예약 생성 (PENDING)
        String paymentId = UUID.randomUUID().toString();
        Reservation reservation = Reservation.builder()
                .member(member)
                .reservationSlot(reservationSlot)
                .partySize(request.getPartySize())
                .reservationStatus(ReservationStatus.CONFIRMED)
                .restaurant(restaurant)
                .paymentId(paymentId)
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        // 슬롯 카운트 증가
        reservationSlot.setCount(count + 1);
        reservationSlotRepository.save(reservationSlot);

        // 비동기 결제 요청
        // requestPaymentAsync(paymentId, request, member, restaurant);

        // 예약 완료 알림 및 예약 시간 기준 알림 예약
//        try {
//            // 예약 완료 알림 즉시 전송
//            scheduleReservationCompletedNotification(savedReservation);
//
//            // 예약 시간 기준 알림 예약 (1일 전, 3시간 전, 1시간 전, 3시간 후)
//            scheduleAllReservationNotifications(savedReservation);
//
//            // ReservationNotificationScheduler 인터페이스 사용
//            reservationNotificationScheduler.scheduleReservationNotifications(savedReservation);
//
//            log.info("예약 ID: {}에 대한 모든 알림이 성공적으로 예약되었습니다.", savedReservation.getId());
//        } catch (Exception e) {
//            log.error("예약 알림 스케줄링 중 오류 발생: {}", e.getMessage(), e);
//            // 알림 예약 실패해도 예약 자체는 성공으로 처리
//        }

        CreateReservationResponseDto dto = CreateReservationResponseDto.builder()
                .reservationId(savedReservation.getId())
                .build();

        return dto;
    }

//    @Async
//    public void requestPaymentAsync(String paymentId, ReservationRequestDto request, Member member,
//                                    Restaurant restaurant) {
//        PaymentRequestDto paymentRequest = PaymentRequestDto.builder()
//                .paymentId(paymentId)
//                .restaurantId(request.getRestaurantId())
//                .memberId(member.getId())
//                .amount(calculateAmount(request.getPartySize(), restaurant))
//                .status("REQUEST")
//                .build();
//
//        try {
//            PaymentResultDto result = restTemplate.postForObject(
//                    "http://localhost:8082/api/payments/process",
//                    paymentRequest,
//                    PaymentResultDto.class);
//
//            // 결제 결과 이벤트 발행
//            eventPublisher.publishEvent(new PaymentResultEvent(this, paymentId, result.getStatus()));
//        } catch (Exception e) {
//            // 결제 실패 이벤트 발행
//            eventPublisher.publishEvent(new PaymentResultEvent(this, paymentId, "FAILED"));
//        }
//    }


    @Override
    @Transactional
    public void cancelReservation(String username, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND));

        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!reservation.getMember().equals(member)) {
            throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_CANCEL);
        }

        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationException(ReservationErrorCode.ALREADY_CANCELLED);
        }

        // 예약 및 결제 상태 변경
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservation.setPaymentStatus("CANCELLED");
        reservationRepository.save(reservation);

        ReservationSlot reservationSlot = reservationSlotRepository.findById(reservation.getReservationSlot().getId())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.NO_RESERVATION_SLOT));
        reservationSlot.setCount(Math.max(0, reservationSlot.getCount() - 1));
        reservationSlotRepository.save(reservationSlot);

//        PaymentRequestDto cancelRequest = PaymentRequestDto.builder()
//                .paymentId(reservation.getPaymentId())
//                .status("CANCEL")
//                .build();
//        try {
//            restTemplate.postForObject(
//                    "http://localhost:8082/api/payments/process",
//                    cancelRequest,
//                    PaymentResultDto.class);
//        } catch (Exception e) {
//            throw new RuntimeException("결제 취소 실패");
//        }
    }

    public List<String> getAvailableReservationTimes(Long restaurantId, LocalDate date) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        List<ReservationSlot> reservationTimes = reservationSlotRepository.findAvailableTimes(restaurantId, date);

        return reservationTimes.stream()
                .map(slot -> slot.getTime().truncatedTo(ChronoUnit.MINUTES))
                .distinct()
                .sorted()
                .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm"))) // 문자열 변환
                .toList();
    }

    private Long calculateAmount(Long partySize, Restaurant restaurant) {
        return partySize * 5000L; // 인원당 5,000원
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
     * @param memberId            회원 ID
     * @param reservationId       예약 ID
     * @param notificationTypeStr 알림 타입 문자열
     * @param scheduledAt         예약 시간
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
