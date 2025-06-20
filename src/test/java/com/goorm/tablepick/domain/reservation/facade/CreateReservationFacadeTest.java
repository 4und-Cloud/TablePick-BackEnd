package com.goorm.tablepick.domain.reservation.facade;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.config.IntegrationTest;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.facade.V0.CreateReservationFacade;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.reservation.service.ImprovedReservationService.ReservationServiceV2;
import com.goorm.tablepick.domain.reservation.util.ConcurrentTestUtil;
import com.goorm.tablepick.domain.reservation.util.ReservationFixture;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@IntegrationTest
class CreateReservationFacadeTest {
    private static final int TEST_THREAD_COUNT = 20;

    @Autowired
    private ReservationServiceV2 originalService; // 오리지널 낙관적 락 버전
    @Autowired
    private CreateReservationFacade improvedFacade; // 개선된 버전
    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    private List<ReservationSlot> testReservationSlots;
    private List<Member> testMembers;

    @BeforeEach
    void setUp() {
        // 테스트용 레스토랑 생성 - 최대 용량 20명
        Restaurant testRestaurant = restaurantRepository.save(
                ReservationFixture.createRestaurant("Test Restaurant", 20L)
        );

        // 테스트용 예약 슬롯 생성
        testReservationSlots = new ArrayList<>();
        for (int i = 0; i < TEST_THREAD_COUNT; i++) {
            ReservationSlot slot = ReservationFixture.createReservationSlot(
                    LocalDate.now().plusDays(i + 1),
                    LocalTime.of(12, 0),
                    0L,
                    testRestaurant
            );
            testReservationSlots.add(reservationSlotRepository.save(slot));
        }

        // 테스트용 회원 생성
        testMembers = memberRepository.saveAll(ReservationFixture.createTestMembers(TEST_THREAD_COUNT));
    }

    @Test
    @DisplayName("낙관적 락 (긴 트랜잭션) vs 개선된 버전 (짧은 트랜잭션 + 외부 결제) 비교")
    void comparePerformanceTest() throws InterruptedException {
        List<Long> originalVersionTimes = Collections.synchronizedList(new ArrayList<>());
        List<Long> improvedVersionTimes = Collections.synchronizedList(new ArrayList<>());

        ReservationSlot originalSlot = testReservationSlots.get(0); // 오리지널 테스트 슬롯
        ReservationRequestDto originalRequest = ReservationRequestDto.builder()
                .restaurantId(originalSlot.getRestaurant().getId())
                .reservationDate(originalSlot.getDate())
                .reservationTime(originalSlot.getTime())
                .partySize(1L)
                .build();

        // 오리지널 낙관적 락 테스트
        long originalStartTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentReservations(
                originalSlot.getId(),
                testMembers,
                0,
                (slotId, memberId) -> {
                    long start = System.currentTimeMillis();
                    String username = getUsernameById(memberId);
                    originalService.createReservationOptimistic(username, originalRequest);
                    originalVersionTimes.add(System.currentTimeMillis() - start);
                }
        );

        // 개선된 테스트: 새로운 슬롯 사용 (낙관적 락 충돌 제거 목적)
        ReservationSlot improvedSlot = reservationSlotRepository.save(
                ReservationFixture.createReservationSlot(
                        LocalDate.now().plusDays(100), // 날짜 완전히 다르게
                        LocalTime.of(12, 0),
                        0L,
                        originalSlot.getRestaurant()
                )
        );
        ReservationRequestDto improvedRequest = ReservationRequestDto.builder()
                .restaurantId(improvedSlot.getRestaurant().getId())
                .reservationDate(improvedSlot.getDate())
                .reservationTime(improvedSlot.getTime())
                .partySize(1L)
                .build();

        // 개선된 버전 테스트
        long improvedStartTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentReservations(
                improvedSlot.getId(),
                testMembers,
                0,
                (slotId, memberId) -> {
                    long start = System.currentTimeMillis();
                    String username = getUsernameById(memberId);
                    improvedFacade.createReservation(username, improvedRequest);
                    improvedVersionTimes.add(System.currentTimeMillis() - start);
                }
        );

        // 결과 출력
        logTestResults("오리지널 버전 (낙관적 락, 긴 트랜잭션)", originalStartTime, originalVersionTimes);
        logTestResults("개선된 버전 (짧은 트랜잭션 + 외부 결제)", improvedStartTime, improvedVersionTimes);
    }

    private double calculateAverage(List<Long> times) {
        return times.stream()
                .mapToLong(Long::valueOf)
                .average()
                .orElse(0.0);
    }

    private void logTestResults(String version, long startTime, List<Long> executionTimes) {
        long totalExecutionTime = System.currentTimeMillis() - startTime;
        double averageExecutionTime = calculateAverage(executionTimes);

        log.info("=== {} 성능 테스트 결과 ===", version);
        log.info("동시 요청 수: {}", testMembers.size());
        log.info("커넥션 풀 사이즈: 32");
        log.info("총 실행 시간: {}ms", totalExecutionTime);
        log.info("평균 실행 시간: {}ms", averageExecutionTime);
        log.info("최소 실행 시간: {}ms", Collections.min(executionTimes));
        log.info("최대 실행 시간: {}ms", Collections.max(executionTimes));
    }

    private String getUsernameById(Long memberId) {
        return testMembers.stream()
                .filter(m -> m.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND))
                .getEmail();
    }

}