package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.board.entity.QBoard;
import com.goorm.tablepick.domain.board.entity.QBoardTag;
import com.goorm.tablepick.domain.reservation.entity.QReservation;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import com.goorm.tablepick.domain.restaurant.entity.QMenu;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurant;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurantImage;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurantOperatingHour;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.TimeTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

;

@Repository
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public RestaurantRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 페이징 없이 조건에 맞는 모든 식당을 조회합니다.
     *
     * @param keyword       검색어 (name, address, menu.name)
     * @param tagIds        필터할 태그 ID 리스트 (빈 리스트 또는 null 이면 태그 필터 미적용)
     * @param sort          "boardCount" 또는 "reservationCount" 정렬 키 (null 이면 정렬 미적용)
     * @param onlyOperating true 면 영업 중인 식당만 조회
     */
    @Override
    public Page<RestaurantSearchResponseDto> searchRestaurantResult(
            String keyword,
            List<Long> tagIds,
            String sort,
            Boolean onlyOperating,
            Pageable pageable
    ) {
        QRestaurant restaurant = QRestaurant.restaurant;
        QBoardTag boardTag = QBoardTag.boardTag;
        QBoard board = QBoard.board;
        QReservation reservation = QReservation.reservation;
        QRestaurantOperatingHour oh = QRestaurantOperatingHour.restaurantOperatingHour;
        QMenu menu = QMenu.menu;
        QRestaurantImage image = QRestaurantImage.restaurantImage;

        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasTags = tagIds != null && !tagIds.isEmpty();
        int tagCount = hasTags ? tagIds.size() : 0;

        BooleanBuilder where = new BooleanBuilder();

        if (hasKeyword) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            where.and(
                    restaurant.name.likeIgnoreCase(kw)
                            .or(restaurant.address.likeIgnoreCase(kw))
                            .or(JPAExpressions.selectOne()
                                    .from(menu)
                                    .where(menu.restaurant.eq(restaurant)
                                            .and(menu.name.likeIgnoreCase(kw)))
                                    .exists())
            );
        }

        if (Boolean.TRUE.equals(onlyOperating)) {
            TimeTemplate<LocalTime> now = Expressions.timeTemplate(LocalTime.class, "CURRENT_TIME");
            DayOfWeek dow = LocalDate.now().getDayOfWeek();
            where.and(
                    JPAExpressions.selectOne()
                            .from(oh)
                            .where(
                                    oh.restaurant.eq(restaurant),
                                    oh.dayOfWeek.stringValue().eq(dow.name()),
                                    now.between(oh.openTime, oh.closeTime)
                            )
                            .exists()
            );
        }

        JPAQuery<RestaurantSearchResponseDto> contentQuery = queryFactory
                .select(Projections.fields(
                        RestaurantSearchResponseDto.class,
                        restaurant.id,
                        restaurant.name,
                        restaurant.address,
                        restaurant.restaurantCategory.name.as("restaurantCategory")
                ))
                .from(restaurant)
                .where(where);

        if (hasTags) {
            contentQuery.leftJoin(boardTag)
                    .on(boardTag.restaurant.eq(restaurant),
                            boardTag.tag.id.in(tagIds));
        }

        boolean sortByBoard = "boardCount".equals(sort);
        boolean sortByResv = "reservationCount".equals(sort);
        if (sortByBoard) {
            contentQuery.leftJoin(board).on(board.restaurantId.eq(restaurant.id));
        }
        if (sortByResv) {
            contentQuery.leftJoin(reservation).on(reservation.restaurant.eq(restaurant));
        }

        if (hasTags) {
            contentQuery.having(boardTag.tag.id.countDistinct().eq((long) tagCount));
        }

        contentQuery.groupBy(
                restaurant.id,
                restaurant.name,
                restaurant.address,
                restaurant.restaurantCategory.name
        );

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (sortByBoard) {
            orders.add(board.id.countDistinct().desc());
        } else if (sortByResv) {
            orders.add(reservation.id.countDistinct().desc());
        }
        if (hasTags) {
            orders.add(boardTag.tag.id.countDistinct().desc());
        }
        if (!orders.isEmpty()) {
            contentQuery.orderBy(orders.toArray(new OrderSpecifier[0]));
        }

        contentQuery.limit(pageable.getPageSize());

        List<RestaurantSearchResponseDto> content = contentQuery.fetch();

        if (content.isEmpty()) {
            return new PageImpl<>(content, pageable, 0);
        }

        List<Long> ids = content.stream().map(RestaurantSearchResponseDto::getId).toList();

        Map<Long, String> imageMap = queryFactory
                .select(image.restaurant.id, image.imageUrl)
                .from(image)
                .where(image.restaurant.id.in(ids))
                .orderBy(image.id.asc())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(image.restaurant.id),
                        tuple -> tuple.get(image.imageUrl),
                        (a, b) -> a
                ));

        Map<Long, List<String>> tagMap = queryFactory
                .selectDistinct(boardTag.restaurant.id, boardTag.tag.name)
                .from(boardTag)
                .where(boardTag.restaurant.id.in(ids))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(boardTag.restaurant.id),
                        Collectors.mapping(
                                tuple -> tuple.get(boardTag.tag.name),
                                Collectors.toList()
                        )
                ));

        content.forEach(dto -> {
            dto.setRestaurantImage(imageMap.get(dto.getId()));
            dto.setBoardTags(tagMap.getOrDefault(dto.getId(), List.of()));
        });

        // count query
        Long count = queryFactory
                .select(restaurant.countDistinct())
                .from(restaurant)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }
}
