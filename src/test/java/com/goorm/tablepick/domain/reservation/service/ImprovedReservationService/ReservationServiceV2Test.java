package com.goorm.tablepick.domain.reservation.service.ImprovedReservationService;

import static org.assertj.core.api.Assertions.assertThat;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.config.IntegrationTest;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
import com.goorm.tablepick.domain.reservation.facade.OptimisticLockFacade;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.reservation.util.ConcurrentTestUtil;
import com.goorm.tablepick.domain.reservation.util.ReservationFixture;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@IntegrationTest
class ReservationServiceV2Test {

    @Autowired
    private ReservationServiceV2 reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OptimisticLockFacade optimisticLockFacade;

    private ReservationSlot testReservationSlot;
    private Restaurant testRestaurant;
    private List<Member> testMembers;

    // 동시 요청 수
    private static final int THREAD_COUNT = 100;


    @BeforeEach
    void setUp() {
        // 테스트용 레스토랑 생성 - 최대 용량 100명
        testRestaurant = restaurantRepository.save(
                ReservationFixture.createRestaurant("Test Restaurant", 100L)
        );

        // 테스트용 예약 슬롯 생성
        testReservationSlot = reservationSlotRepository.save(
                ReservationFixture.createReservationSlot(
                        LocalDate.now().plusDays(1),
                        LocalTime.of(12, 0),
                        0L,
                        testRestaurant
                )
        );

        // 테스트용 회원 생성
        testMembers = memberRepository.saveAll(
                ReservationFixture.createTestMembers(THREAD_COUNT)
        );
    }

    @AfterEach
    void cleanup() {
        reservationRepository.deleteAllInBatch();
        reservationSlotRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("비관적 락으로 100명 동시 예약 테스트")
    void pessimisticLockTest() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentReservations(
                testReservationSlot.getId(),
                testMembers,
                0,
                (slotId, memberId) -> {
                    try {
                        reservationService.createReservationPessimistic(
                                memberRepository.findById(memberId).orElseThrow().getEmail(),
                                request
                        );
                    } catch (ReservationException e) {
                        // 예약 실패는 무시 (용량 초과 시 예외 발생 예상)
                        log.debug("Reservation failed for member {}: {}", memberId, e.getMessage());
                    }
                }
        );
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        ReservationSlot updatedSlot = reservationSlotRepository.findById(testReservationSlot.getId()).orElseThrow();
        long totalParticipants = updatedSlot.getCount();

        log.info("=== 비관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("가능한 예약 수: {}", testRestaurant.getMaxCapacity());
        log.info("총 예약된 인원 수: {}", totalParticipants);

        assertThat(totalParticipants).isEqualTo(THREAD_COUNT);

    }

    @Test
    @DisplayName("낙관적 락으로 100명 동시 예약 테스트")
    void optimisticLockTest() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentReservations(
                testReservationSlot.getId(),
                testMembers,
                0,
                (slotId, memberId) -> {
                    try {
                        optimisticLockFacade.createReservationWithOptimisticLock(
                                memberRepository.findById(memberId).orElseThrow().getEmail(),
                                request
                        );
                    } catch (ReservationException e) {
                        // 예약 실패는 무시 (용량 초과 시 예외 발생 예상)
                        log.debug("Reservation failed for member {}: {}", memberId, e.getMessage());
                    }
                }
        );
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        ReservationSlot updatedSlot = reservationSlotRepository.findById(testReservationSlot.getId()).orElseThrow();
        long totalParticipants = updatedSlot.getCount();

        log.info("=== 낙관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("가능한 예약 수: {}", testRestaurant.getMaxCapacity());
        log.info("총 예약된 인원 수: {}", totalParticipants);

        assertThat(totalParticipants).isEqualTo(THREAD_COUNT);

    }

    @Test
    @DisplayName("낙관적 락 - 1000ms 간격 요청 테스트")
    void optimisticLockWithDelayTest() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // when
        long start = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentReservations(
                testReservationSlot.getId(),
                testMembers,
                1000,
                (slotId, memberId) -> {
                    try {
                        optimisticLockFacade.createReservationWithOptimisticLock(
                                memberRepository.findById(memberId).orElseThrow().getEmail(),
                                request
                        );
                    } catch (ReservationException e) {
                        log.debug("예약 실패 member {}: {}", memberId, e.getMessage());
                    }
                }
        );
        long executionTime = System.currentTimeMillis() - start;

        // then
        ReservationSlot updatedSlot = reservationSlotRepository.findById(testReservationSlot.getId()).orElseThrow();
        long totalParticipants = updatedSlot.getCount();

        log.info("=== 낙관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("가능한 예약 수: {}", testRestaurant.getMaxCapacity());
        log.info("총 예약된 인원 수: {}", totalParticipants);
    }

    @Test
    @DisplayName("비관적 락 - 1000ms 간격 요청 테스트")
    void pessimisticLockWithDelayTest() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // when
        long start = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentReservations(
                testReservationSlot.getId(),
                testMembers,
                1000, // 요청 간 딜레이 (1초)
                (slotId, memberId) -> {
                    try {
                        reservationService.createReservationPessimistic(
                                memberRepository.findById(memberId).orElseThrow().getEmail(),
                                request
                        );
                    } catch (ReservationException e) {
                        log.debug("예약 실패 member {}: {}", memberId, e.getMessage());
                    }
                }
        );
        long executionTime = System.currentTimeMillis() - start;

        // then
        ReservationSlot updatedSlot = reservationSlotRepository.findById(testReservationSlot.getId()).orElseThrow();
        long totalParticipants = updatedSlot.getCount();

        log.info("=== 비관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("가능한 예약 수: {}", testRestaurant.getMaxCapacity());
        log.info("총 예약된 인원 수: {}", totalParticipants);
    }

    @Test
    @DisplayName("낙관적 락 - 5초 간격, 5명씩 20번 요청 테스트")
    void optimisticLock_BatchDelayTest() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // when
        long start = System.currentTimeMillis();
        ConcurrentTestUtil.executeBatchConcurrentReservations(
                testReservationSlot.getId(),
                testMembers,
                5,              // batch size
                5000,           // 5초 간격
                (slotId, memberId) -> {
                    try {
                        optimisticLockFacade.createReservationWithOptimisticLock(
                                memberRepository.findById(memberId).orElseThrow().getEmail(),
                                request
                        );
                    } catch (ReservationException e) {
                        log.debug("예약 실패 member {}: {}", memberId, e.getMessage());
                    }
                }
        );
        long elapsed = System.currentTimeMillis() - start;

        // then
        ReservationSlot updatedSlot = reservationSlotRepository.findById(testReservationSlot.getId()).orElseThrow();
        long totalParticipants = updatedSlot.getCount();

        log.info("=== 낙관적 락 배치 테스트 결과 ===");
        log.info("총 소요 시간: {}ms", elapsed);
        log.info("총 예약 인원: {}", totalParticipants);
        assertThat(totalParticipants).isEqualTo(testMembers.size());
    }

    @Test
    @DisplayName("비관적 락 - 5초 간격, 5명씩 20번 요청 테스트")
    void pessimisticLock_BatchDelayTest() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // when
        long start = System.currentTimeMillis();
        ConcurrentTestUtil.executeBatchConcurrentReservations(
                testReservationSlot.getId(),
                testMembers,
                5,              // batch size
                5000,           // 5초 간격
                (slotId, memberId) -> {
                    try {
                        reservationService.createReservationPessimistic(
                                memberRepository.findById(memberId).orElseThrow().getEmail(),
                                request
                        );
                    } catch (ReservationException e) {
                        log.debug("예약 실패 member {}: {}", memberId, e.getMessage());
                    }
                }
        );
        long elapsed = System.currentTimeMillis() - start;

        // then
        ReservationSlot updatedSlot = reservationSlotRepository.findById(testReservationSlot.getId()).orElseThrow();
        long totalParticipants = updatedSlot.getCount();

        log.info("=== 비관적 락 배치 테스트 결과 ===");
        log.info("총 소요 시간: {}ms", elapsed);
        log.info("총 예약 인원: {}", totalParticipants);
        assertThat(totalParticipants).isEqualTo(testMembers.size());
    }


}
