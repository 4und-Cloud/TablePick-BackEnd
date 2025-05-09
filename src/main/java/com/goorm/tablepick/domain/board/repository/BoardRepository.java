package com.goorm.tablepick.domain.board.repository;

import com.goorm.tablepick.domain.board.entity.Board;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByMemberEmail(String memberEmail);
}
