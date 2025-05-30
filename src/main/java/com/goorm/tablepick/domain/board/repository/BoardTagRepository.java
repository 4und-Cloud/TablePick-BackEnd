package com.goorm.tablepick.domain.board.repository;

import com.goorm.tablepick.domain.board.entity.BoardTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardTagRepository extends JpaRepository<BoardTag, Long> {
    @Query(value = "SELECT t.name, COUNT(t.id) as tag_count " +
            "FROM board_tag bt " +
            "JOIN tag t ON bt.tag_id = t.id " +
            "WHERE bt.restaurant_id = :restaurantId " +
            "GROUP BY t.id, t.name " +
            "ORDER BY tag_count DESC " +
            "LIMIT 5", nativeQuery = true)
    List<String> findTopTagsByRestaurantIdNative(@Param("restaurantId") Long restaurantId);
}
