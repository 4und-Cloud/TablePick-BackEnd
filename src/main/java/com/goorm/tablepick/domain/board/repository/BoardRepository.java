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
    // 특정 회원 이메일로 게시글 조회
    List<Board> findAllByMemberEmail(String memberEmail);

    // 게시글을 최신순으로 정렬해서 모두 가져오기
    List<Board> findAllByOrderByCreatedAtDesc();  // 실제 필드명 사용

    @Query("SELECT b FROM Board b WHERE b.restaurant.restaurantCategory.id = :categoryId")
    Page<Board> findAllByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

}

