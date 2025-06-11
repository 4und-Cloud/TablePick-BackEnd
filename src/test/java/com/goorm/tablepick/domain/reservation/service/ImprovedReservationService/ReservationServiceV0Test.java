package com.goorm.tablepick.domain.reservation.service.ImprovedReservationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.config.IntegrationTest;
import com.goorm.tablepick.domain.reservation.dto.request.ReservationRequestDto;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.exception.ReservationErrorCode;
import com.goorm.tablepick.domain.reservation.exception.ReservationException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@IntegrationTest
class ReservationServiceV0Test {

    @Autowired
    private ReservationServiceV0 reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private ReservationSlot testReservationSlot;
    private Restaurant testRestaurant;
    private List<Member> testMembers;

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

        // 테스트용 회원 150명 생성
        testMembers = memberRepository.saveAll(
                ReservationFixture.createTestMembers(150)
        );
    }

    @Test
    @DisplayName("트랜잭션만으로는 동시성 제어가 안 되는 것을 보여주는 테스트")
    void transactionDoesNotGuaranteeAtomicity() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // when
        ConcurrentTestUtil.executeConcurrentReservations(
                testReservationSlot.getId(),
                testMembers,
                0,
                (slotId, memberId) -> {
                    try {
                        reservationService.createReservation(
                                memberRepository.findById(memberId).orElseThrow().getEmail(),
                                request
                        );
                    } catch (ReservationException e) {
                        // 예약 실패는 무시 (용량 초과 시 예외 발생 예상)
                        log.debug("Reservation failed for member {}: {}", memberId, e.getMessage());
                    }
                }
        );

        // then
        ReservationSlot updatedSlot = reservationSlotRepository.findById(testReservationSlot.getId()).orElseThrow();
        long actualReservationCount = reservationRepository.countByReservationSlot(updatedSlot);
        long totalParticipants = reservationRepository.count();

        log.info("=== 트랜잭션 동시성 테스트 결과 ===");
        log.info("레스토랑 최대 용량: {}", testRestaurant.getMaxCapacity());
        log.info("슬롯 현재 예약 수: {}", updatedSlot.getCount());
        log.info("실제 예약 레코드 수: {}", actualReservationCount);
        log.info("총 참가자 수: {}", totalParticipants);

        // 검증 1: 슬롯의 count와 실제 예약 수가 일치하지 않음
        assertThat(updatedSlot.getCount()).isNotEqualTo(actualReservationCount);
        // 검증 2: 슬롯의 count는 최대 용량을 초과하지 않음
        assertThat(updatedSlot.getCount()).isLessThanOrEqualTo(testRestaurant.getMaxCapacity());
        // 검증 3: 총 예약 수는 최대 용량을 초과할 수 있음 (동시성 문제로 중복 허용)
        assertThat(totalParticipants).isGreaterThan(testRestaurant.getMaxCapacity());
    }

    @Test
    @DisplayName("동시에 예약 시도 시 용량 초과 예외 발생 확인")
    void shouldThrowExceptionOnCapacityExceed() throws InterruptedException {
        // given
        ReservationRequestDto request = ReservationRequestDto.builder()
                .restaurantId(testRestaurant.getId())
                .reservationDate(testReservationSlot.getDate())
                .reservationTime(testReservationSlot.getTime())
                .partySize(1L)
                .build();

        // 첫 100명 예약 성공 확인
        for (int i = 0; i < 100; i++) {
            Member member = testMembers.get(i);
            reservationService.createReservation(member.getEmail(), request);
        }
        assertThat(testReservationSlot.getCount()).isEqualTo(100L);

        // 101번째 예약 시도에서 예외 발생 확인
        Member extraMember = testMembers.get(100);
        assertThatThrownBy(() -> reservationService.createReservation(extraMember.getEmail(), request))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining(ReservationErrorCode.EXCEED_RESERVATION_LIMIT.getMessage());
    }
}