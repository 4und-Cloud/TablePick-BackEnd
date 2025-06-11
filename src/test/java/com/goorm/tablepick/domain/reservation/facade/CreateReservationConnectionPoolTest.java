package com.goorm.tablepick.domain.reservation.facade;


import static org.assertj.core.api.Assertions.assertThat;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.config.IntegrationTest;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@IntegrationTest
class CreateReservationConnectionPoolTest {
    private static final int BACKGROUND_THREAD_COUNT = 40; // 40개 커넥션 점유
    private static final int TEST_THREAD_COUNT = 20;

    @Autowired
    private ReservationServiceV2 originalService; // 오리지널 낙관적 락 버전
    @Autowired
    private CreateReservationFacade improvedFacade;
    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private List<ReservationSlot> testReservationSlots;
    private List<Member> testMembers;
    private ExecutorService backgroundExecutor;

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

        // 백그라운드 실행자 생성
        backgroundExecutor = Executors.newFixedThreadPool(BACKGROUND_THREAD_COUNT);
    }

    @AfterEach
    void cleanup() {
        backgroundExecutor.shutdownNow();
    }

    @Test
    @DisplayName("커넥션 풀 점유 상황에서 트랜잭션 분리 효과 비교")
    void compareWaitTimeTest() throws InterruptedException {
        // given
        occupyConnections();
        Thread.sleep(1000); // 커넥션이 점유되기를 기다림

        List<Long> originalVersionTimes = Collections.synchronizedList(new ArrayList<>());
        List<Long> improvedVersionTimes = Collections.synchronizedList(new ArrayList<>());

        ReservationSlot originalSlot = testReservationSlots.get(0);
        ReservationRequestDto originalRequest = ReservationRequestDto.builder()
                .restaurantId(originalSlot.getRestaurant().getId())
                .reservationDate(originalSlot.getDate())
                .reservationTime(originalSlot.getTime())
                .partySize(1L)
                .build();

        // when - 오리지널 버전 테스트 (낙관적 락, 긴 트랜잭션)
        ConcurrentTestUtil.executeConcurrentReservations(
                originalSlot.getId(),
                testMembers,
                0,
                (slotId, memberId) -> {
                    long startTime = System.currentTimeMillis();
                    String username = getUsernameById(memberId);
                    originalService.createReservationOptimistic(username, originalRequest);
                    originalVersionTimes.add(System.currentTimeMillis() - startTime);
                }
        );

        // 새로운 예약 슬롯 생성 (충돌 방지)
        ReservationSlot improvedSlot = reservationSlotRepository.save(
                ReservationFixture.createReservationSlot(
                        LocalDate.now().plusDays(100),
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

        // when - 개선된 버전 테스트 (짧은 트랜잭션 + 외부 결제)
        ConcurrentTestUtil.executeConcurrentReservations(
                improvedSlot.getId(),
                testMembers,
                0,
                (slotId, memberId) -> {
                    long startTime = System.currentTimeMillis();
                    String username = getUsernameById(memberId);
                    improvedFacade.createReservation(username, improvedRequest);
                    improvedVersionTimes.add(System.currentTimeMillis() - startTime);
                }
        );

        // then
        double originalAverage = calculateAverage(originalVersionTimes);
        double improvedAverage = calculateAverage(improvedVersionTimes);

        logTestResults("오리지널 버전 (낙관적 락, 긴 트랜잭션)", originalVersionTimes);
        logTestResults("개선된 버전 (짧은 트랜잭션 + 외부 결제)", improvedVersionTimes);

        assertThat(improvedAverage)
                .isLessThan(originalAverage * 0.5)
                .as("개선된 버전이 원본 버전보다 50% 이상 빨라야 함");
    }

    private double calculateAverage(List<Long> times) {
        return times.stream()
                .mapToLong(Long::valueOf)
                .average()
                .orElse(0.0);
    }

    private void logTestResults(String version, List<Long> waitTimes) {
        double averageWaitTime = calculateAverage(waitTimes);
        log.info("=== {} 성능 테스트 결과 ===", version);
        log.info("평균 대기 시간: {}ms", averageWaitTime);
        log.info("최소 대기 시간: {}ms", Collections.min(waitTimes));
        log.info("최대 대기 시간: {}ms", Collections.max(waitTimes));
    }

    private void occupyConnections() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        for (int i = 0; i < BACKGROUND_THREAD_COUNT; i++) {
            backgroundExecutor.submit(() -> {
                try {
                    transactionTemplate.execute(status -> {
                        try {
                            Thread.sleep(Integer.MAX_VALUE);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    });
                } catch (Exception e) {
                    log.error("Error in background thread: ", e);
                }
            });
        }
    }

    private String getUsernameById(Long memberId) {
        return testMembers.stream()
                .filter(m -> m.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND))
                .getEmail();
    }
}