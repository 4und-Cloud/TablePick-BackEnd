package com.goorm.tablepick.domain.board.repository;

import com.goorm.tablepick.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 특정 회원 이메일로 게시글 조회
    List<Board> findAllByMemberEmail(String memberEmail);

    // BoardImage가 존재하고 imageUrl이 null이 아닌 게시글만 조회
    @Query("SELECT DISTINCT b FROM Board b JOIN b.boardImages i WHERE i.imageUrl IS NOT NULL ORDER BY b.createdAt DESC")
    Page<Board> findBoardsWithImagesOrderByCreatedAtDesc(Pageable pageable);

}