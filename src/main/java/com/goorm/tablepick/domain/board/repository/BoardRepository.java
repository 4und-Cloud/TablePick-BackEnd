package com.goorm.tablepick.domain.board.repository;

import com.goorm.tablepick.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    // 특정 회원 이메일로 게시글 조회
    List<Board> findAllByMemberEmail(String memberEmail);

    // 게시글을 최신순으로 정렬해서 모두 가져오기
    List<Board> findAllByOrderByCreatedAtDesc();  // 실제 필드명 사용
}