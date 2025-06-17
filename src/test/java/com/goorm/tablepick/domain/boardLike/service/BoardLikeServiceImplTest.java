package com.goorm.tablepick.domain.boardLike.service;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.boardLike.entity.BoardLike;
import com.goorm.tablepick.domain.boardLike.repository.BoardLikeRepository;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.repository.MenuRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantCategoryRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantImageRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantOperatingHourRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import com.goorm.tablepick.global.exception.BoardException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class BoardLikeServiceImplTest {
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    private BoardTagRepository boardTagRepository;
    @Autowired
    private RestaurantCategoryRepository restaurantCategoryRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    @Autowired
    private RestaurantImageRepository restaurantImageRepository;
    @Autowired
    private RestaurantOperatingHourRepository restaurantOperatingHourRepository;
    @Autowired
    private BoardLikeRepository boardLikeRepository;
    @Autowired
    private BoardLikeService boardLikeService;

    @AfterEach
    void tearDown() {
        boardTagRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        menuRepository.deleteAllInBatch();
        boardLikeRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        reservationSlotRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        restaurantImageRepository.deleteAllInBatch();
        restaurantOperatingHourRepository.deleteAllInBatch();
        restaurantRepository.deleteAllInBatch();
    }


    @Test
    void updateLike(){
    //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);

        restaurantCategoryRepository.save(restaurantCategory1);

        restaurantRepository.save(restaurant1);

        Member member1 = createMember("member1@gmail.com", "홍길동");

        memberRepository.save(member1);

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);

        reservationSlotRepository.save(reservationSlot1);

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);

        reservationRepository.save(reservation1);

        Board board1 = createBoard(restaurant1, member1, reservation1);

        boardRepository.save(board1);

        //when
        boardLikeService.LikeBoard(member1.getId(), board1.getId());

    //then
        Assertions.assertThat(boardRepository.findAll().size()).isEqualTo(1);

    }

    @Test
    void updateLikeDouble(){
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);

        restaurantCategoryRepository.save(restaurantCategory1);

        restaurantRepository.save(restaurant1);

        Member member1 = createMember("member1@gmail.com", "홍길동");

        memberRepository.save(member1);

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);

        reservationSlotRepository.save(reservationSlot1);

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);

        reservationRepository.save(reservation1);

        Board board1 = createBoard(restaurant1, member1, reservation1);

        boardRepository.save(board1);

        boardLikeRepository.save(BoardLike.builder().member(member1).board(board1).build());
        //when
        boardLikeService.LikeBoard(member1.getId(), board1.getId());
        //then
        Assertions.assertThat(boardLikeRepository.findAll().size()).isEqualTo(0);

    }

    @Test
    void updateLikeWithWrongMember(){
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);

        restaurantCategoryRepository.save(restaurantCategory1);

        restaurantRepository.save(restaurant1);

        Member member1 = createMember("member1@gmail.com", "홍길동");

        memberRepository.save(member1);

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);

        reservationSlotRepository.save(reservationSlot1);

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);

        reservationRepository.save(reservation1);

        Board board1 = createBoard(restaurant1, member1, reservation1);

        boardRepository.save(board1);

        boardLikeRepository.save(BoardLike.builder().member(member1).board(board1).build());
        //when
        //then
        Assertions.assertThatThrownBy(() -> boardLikeService.LikeBoard(100L, board1.getId()))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("사용자 정보를 찾을 수 없습니다.");
    }

    @Test
    void updateLikeWithWrongBoard(){
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);

        restaurantCategoryRepository.save(restaurantCategory1);

        restaurantRepository.save(restaurant1);

        Member member1 = createMember("member1@gmail.com", "홍길동");

        memberRepository.save(member1);

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);

        reservationSlotRepository.save(reservationSlot1);

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);

        reservationRepository.save(reservation1);

        Board board1 = createBoard(restaurant1, member1, reservation1);

        boardRepository.save(board1);

        boardLikeRepository.save(BoardLike.builder().member(member1).board(board1).build());
        //when
        //then
        Assertions.assertThatThrownBy(() -> boardLikeService.LikeBoard(member1.getId(), 100L))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다.");
    }


    private Member createMember(String email, String nickname) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }

    private static Board createBoard(Restaurant restaurant1, Member member, Reservation reservation) {
        return Board.builder().content("맛있고 분위가가 좋았어요")
                .restaurantId(restaurant1.getId())
                .member(member)
                .reservation(reservation)
                .build();
    }

    private Restaurant createRestaurant(String name, String address, RestaurantCategory restaurantCategory) {
        return Restaurant.builder()
                .name(name)
                .address(address)
                .restaurantPhoneNumber("02-345-3465")
                .restaurantCategory(restaurantCategory)
                .xcoordinate(127.067162146)
                .ycoordinate(37.497144519)
                .maxCapacity(3L)
                .build();
    }

    private static Reservation createReservation(ReservationSlot reservationSlot, Member member,
                                                 Restaurant restaurant) {
        return Reservation.builder()
                .reservationSlot(reservationSlot)
                .member(member)
                .restaurant(restaurant)
                .partySize(3L)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();
    }

    private static ReservationSlot createReservationSlot(Restaurant restaurant) {
        return ReservationSlot.builder()
                .restaurant(restaurant)
                .count(0L)
                .date(LocalDate.now())
                .time(LocalTime.parse("14:00"))
                .build();
    }
}