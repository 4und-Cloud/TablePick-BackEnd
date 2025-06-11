package com.goorm.tablepick.domain.tag.repository;

import com.goorm.tablepick.domain.board.entity.QBoardTag;
import com.goorm.tablepick.domain.tag.entity.QTag;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query(value = """
            SELECT t.name AS tagName
            FROM board_tag bt
            JOIN tag t ON bt.tag_id = t.id
            WHERE bt.restaurant_id = :restaurantId
            """, nativeQuery = true)
    List<String> findTagsByRestaurantId(@Param("restaurantId") Long restaurantId);

}
