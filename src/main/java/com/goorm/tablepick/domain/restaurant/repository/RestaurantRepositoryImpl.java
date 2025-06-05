package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.board.entity.QBoardTag;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import com.goorm.tablepick.domain.restaurant.entity.QMenu;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurant;
import com.goorm.tablepick.domain.restaurant.entity.QRestaurantImage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public RestaurantRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<RestaurantSearchResponseDto> searchRestaurantResult(
            String keyword,
            List<Long> tagIds,
            Pageable pageable
    ) {
        QRestaurant restaurant = QRestaurant.restaurant;
        QBoardTag boardTag = QBoardTag.boardTag;
        QRestaurantImage image = QRestaurantImage.restaurantImage;

        boolean hasKeyword = (keyword != null && !keyword.isBlank());
        boolean hasTags = (tagIds != null && !tagIds.isEmpty());
        int tagCount = hasTags ? tagIds.size() : 0;

        BooleanBuilder where = new BooleanBuilder();

        // ✅ 1. 키워드 조건 (EXISTS 사용)
        if (hasKeyword) {
            keyword = keyword.toLowerCase().trim();

            BooleanBuilder keywordCond = new BooleanBuilder();
            keywordCond.or(restaurant.name.like("%" + keyword + "%"));
            keywordCond.or(restaurant.address.like("%" + keyword + "%"));

            QMenu menu = QMenu.menu;

            // EXISTS 서브쿼리로 대체
            keywordCond.or(
                    JPAExpressions.selectOne()
                            .from(menu)
                            .where(
                                    menu.restaurant.id.eq(restaurant.id)
                                            .and(menu.name.like("%" + keyword + "%"))
                            )
                            .exists()
            );

            where.and(keywordCond);
        }

        // ✅ 2. 태그 필터 사전 추출
        List<Long> filteredRestaurantIds = null;
        if (hasTags) {
            filteredRestaurantIds = queryFactory
                    .select(boardTag.restaurant.id)
                    .from(boardTag)
                    .where(boardTag.tag.id.in(tagIds))
                    .groupBy(boardTag.restaurant.id)
                    .having(boardTag.tag.id.countDistinct().eq((long) tagCount))
                    .fetch();

            if (filteredRestaurantIds.isEmpty()) return List.of();

            where.and(restaurant.id.in(filteredRestaurantIds));
        }

        // ✅ 3. 메인 쿼리
        List<RestaurantSearchResponseDto> dtos = queryFactory
                .select(Projections.fields(
                        RestaurantSearchResponseDto.class,
                        restaurant.id,
                        restaurant.name,
                        restaurant.address,
                        restaurant.restaurantCategory.name.as("restaurantCategory")
                ))
                .from(restaurant)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // ✅ 4. ID 리스트 추출
        List<Long> ids = dtos.stream()
                .map(RestaurantSearchResponseDto::getId)
                .toList();

        if (ids.isEmpty()) return List.of();

        // ✅ 5. 이미지 맵핑 (최초 이미지만)
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
                        (first, second) -> first
                ));

        // ✅ 6. 태그 맵핑
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

        // ✅ 7. 결과 매핑
        dtos.forEach(dto -> {
            dto.setRestaurantImage(imageMap.get(dto.getId()));
            dto.setBoardTags(tagMap.getOrDefault(dto.getId(), List.of()));
        });

        return dtos;
    }
}