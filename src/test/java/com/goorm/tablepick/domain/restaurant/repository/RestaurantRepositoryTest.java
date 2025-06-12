//package com.goorm.tablepick.domain.restaurant.repository;
//
//import com.goorm.tablepick.domain.board.entity.Board;
//import com.goorm.tablepick.domain.board.entity.BoardTag;
//import com.goorm.tablepick.domain.board.repository.BoardRepository;
//import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
//import com.goorm.tablepick.domain.member.entity.Member;
//import com.goorm.tablepick.domain.member.repository.MemberRepository;
//import com.goorm.tablepick.domain.reservation.entity.Reservation;
//import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
//import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
//import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
//import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
//import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
//import com.goorm.tablepick.domain.restaurant.entity.Menu;
//import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
//import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
//import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
//import com.goorm.tablepick.domain.tag.entity.Tag;
//import com.goorm.tablepick.domain.tag.repository.TagRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//
//@ActiveProfiles("test")
//@DataJpaTest
//public class RestaurantRepositoryTest {
//    @Autowired
//    private RestaurantRepository restaurantRepository;
//    @Autowired
//    private MenuRepository menuRepository;
//    @Autowired
//    TagRepository tagRepository;
//    @Autowired
//    private BoardTagRepository boardTagRepository;
//    @Autowired
//    private RestaurantCategoryRepository restaurantCategoryRepository;
//    @Autowired
//    private MemberRepository memberRepository;
//    @Autowired
//    private BoardRepository boardRepository;
//    @Autowired
//    private ReservationRepository reservationRepository;
//    @Autowired
//    private ReservationSlotRepository reservationSlotRepository;
//
//    @DisplayName("키워드가 식당 이름, 주소, 메뉴이름에 포함된 식당을 찾는다.")
//    @Test
//    void searchByKeyword() {
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//
//        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
//        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
//        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
//        Menu menu4 = createMenu("광어회", 36000, restaurant2);
//        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
//        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));
//
//        // when
//        Pageable pageable = PageRequest.of(0, 6);
//        Page<Restaurant> restaurants = restaurantRepository.findAllByKeyword("식당", pageable);
//
//        // then
//        assertThat(restaurants).hasSize(2)
//                .extracting(Restaurant::getName)
//                .containsExactlyInAnyOrder("골목 식당", "바다 식당");
//    }
//
//    @DisplayName("키워드와 태그가 모든 식당을 반환한다.")
//    @Test
//    void searchWithoutAnything() {
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//
//        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
//        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
//        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
//        Menu menu4 = createMenu("광어회", 36000, restaurant2);
//        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
//        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));
//
//        // when
//        Pageable pageable = PageRequest.of(0, 6);
//        Page<Restaurant> restaurants = restaurantRepository.findAllByKeyword("", pageable);
//
//        // then
//        assertThat(restaurants).hasSize(3)
//                .extracting(Restaurant::getName)
//                .containsExactlyInAnyOrder("골목 식당", "바다 식당", "달콤한 카페");
//    }
//
//
//    @DisplayName("검색어와 3개이하의 태그로 검색시 검색어가 주소, 이름, 메뉴에 포함되고 보드 태그가 모두 포함된 식당을 찾는다.")
//    @Test
//    void searchAllByKeywordAndTags() {
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//
//        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
//        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
//        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
//        Menu menu4 = createMenu("광어회", 36000, restaurant2);
//        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
//        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));
//
//        Member member1 = createMember("member1@gmail.com", "홍길동");
//
//        memberRepository.save(member1);
//
//        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
//        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
//        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
//
//        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3));
//
//        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
//        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
//        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
//
//        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));
//
//        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
//        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
//        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();
//
//        tagRepository.saveAll(List.of(tag1, tag2, tag3));
//
//        Board board1 = createBoard(restaurant1, member1, reservation1);
//        Board board2 = createBoard(restaurant2, member1, reservation2);
//        Board board3 = createBoard(restaurant3, member1, reservation3);
//
//        boardRepository.saveAll(List.of(board1, board2, board3));
//
//        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
//        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
//        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
//        BoardTag boardTag4 = BoardTag.builder().tag(tag3).restaurant(restaurant2).board(board2).build();
//        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
//        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();
//
//        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));
//
//        // when
//        Pageable pageable = PageRequest.of(0, 6);
//        List<Long> tagIds = List.of(tag1.getId(), tag2.getId());
//        Page<Restaurant> restaurants = restaurantRepository.findAllByKeywordAndTags("식당", tagIds, tagIds.size(), pageable);
//
//        // then
//        assertThat(restaurants).hasSize(1)
//                .extracting(Restaurant::getName)
//                .containsExactlyInAnyOrder("골목 식당");
//    }
//
//    @DisplayName("검색어가 식당 이름, 주소, 메뉴이름에 포함된 식당을 찾는다.")
//    @Test
//    void searchByKeywordUsingQueryDSL() {
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//
//        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
//        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
//        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
//        Menu menu4 = createMenu("광어회", 36000, restaurant2);
//        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
//        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));
//
//        // when
//        Pageable pageable = PageRequest.of(0, 6);
//        List<Long> tagIds = new ArrayList<>();
//        List<RestaurantSearchResponseDto> restaurants = restaurantRepository.searchRestaurantResult("식당", tagIds,
//                pageable);
//
//        // then
//        assertThat(restaurants).hasSize(2)
//                .flatExtracting(RestaurantSearchResponseDto::getName)
//                .containsExactlyInAnyOrder("골목 식당", "바다 식당");
//    }
//
//
//    @DisplayName("3개 이하의 갯수의 태그로 검색시 보드 태그가 모두 포함된 식당을 찾는다.")
//    @Test
//    void searchAllByTagsUsingQueryDSL() {
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//
//        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
//        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
//        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
//        Menu menu4 = createMenu("광어회", 36000, restaurant2);
//        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
//        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));
//
//        Member member1 = createMember("member1@gmail.com", "홍길동");
//
//        memberRepository.save(member1);
//
//        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
//        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
//        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
//
//        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3));
//
//        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
//        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
//        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
//
//        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));
//
//        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
//        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
//        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();
//
//        tagRepository.saveAll(List.of(tag1, tag2, tag3));
//
//        Board board1 = createBoard(restaurant1, member1, reservation1);
//        Board board2 = createBoard(restaurant2, member1, reservation2);
//        Board board3 = createBoard(restaurant3, member1, reservation3);
//
//        boardRepository.saveAll(List.of(board1, board2, board3));
//
//        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
//        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
//        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
//        BoardTag boardTag4 = BoardTag.builder().tag(tag3).restaurant(restaurant2).board(board2).build();
//        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
//        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();
//
//        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));
//
//        // when
//        Pageable pageable = PageRequest.of(0, 6);
//        List<RestaurantSearchResponseDto> restaurants = restaurantRepository.searchRestaurantResult(null, List.of(1L, 2L), pageable);
//
//        // then
//        assertThat(restaurants).hasSize(1)
//                .flatExtracting(RestaurantSearchResponseDto::getName)
//                .containsExactlyInAnyOrder("골목 식당");
//    }
//
//    @DisplayName("검색어와 태그 없이 검색시 모든 식당을 찾는다.")
//    @Test
//    void searchWithoutAnythingUsingQueryDSL() {
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//
//        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
//        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
//        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
//        Menu menu4 = createMenu("광어회", 36000, restaurant2);
//        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
//        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));
//
//        // when
//        Pageable pageable = PageRequest.of(0, 6);
//        List<RestaurantSearchResponseDto> restaurants = restaurantRepository.searchRestaurantResult(null, List.of(), pageable);
//
//        // then
//        assertThat(restaurants).hasSize(3)
//                .flatExtracting(RestaurantSearchResponseDto::getName)
//                .containsExactlyInAnyOrder("골목 식당", "바다 식당", "달콤한 카페");
//    }
//
//    @DisplayName("검색어와 3개이하의 태그로 검색시 검색어가 주소, 이름, 메뉴에 포함되고 보드 태그가 모두 포함된 식당을 찾는다.")
//    @Test
//    void searchAllByKeywordAndTagsUsingQueryDSL() {
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//
//        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
//        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
//        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
//        Menu menu4 = createMenu("광어회", 36000, restaurant2);
//        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
//        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));
//
//        Member member1 = createMember("member1@gmail.com", "홍길동");
//
//        memberRepository.save(member1);
//
//        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
//        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
//        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
//
//        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3));
//
//        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
//        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
//        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
//
//        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));
//
//        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
//        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
//        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();
//
//        tagRepository.saveAll(List.of(tag1, tag2, tag3));
//
//        Board board1 = createBoard(restaurant1, member1, reservation1);
//        Board board2 = createBoard(restaurant2, member1, reservation2);
//        Board board3 = createBoard(restaurant3, member1, reservation3);
//
//        boardRepository.saveAll(List.of(board1, board2, board3));
//
//        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
//        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
//        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
//        BoardTag boardTag4 = BoardTag.builder().tag(tag3).restaurant(restaurant2).board(board2).build();
//        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
//        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();
//
//        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));
//
//        // when
//        Pageable pageable = PageRequest.of(0, 6);
//        List<Long> tagIds = List.of(tag1.getId(), tag2.getId());
//        List<RestaurantSearchResponseDto> restaurants = restaurantRepository.searchRestaurantResult("식당", tagIds, pageable);
//        // then
//        assertThat(restaurants).hasSize(1)
//                .flatExtracting(RestaurantSearchResponseDto::getName)
//                .containsExactlyInAnyOrder("골목 식당");
//    }
//
//    @DisplayName("예약이 많은 순으로 식당을 조회한다.")
//    @Test
//    void findRestaurantsOrderByReservationSlot(){
//    // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
//        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
//        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
//        restaurant1.getRestaurantImages().add(restaurantImage1);
//        restaurant1.getRestaurantImages().add(restaurantImage2);
//        restaurant1.getRestaurantImages().add(restaurantImage3);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
//
//        Member member1 = createMember("member1@gmail.com", "홍길동");
//
//        memberRepository.save(member1);
//
//        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
//        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
//        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
//
//        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3));
//
//        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
//        Reservation reservation2 = createReservation(reservationSlot3, member1, restaurant3);
//        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
//
//        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));
//    // when
//        Pageable pageable = PageRequest.of(0, 6);
//        Page<Restaurant> popularRestaurants = restaurantRepository.findPopularRestaurants(pageable);
//
//        // then
//        Assertions.assertThat(popularRestaurants).hasSize(3)
//                .containsExactlyElementsOf(List.of(restaurant3, restaurant1, restaurant2));
//    }
//
//    @DisplayName("보드 많은 순으로 식당을 정렬한다.")
//    @Test
//    void findRestaurantsOrderByBoardNum(){
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
//        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
//        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();
//
//        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
//        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
//        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
//        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
//        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
//        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
//        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
//        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
//        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
//        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
//        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
//        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
//        restaurant1.getRestaurantImages().add(restaurantImage1);
//        restaurant2.getRestaurantImages().add(restaurantImage2);
//        restaurant3.getRestaurantImages().add(restaurantImage3);
//        restaurant4.getRestaurantImages().add(restaurantImage4);
//        restaurant5.getRestaurantImages().add(restaurantImage5);
//        restaurant6.getRestaurantImages().add(restaurantImage6);
//
//        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
//        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));
//
//        Member member1 = createMember("member1@gmail.com", "홍길동");
//        Member member2 = createMember("member2@gmail.com", "고길동");
//
//        memberRepository.saveAll(List.of(member1, member2));
//
//        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
//        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
//        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
//        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);
//        ReservationSlot reservationSlot5 = createReservationSlot(restaurant5);
//        ReservationSlot reservationSlot6 = createReservationSlot(restaurant6);
//
//        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4, reservationSlot5, reservationSlot6));
//
//        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
//        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
//        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
//        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
//        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
//        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
//        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);
//
//        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6, reservation7));
//
//        Board board1 = createBoard(restaurant1, member1, reservation1);
//        Board board2 = createBoard(restaurant2, member1, reservation2);
//        Board board3 = createBoard(restaurant3, member1, reservation3);
//        Board board4 = createBoard(restaurant3, member2, reservation3);
//        Board board5 = createBoard(restaurant3, member1, reservation3);
//        Board board6 = createBoard(restaurant4, member1, reservation4);
//        Board board7 = createBoard(restaurant4, member2, reservation4);
//
//        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));
//
//        //when
//        Pageable pageable = PageRequest.of(0, 6);
//        Page<Restaurant> allOrderByBoardNum = restaurantRepository.findAllOrderByBoardNum(pageable);
//
//        //then
//        Assertions.assertThat(allOrderByBoardNum.getContent()).hasSize(6)
//                .extracting("name")
//                .containsExactlyElementsOf(List.of("달콤한 카페", "조용한 카페", "골목 식당", "바다 식당", "케이크가 맛있는 카페", "경양식집"));
//
//    }
//
//    private static RestaurantImage createRestaurantImage(Restaurant restaurant1, String url) {
//        return RestaurantImage.builder()
//                .imageUrl(url)
//                .restaurant(restaurant1)
//                .build();
//    }
//
//    private Restaurant createRestaurant(String name, String address, RestaurantCategory restaurantCategory) {
//        return Restaurant.builder()
//                .name(name)
//                .address(address)
//                .restaurantPhoneNumber("02-345-3465")
//                .restaurantCategory(restaurantCategory)
//                .xcoordinate(127.067162146)
//                .ycoordinate(37.497144519)
//                .maxCapacity(3L)
//                .build();
//    }
//
//    private Menu createMenu(String name, int price, Restaurant restaurant) {
//        return Menu.builder()
//                .name(name)
//                .price(BigDecimal.valueOf(price))
//                .restaurant(restaurant)
//                .build();
//    }
//
//    private Member createMember(String email, String nickname) {
//        return Member.builder()
//                .email(email)
//                .nickname(nickname)
//                .build();
//    }
//
//    private static Board createBoard(Restaurant restaurant1, Member member, Reservation reservation) {
//        return Board.builder().content("맛있고 분위가가 좋았어요")
//                .restaurantId(restaurant1.getId())
//                .member(member)
//                .reservation(reservation)
//                .build();
//    }
//
//    private static Reservation createReservation(ReservationSlot reservationSlot, Member member,
//                                                 Restaurant restaurant) {
//        return Reservation.builder()
//                .reservationSlot(reservationSlot)
//                .member(member)
//                .restaurant(restaurant)
//                .partySize(3L)
//                .reservationStatus(ReservationStatus.CONFIRMED)
//                .build();
//    }
//
//    private static ReservationSlot createReservationSlot(Restaurant restaurant) {
//        return ReservationSlot.builder()
//                .restaurant(restaurant)
//                .count(0L)
//                .date(LocalDate.now())
//                .time(LocalTime.parse("14:00"))
//                .build();
//    }
//
//}
