package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.CategoryResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import com.goorm.tablepick.domain.restaurant.entity.Menu;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import com.goorm.tablepick.domain.restaurant.enums.DayOfWeek;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.MenuRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantCategoryRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantImageRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantOperatingHourRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class RestaurantServiceTest {

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
    private RestaurantService restaurantService;

    @AfterEach
    void tearDown() {
        boardTagRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        menuRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        reservationSlotRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        restaurantImageRepository.deleteAllInBatch();
        restaurantOperatingHourRepository.deleteAllInBatch();
        restaurantRepository.deleteAllInBatch();
    }

    @DisplayName("예약이 많은 순으로 상위 4개의 식당을 찾는다.")
    @Test
    void getMainPageRestaurantList() {
        // given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);
        restaurant5.getRestaurantImages().add(restaurantImage5);
        restaurant6.getRestaurantImages().add(restaurantImage6);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);
        ReservationSlot reservationSlot5 = createReservationSlot(restaurant5);
        ReservationSlot reservationSlot6 = createReservationSlot(restaurant6);

        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4, reservationSlot5, reservationSlot6));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation6 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6));

        // when
        Pageable pageable = PageRequest.of(0, 4);
        Page<RestaurantResponseDto> restaurants = restaurantService.getAllRestaurants(pageable, member1);

        // then
        Assertions.assertThat(restaurants.getContent()).hasSize(4)
                .extracting( "name")
                .containsExactlyElementsOf(List.of("달콤한 카페", "조용한 카페", "골목 식당", "바다 식당"));
    }

    @DisplayName("게시글 많은 순으로 식당을 조회합니다.")
    @Test
    void getAllOrderByBoardNum() {
    //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);
        restaurant5.getRestaurantImages().add(restaurantImage5);
        restaurant6.getRestaurantImages().add(restaurantImage6);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);
        ReservationSlot reservationSlot5 = createReservationSlot(restaurant5);
        ReservationSlot reservationSlot6 = createReservationSlot(restaurant6);

        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4, reservationSlot5, reservationSlot6));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6, reservation7));

        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant2, member1, reservation2);
        Board board3 = createBoard(restaurant3, member1, reservation3);
        Board board4 = createBoard(restaurant3, member2, reservation3);
        Board board5 = createBoard(restaurant3, member1, reservation3);
        Board board6 = createBoard(restaurant4, member1, reservation4);
        Board board7 = createBoard(restaurant4, member2, reservation4);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        //when
        PagedRestaurantResponseDto allRestaurantsOrderedByBoardNum = restaurantService.getAllRestaurantsOrderedByBoardNum(0, member1);

        //then
        Assertions.assertThat(allRestaurantsOrderedByBoardNum.getRestaurants()).hasSize(6)
                .extracting("name")
                .containsExactlyElementsOf(List.of("달콤한 카페", "조용한 카페", "골목 식당", "바다 식당", "케이크가 맛있는 카페", "경양식집"));
    }

    @DisplayName("식당 아이디로 식당 상세 정보를 조회한다.")
    @Test
    void getRestaurantDetails(){
    //given
        Member member1 = createMember("member1@gmail.com", "홍길동");

        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);

        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");

        restaurant1.getRestaurantImages().add(restaurantImage1);

        restaurantCategoryRepository.save(restaurantCategory1);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant1);

        //when
        restaurantService.getRestaurantDetail(savedRestaurant.getId(), member1);

    //then
        Assertions.assertThat(savedRestaurant).isNotNull();
        Assertions.assertThat(savedRestaurant).isEqualTo(restaurant1);
    }

    @DisplayName("없는 식당 아이디로 식당 상세 정보를 조회하면 에러가 발생한다.")
    @Test
    void getRestaurantDetailsWithWrongId(){
        //given
        Member member1 = createMember("member1@gmail.com", "홍길동");

        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);

        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");

        restaurant1.getRestaurantImages().add(restaurantImage1);

        restaurantCategoryRepository.save(restaurantCategory1);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant1);

        //when&then
        Assertions.assertThatThrownBy(() -> restaurantService.getRestaurantDetail(1234L, member1))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("식당 정보를 찾을 수 없습니다.");
    }

    @DisplayName("식당 카테고리 목록을 반환한다.")
    @Test
    void getRestaurantCategoryList(){
    //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1,restaurantCategory2,restaurantCategory3));
    //when
        List<CategoryResponseDto> categoryList = restaurantService.getCategoryList();

    //then
        Assertions.assertThat(categoryList).hasSize(3)
                .extracting("name")
                .containsExactlyInAnyOrder("한식", "양식", "카페");
    }

    @DisplayName("20자 이내의 키워드로 검색한다.")
    @Test
    void searchByKeywordUsingQueryDSL(){
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("여기는맛있는숨겨진아주아주맛이좋은식당")
                .page(0)
                .build();

        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        Restaurant restaurant1 = createRestaurant("골목 여기는맛있는숨겨진아주아주맛이좋은식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 여기는맛있는숨겨진아주아주맛이좋은식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant1.getRestaurantImages().add(restaurantImage2);
        restaurant1.getRestaurantImages().add(restaurantImage3);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));

        // when
        Page<RestaurantSearchResponseDto> result = restaurantService.searchRestaurantsV1(requestDto);

        // then
        Assertions.assertThat(result.getContent()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 여기는맛있는숨겨진아주아주맛이좋은식당", "바다 여기는맛있는숨겨진아주아주맛이좋은식당");

    }

    @Test
    @DisplayName("21자 이상의 키워드로 검색하면 예외가 발생한다")
    void searchByKeywordOver20UsingQueryDSL() {
        // given
        String invalidKeyword = "이것은_21자_이상의_너무_긴_검색어입니다";
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(invalidKeyword)
                .build();

        // when & then
        Assertions.assertThatThrownBy(() -> restaurantService.searchRestaurantsV1(requestDto))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("키워드는 20자 이내만 가능합니다.");
    }

    @Test
    @DisplayName("3개 이내의 키워드를 선택해 검색한다.")
    void searchByTagUsingQueryDSL() {
        // given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);

        Menu menu1 = createMenu("고등어 구이", 12000, restaurant1);
        Menu menu2 = createMenu("제육 볶음", 10000, restaurant1);
        Menu menu3 = createMenu("회덮밥", 12000, restaurant2);
        Menu menu4 = createMenu("광어회", 36000, restaurant2);
        Menu menu5 = createMenu("아메리카노", 4000, restaurant3);
        Menu menu6 = createMenu("딸기 생크림 케이크", 7500, restaurant3);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));
        menuRepository.saveAll(List.of(menu1, menu2, menu3, menu4, menu5, menu6));

        Member member1 = createMember("member1@gmail.com", "홍길동");

        memberRepository.save(member1);

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);

        reservationSlotRepository.saveAll(List.of(reservationSlot1, reservationSlot2, reservationSlot3));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);

        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));

        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();

        List<Tag> tags = tagRepository.saveAll(List.of(tag1, tag2, tag3));

        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant2, member1, reservation2);
        Board board3 = createBoard(restaurant3, member1, reservation3);

        boardRepository.saveAll(List.of(board1, board2, board3));

        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag3 = BoardTag.builder().tag(tag3).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag4 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag5 = BoardTag.builder().tag(tag3).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag6 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();

        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));

        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .tagIds(tags.stream().map(Tag::getId).collect(Collectors.toList()))
                .build();

        // when
        Page<RestaurantSearchResponseDto> result = restaurantService.searchRestaurantsV1(requestDto);

        // then
        Assertions.assertThat(result.getContent()).hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당");
    }

    @Test
    @DisplayName("QueryDSL - 4개 이상의 키워드를 선택해 검색하면 예외가 발생한다.")
    void searchByTagOver3UsingQueryDSL() {
        // given
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .tagIds(List.of(1L,2L,3L,4L))
                .build();

        // when & then
        Assertions.assertThatThrownBy(() -> restaurantService.searchRestaurantsV1(requestDto))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("태그는 최대 3개까지 선택 가능합니다.");
    }


    @DisplayName("아무것도 없이 검색하면 식당 목록을 찾는다.")
    @Test
    void searchWithoutAnythingUsingQueryDSL() {
        // given
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(null)
                .tagIds(null)
                .build();

        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant1.getRestaurantImages().add(restaurantImage2);
        restaurant1.getRestaurantImages().add(restaurantImage3);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3));

        // when
        Page<RestaurantSearchResponseDto> restaurants = restaurantService.searchRestaurantsV1(requestDto);

        // then
        Assertions.assertThat(restaurants.getContent()).hasSize(3)
                .extracting("name")
                .containsExactlyElementsOf(List.of("골목 식당", "바다 식당","달콤한 카페"));
    }

    @DisplayName("예약이 많은 순으로 식당을 찾는다.")
    @Test
    void orderByReservation() {
        // given
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(null)
                .tagIds(null)
                .sort("boardCount")
                .build();

        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));

        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant2, member1, reservation2);
        Board board3 = createBoard(restaurant3, member1, reservation3);
        Board board4 = createBoard(restaurant3, member2, reservation4);
        Board board5 = createBoard(restaurant3, member1, reservation5);
        Board board6 = createBoard(restaurant4, member1, reservation6);
        Board board7 = createBoard(restaurant4, member2, reservation7);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();

        List<Tag> tags = tagRepository.saveAll(List.of(tag1, tag2, tag3));

        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag4 = BoardTag.builder().tag(tag2).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();

        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));

        // when
        Page<RestaurantSearchResponseDto> restaurants = restaurantService.searchRestaurantsV1(requestDto);

        // then
        Assertions.assertThat(restaurants.getContent()).hasSize(4)
                .extracting("name")
                .containsExactlyElementsOf(List.of("달콤한 카페", "조용한 카페","골목 식당","바다 식당"));
    }

    @DisplayName("식당을 키워드로 검색하고 게시글 많은 순으로 반환한다.")
    @Test
    void searchByKeywordOrderByBoardNumberDesc() {
        //given
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(null)
                .sort("boardCount")
                .build();

        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));

        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant1, member1, reservation2);
        Board board3 = createBoard(restaurant1, member1, reservation3);
        Board board4 = createBoard(restaurant2, member2, reservation3);
        Board board5 = createBoard(restaurant3, member1, reservation3);
        Board board6 = createBoard(restaurant4, member1, reservation4);
        Board board7 = createBoard(restaurant4, member2, reservation4);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        //when

        Page<RestaurantSearchResponseDto> restaurantSearchResponseDtos = restaurantService.searchRestaurantsV1(
                requestDto);

        //then
        Assertions.assertThat(restaurantSearchResponseDtos.getContent()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당", "바다 식당");
    }

    @DisplayName("식당을 키워드와 태그로 검색하고 게시글 많은 순으로 반환한다.")
    @Test
    void searchByKeywordAndTagsOrderByBoardNumberDesc() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot2, member1, restaurant2);
        Reservation reservation3 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));

        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant1, member1, reservation2);
        Board board3 = createBoard(restaurant1, member1, reservation3);
        Board board4 = createBoard(restaurant2, member2, reservation3);
        Board board5 = createBoard(restaurant3, member1, reservation3);
        Board board6 = createBoard(restaurant4, member1, reservation4);
        Board board7 = createBoard(restaurant4, member2, reservation4);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();

        List<Tag> tags = tagRepository.saveAll(List.of(tag1, tag2, tag3));

        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag4 = BoardTag.builder().tag(tag2).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();

        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));
        //when
        List<Long> tagIds = tags.stream()
                .filter(tag -> tag.getName().equals(tag1.getName())
                        || tag.getName().equals(tag2.getName()))
                .map(Tag::getId)
                .collect(Collectors.toList());
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(tagIds)
                .sort("boardCount")
                .build();
        Page<RestaurantSearchResponseDto> restaurantSearchResponseDtos = restaurantService.searchRestaurantsV1(
                requestDto);

        //then
        Assertions.assertThat(restaurantSearchResponseDtos.getContent()).hasSize(2)
                .extracting("name")
                .containsExactly("골목 식당", "바다 식당");
    }

    @DisplayName("식당을 예약 많은 순으로 반환한다.")
    @Test
    void OrderByReservationNumberDesc() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation3 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));

        //when
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(null)
                .tagIds(null)
                .sort("reservationCount")
                .build();
        Page<RestaurantSearchResponseDto> restaurants = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurants.getContent()).hasSize(4)
                .extracting("name")
                .containsExactly("골목 식당", "달콤한 카페", "조용한 카페", "바다 식당");
    }
    @DisplayName("키워드로 식당을 검색하고 예약 많은 순으로 반환한다.")
    @Test
    void SearchByKeywordOrderByReservationNumberDesc() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation3 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));

        //when
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(null)
                .sort("reservationCount")
                .build();
        Page<RestaurantSearchResponseDto> restaurants = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurants.getContent()).hasSize(2)
                .extracting("name")
                .containsExactly("골목 식당", "바다 식당");
    }

    @DisplayName("키워드와 태그로 식당을 검색하고 예약 많은 순으로 반환한다.")
    @Test
    void SearchByKeywordAndTagsOrderByReservationNumberDesc() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(List.of(restaurant1, restaurant2, restaurant3, restaurant4));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation3 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));
        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant1, member1, reservation2);
        Board board3 = createBoard(restaurant1, member1, reservation3);
        Board board4 = createBoard(restaurant2, member2, reservation3);
        Board board5 = createBoard(restaurant3, member1, reservation3);
        Board board6 = createBoard(restaurant4, member1, reservation4);
        Board board7 = createBoard(restaurant4, member2, reservation4);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();

        List<Tag> tags = tagRepository.saveAll(List.of(tag1, tag2, tag3));

        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag4 = BoardTag.builder().tag(tag2).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();

        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));

        //when
        List<Long> tagIds = tags.stream()
                .filter(tag -> tag.getName().equals(tag1.getName())
                        || tag.getName().equals(tag2.getName()))
                .map(Tag::getId)
                .collect(Collectors.toList());
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(tagIds)
                .sort("reservationCount")
                .build();
        Page<RestaurantSearchResponseDto> restaurants = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurants.getContent()).hasSize(2)
                .extracting("name")
                .containsExactly("골목 식당", "바다 식당");
    }


    @DisplayName("운영중인 식당만 반환한다.")
    @Test
    void filteredOnlyOperating() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);
        restaurant5.getRestaurantImages().add(restaurantImage5);
        restaurant6.getRestaurantImages().add(restaurantImage6);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(
                List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));

        RestaurantOperatingHour restaurantOperatingHour1 = createOpenRestaurantHour(restaurant1);
        RestaurantOperatingHour restaurantOperatingHour2 = createOpenRestaurantHour(restaurant2);
        RestaurantOperatingHour restaurantOperatingHour3 = createOpenRestaurantHour(restaurant3);
        RestaurantOperatingHour restaurantOperatingHour4 = createClosedRestaurantHour(restaurant4);
        RestaurantOperatingHour restaurantOperatingHour5 = createClosedRestaurantHour(restaurant5);
        RestaurantOperatingHour restaurantOperatingHour6 = createClosedRestaurantHour(restaurant6);

        restaurantOperatingHourRepository.saveAll(
                List.of(restaurantOperatingHour1, restaurantOperatingHour2, restaurantOperatingHour3,
                        restaurantOperatingHour4, restaurantOperatingHour5, restaurantOperatingHour6));
        //when
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(null)
                .tagIds(null)
                .onlyOperating(true)
                .sort(null)
                .build();
        Page<RestaurantSearchResponseDto> restaurantSearchResponseDtos = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurantSearchResponseDtos.getContent()).hasSize(3)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당", "바다 식당", "달콤한 카페");
    }

    @DisplayName("키워드로 식당을 검색하고 운영중인 식당만 반환한다.")
    @Test
    void SearchKeywordFilteredOnlyOperating() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);
        restaurant5.getRestaurantImages().add(restaurantImage5);
        restaurant6.getRestaurantImages().add(restaurantImage6);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(
                List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));

        RestaurantOperatingHour restaurantOperatingHour1 = createOpenRestaurantHour(restaurant1);
        RestaurantOperatingHour restaurantOperatingHour2 = createOpenRestaurantHour(restaurant2);
        RestaurantOperatingHour restaurantOperatingHour3 = createOpenRestaurantHour(restaurant3);
        RestaurantOperatingHour restaurantOperatingHour4 = createClosedRestaurantHour(restaurant4);
        RestaurantOperatingHour restaurantOperatingHour5 = createClosedRestaurantHour(restaurant5);
        RestaurantOperatingHour restaurantOperatingHour6 = createClosedRestaurantHour(restaurant6);

        restaurantOperatingHourRepository.saveAll(
                List.of(restaurantOperatingHour1, restaurantOperatingHour2, restaurantOperatingHour3,
                        restaurantOperatingHour4, restaurantOperatingHour5, restaurantOperatingHour6));
        //when
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(null)
                .sort(null)
                .onlyOperating(true)
                .build();
        Page<RestaurantSearchResponseDto> restaurantSearchResponseDtos = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurantSearchResponseDtos.getContent()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당", "바다 식당");
    }

    @DisplayName("키워드와 태그로 식당을 검색하고 운영중인 식당만 반환한다.")
    @Test
    void SearchKeywordAndTagsFilteredOnlyOperating() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);
        restaurant5.getRestaurantImages().add(restaurantImage5);
        restaurant6.getRestaurantImages().add(restaurantImage6);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(
                List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));

        RestaurantOperatingHour restaurantOperatingHour1 = createOpenRestaurantHour(restaurant1);
        RestaurantOperatingHour restaurantOperatingHour2 = createOpenRestaurantHour(restaurant2);
        RestaurantOperatingHour restaurantOperatingHour3 = createOpenRestaurantHour(restaurant3);
        RestaurantOperatingHour restaurantOperatingHour4 = createClosedRestaurantHour(restaurant4);
        RestaurantOperatingHour restaurantOperatingHour5 = createClosedRestaurantHour(restaurant5);
        RestaurantOperatingHour restaurantOperatingHour6 = createClosedRestaurantHour(restaurant6);

        restaurantOperatingHourRepository.saveAll(
                List.of(restaurantOperatingHour1, restaurantOperatingHour2, restaurantOperatingHour3,
                        restaurantOperatingHour4, restaurantOperatingHour5, restaurantOperatingHour6));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation3 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));
        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant1, member1, reservation2);
        Board board3 = createBoard(restaurant1, member1, reservation3);
        Board board4 = createBoard(restaurant2, member2, reservation3);
        Board board5 = createBoard(restaurant3, member1, reservation3);
        Board board6 = createBoard(restaurant4, member1, reservation4);
        Board board7 = createBoard(restaurant4, member2, reservation4);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();

        List<Tag> tags = tagRepository.saveAll(List.of(tag1, tag2, tag3));

        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag4 = BoardTag.builder().tag(tag2).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();

        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));

        //when
        List<Long> tagIds = tags.stream()
                .filter(tag -> tag.getName().equals(tag1.getName())
                        || tag.getName().equals(tag2.getName()))
                .map(Tag::getId)
                .collect(Collectors.toList());
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(tagIds)
                .sort(null)
                .onlyOperating(true)
                .build();
        Page<RestaurantSearchResponseDto> restaurantSearchResponseDtos = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurantSearchResponseDtos.getContent()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당", "바다 식당");
    }


    @DisplayName("키워드와 태그로 식당을 검색하고 운영중인 식당을 게시글 많은 순으로 반환한다.")
    @Test
    void SearchKeywordAndTagsFilteredOnlyOperatingOrderByBoardNumberDesc() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);
        restaurant5.getRestaurantImages().add(restaurantImage5);
        restaurant6.getRestaurantImages().add(restaurantImage6);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(
                List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));

        RestaurantOperatingHour restaurantOperatingHour1 = createOpenRestaurantHour(restaurant1);
        RestaurantOperatingHour restaurantOperatingHour2 = createOpenRestaurantHour(restaurant2);
        RestaurantOperatingHour restaurantOperatingHour3 = createOpenRestaurantHour(restaurant3);
        RestaurantOperatingHour restaurantOperatingHour4 = createClosedRestaurantHour(restaurant4);
        RestaurantOperatingHour restaurantOperatingHour5 = createClosedRestaurantHour(restaurant5);
        RestaurantOperatingHour restaurantOperatingHour6 = createClosedRestaurantHour(restaurant6);

        restaurantOperatingHourRepository.saveAll(
                List.of(restaurantOperatingHour1, restaurantOperatingHour2, restaurantOperatingHour3,
                        restaurantOperatingHour4, restaurantOperatingHour5, restaurantOperatingHour6));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation3 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));
        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant1, member1, reservation2);
        Board board3 = createBoard(restaurant1, member1, reservation3);
        Board board4 = createBoard(restaurant2, member2, reservation4);
        Board board5 = createBoard(restaurant2, member1, reservation5);
        Board board6 = createBoard(restaurant2, member1, reservation6);
        Board board7 = createBoard(restaurant2, member2, reservation7);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();

        List<Tag> tags = tagRepository.saveAll(List.of(tag1, tag2, tag3));

        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag4 = BoardTag.builder().tag(tag2).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();

        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));

        //when
        List<Long> tagIds = tags.stream()
                .filter(tag -> tag.getName().equals(tag1.getName())
                        || tag.getName().equals(tag2.getName()))
                .map(Tag::getId)
                .collect(Collectors.toList());
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(tagIds)
                .sort("BoardCount")
                .build();
        Page<RestaurantSearchResponseDto> restaurantSearchResponseDtos = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurantSearchResponseDtos.getContent()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("바다 식당", "골목 식당");
    }

    @DisplayName("키워드와 태그로 식당을 검색하고 운영중인 식당을 예약 많은 순으로 반환한다.")
    @Test
    void SearchKeywordAndTagsFilteredOnlyOperatingOrderByReservationNumberDesc() {
        //given
        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder().name("한식").build();
        RestaurantCategory restaurantCategory2 = RestaurantCategory.builder().name("카페").build();
        RestaurantCategory restaurantCategory3 = RestaurantCategory.builder().name("양식").build();

        Restaurant restaurant1 = createRestaurant("골목 식당", "강남구 강남대로 11", restaurantCategory1);
        Restaurant restaurant2 = createRestaurant("바다 식당", "서초구 서초대로 22", restaurantCategory1);
        Restaurant restaurant3 = createRestaurant("달콤한 카페", "도봉구 도봉대로 33", restaurantCategory2);
        Restaurant restaurant4 = createRestaurant("조용한 카페", "강남구 삼성대로 33", restaurantCategory2);
        Restaurant restaurant5 = createRestaurant("케이크가 맛있는 카페", " 도봉대로 33", restaurantCategory2);
        Restaurant restaurant6 = createRestaurant("경양식집", "도봉구 도봉대로 33", restaurantCategory3);
        RestaurantImage restaurantImage1 = createRestaurantImage(restaurant1, "https://example.com/image1.jpg");
        RestaurantImage restaurantImage2 = createRestaurantImage(restaurant2, "https://example.com/image2.jpg");
        RestaurantImage restaurantImage3 = createRestaurantImage(restaurant3, "https://example.com/image3.jpg");
        RestaurantImage restaurantImage4 = createRestaurantImage(restaurant4, "https://example.com/image4.jpg");
        RestaurantImage restaurantImage5 = createRestaurantImage(restaurant5, "https://example.com/image5.jpg");
        RestaurantImage restaurantImage6 = createRestaurantImage(restaurant6, "https://example.com/image6.jpg");
        restaurant1.getRestaurantImages().add(restaurantImage1);
        restaurant2.getRestaurantImages().add(restaurantImage2);
        restaurant3.getRestaurantImages().add(restaurantImage3);
        restaurant4.getRestaurantImages().add(restaurantImage4);
        restaurant5.getRestaurantImages().add(restaurantImage5);
        restaurant6.getRestaurantImages().add(restaurantImage6);

        restaurantCategoryRepository.saveAll(List.of(restaurantCategory1, restaurantCategory2, restaurantCategory3));
        restaurantRepository.saveAll(
                List.of(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6));

        RestaurantOperatingHour restaurantOperatingHour1 = createOpenRestaurantHour(restaurant1);
        RestaurantOperatingHour restaurantOperatingHour2 = createOpenRestaurantHour(restaurant2);
        RestaurantOperatingHour restaurantOperatingHour3 = createOpenRestaurantHour(restaurant3);
        RestaurantOperatingHour restaurantOperatingHour4 = createClosedRestaurantHour(restaurant4);
        RestaurantOperatingHour restaurantOperatingHour5 = createClosedRestaurantHour(restaurant5);
        RestaurantOperatingHour restaurantOperatingHour6 = createClosedRestaurantHour(restaurant6);

        restaurantOperatingHourRepository.saveAll(
                List.of(restaurantOperatingHour1, restaurantOperatingHour2, restaurantOperatingHour3,
                        restaurantOperatingHour4, restaurantOperatingHour5, restaurantOperatingHour6));

        Member member1 = createMember("member1@gmail.com", "홍길동");
        Member member2 = createMember("member2@gmail.com", "고길동");

        memberRepository.saveAll(List.of(member1, member2));

        ReservationSlot reservationSlot1 = createReservationSlot(restaurant1);
        ReservationSlot reservationSlot2 = createReservationSlot(restaurant2);
        ReservationSlot reservationSlot3 = createReservationSlot(restaurant3);
        ReservationSlot reservationSlot4 = createReservationSlot(restaurant4);

        reservationSlotRepository.saveAll(
                List.of(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4));

        Reservation reservation1 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation2 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation3 = createReservation(reservationSlot1, member1, restaurant1);
        Reservation reservation4 = createReservation(reservationSlot3, member2, restaurant3);
        Reservation reservation5 = createReservation(reservationSlot3, member1, restaurant3);
        Reservation reservation6 = createReservation(reservationSlot4, member1, restaurant4);
        Reservation reservation7 = createReservation(reservationSlot4, member2, restaurant4);

        reservationRepository.saveAll(
                List.of(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                        reservation7));
        Board board1 = createBoard(restaurant1, member1, reservation1);
        Board board2 = createBoard(restaurant1, member1, reservation2);
        Board board3 = createBoard(restaurant1, member1, reservation3);
        Board board4 = createBoard(restaurant2, member2, reservation4);
        Board board5 = createBoard(restaurant2, member1, reservation5);
        Board board6 = createBoard(restaurant2, member1, reservation6);
        Board board7 = createBoard(restaurant2, member2, reservation7);

        boardRepository.saveAll(List.of(board1, board2, board3, board4, board5, board6, board7));

        Tag tag1 = Tag.builder().name("분위기가 좋아요").build();
        Tag tag2 = Tag.builder().name("음식이 맛있어요").build();
        Tag tag3 = Tag.builder().name("가성비가 좋아요").build();

        List<Tag> tags = tagRepository.saveAll(List.of(tag1, tag2, tag3));

        BoardTag boardTag1 = BoardTag.builder().tag(tag1).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag2 = BoardTag.builder().tag(tag2).restaurant(restaurant1).board(board1).build();
        BoardTag boardTag3 = BoardTag.builder().tag(tag1).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag4 = BoardTag.builder().tag(tag2).restaurant(restaurant2).board(board2).build();
        BoardTag boardTag5 = BoardTag.builder().tag(tag2).restaurant(restaurant3).board(board3).build();
        BoardTag boardTag6 = BoardTag.builder().tag(tag3).restaurant(restaurant3).board(board3).build();

        boardTagRepository.saveAll(List.of(boardTag1, boardTag2, boardTag3, boardTag4, boardTag5, boardTag6));

        //when
        List<Long> tagIds = tags.stream()
                .filter(tag -> tag.getName().equals(tag1.getName())
                        || tag.getName().equals(tag2.getName()))
                .map(Tag::getId)
                .collect(Collectors.toList());
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .tagIds(tagIds)
                .sort("BoardCount")
                .build();
        Page<RestaurantSearchResponseDto> restaurantSearchResponseDtos = restaurantService.searchRestaurantsV1(requestDto);

        //then
        Assertions.assertThat(restaurantSearchResponseDtos.getContent()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당", "바다 식당");
    }


    private static RestaurantImage createRestaurantImage(Restaurant restaurant1, String url) {
        return RestaurantImage.builder()
                .imageUrl(url)
                .restaurant(restaurant1)
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

    private Menu createMenu(String name, int price, Restaurant restaurant) {
        return Menu.builder()
                .name(name)
                .price(BigDecimal.valueOf(price))
                .restaurant(restaurant)
                .build();
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

    private static RestaurantOperatingHour createOpenRestaurantHour(Restaurant restaurant) {
        return RestaurantOperatingHour.builder().restaurant(restaurant)
                .openTime(LocalTime.of(0, 0))
                .closeTime(LocalTime.of(23, 59))
                .dayOfWeek(DayOfWeek.fromJavaDayOfWeek(LocalDate.now().getDayOfWeek())).build();
    }

    private static RestaurantOperatingHour createClosedRestaurantHour(Restaurant restaurant) {
        return RestaurantOperatingHour.builder().restaurant(restaurant)
                .openTime(LocalTime.of(23, 0))
                .closeTime(LocalTime.of(23, 0))
                .dayOfWeek(DayOfWeek.fromJavaDayOfWeek(LocalDate.now().getDayOfWeek())).build();
    }

}
