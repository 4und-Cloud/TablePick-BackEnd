package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.board.entity.QBoard;
import com.goorm.tablepick.domain.board.entity.QBoardTag;
import com.goorm.tablepick.domain.reservation.entity.QReservation;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import com.goorm.tablepick.domain.restaurant.entity.QMenu;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurant;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurantCategory;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurantImage;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurantOperatingHour;
import com.goorm.tablepick.domain.tag.entity.QTag;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.TimeTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public RestaurantRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<RestaurantSearchResponseDto> searchRestaurantResult(
            String keyword,
            List<Long> tagIds,
            String sort,
            Boolean onlyOperating,
            Integer radiusKm,
            Double lat,
            Double lng,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        QRestaurant r = QRestaurant.restaurant;
        QRestaurantCategory rc = QRestaurantCategory.restaurantCategory;
        QBoardTag bt = QBoardTag.boardTag;
        QBoard b = QBoard.board;
        QReservation rs = QReservation.reservation;
        QRestaurantOperatingHour roh = QRestaurantOperatingHour.restaurantOperatingHour;
        QMenu m = QMenu.menu;
        QRestaurantImage image = QRestaurantImage.restaurantImage;
        QTag tag = QTag.tag;

        BooleanBuilder where = new BooleanBuilder();

        // 키워드 필터
        if (StringUtils.hasText(keyword)) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            where.and(
                    r.name.lower().like(kw)
                            .or(r.address.lower().like(kw))
                            .or(
                                    JPAExpressions.selectOne()
                                            .from(m)
                                            .where(m.restaurant.eq(r)
                                                    .and(m.name.lower().like(kw)))
                                            .exists()
                            )
            );
        }

        // 영업중 필터
        if (Boolean.TRUE.equals(onlyOperating)) {
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            TimeTemplate<LocalTime> now = Expressions.timeTemplate(LocalTime.class, "CURTIME()");
            where.and(
                    JPAExpressions.selectOne()
                            .from(roh)
                            .where(
                                    roh.restaurant.eq(r),
                                    roh.dayOfWeek.stringValue().eq(today.name()),
                                    roh.isHoliday.isFalse(),
                                    now.between(roh.openTime, roh.closeTime)
                            )
                            .exists()
            );
        }

        // 반경 필터
        if (radiusKm != null && lat != null && lng != null) {
            double radius = radiusKm * 1000;
            where.and(
                    Expressions.numberTemplate(Double.class,
                            "ST_Distance_Sphere(point({0},{1}), point({2},{3}))",
                            r.xcoordinate, r.ycoordinate, lng, lat
                    ).loe(radius)
            );
        }

        // 가격 필터
        if (minPrice != null) {
            where.and(
                    JPAExpressions.select(m.price.min())
                            .from(m)
                            .where(m.restaurant.eq(r))
                            .gt(minPrice)
            );
        }
        if (maxPrice != null) {
            where.and(
                    JPAExpressions.select(m.price.max())
                            .from(m)
                            .where(m.restaurant.eq(r))
                            .lt(maxPrice)
            );
        }

        // 메인 쿼리: 기본 정보
        var query = queryFactory
                .select(Projections.fields(
                        RestaurantSearchResponseDto.class,
                        r.id,
                        r.name,
                        r.address,
                        rc.name.as("restaurantCategory")
                ))
                .from(r)
                .join(r.restaurantCategory, rc)
                .where(where);

        // 태그 필터링 & 그룹핑
        if (tagIds != null && !tagIds.isEmpty()) {
            query.leftJoin(bt)
                    .on(bt.restaurant.eq(r), bt.tag.id.in(tagIds));
        }
        query.groupBy(r.id, r.name, r.address, rc.name);
        if (tagIds != null && !tagIds.isEmpty()) {
            query.having(bt.tag.id.countDistinct().eq((long) tagIds.size()));
        }

        // 정렬
        if ("boardCount".equals(sort)) {
            query.leftJoin(b).on(b.restaurantId.eq(r.id))
                    .orderBy(b.id.countDistinct().desc());
        } else if ("reservationCount".equals(sort)) {
            query.leftJoin(rs).on(rs.restaurant.eq(r))
                    .orderBy(rs.id.countDistinct().desc());
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            query.orderBy(bt.tag.id.countDistinct().desc());
        }

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        List<RestaurantSearchResponseDto> content = query.fetch();

        // 이미지 매핑
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
                        (existing, fallback) -> existing
                ));

        // 보드 태그 매핑
        Map<Long, List<String>> tagMap = queryFactory
                .selectDistinct(bt.restaurant.id, tag.name)
                .from(bt)
                .join(tag).on(tag.id.eq(bt.tag.id))
                .where(bt.restaurant.id.in(ids))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(bt.restaurant.id),
                        Collectors.mapping(
                                tuple -> tuple.get(tag.name),
                                Collectors.toList()
                        )
                ));

        // DTO에 이미지와 태그 설정
        content.forEach(dto -> {
            dto.setRestaurantImage(imageMap.get(dto.getId()));
            dto.setBoardTags(tagMap.getOrDefault(dto.getId(), List.of()));
        });

        // 카운트 쿼리
        Long total = queryFactory
                .select(r.countDistinct())
                .from(r)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}