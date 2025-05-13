package com.goorm.tablepick.domain.board.repository;

import com.goorm.tablepick.domain.board.entity.Board;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByMemberEmail(String memberEmail);

    @Query("SELECT b FROM Board b WHERE b.restaurant.restaurantCategory.id = :categoryId")
    Page<Board> findAllByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

}
