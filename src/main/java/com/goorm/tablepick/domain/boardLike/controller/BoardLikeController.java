package com.goorm.tablepick.domain.boardLike.controller;

import com.goorm.tablepick.domain.boardLike.service.BoardLikeService;
import com.goorm.tablepick.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boardLike")
@RequiredArgsConstructor
public class BoardLikeController {
    private final BoardLikeService boardLikeService;

    @PostMapping
    public ResponseEntity<Void> save(@AuthenticationPrincipal CustomUserDetails userDetails, Long boardId) {
        boardLikeService.LikeBoard(userDetails.getId(), boardId);

        return ResponseEntity.ok().build();
    }

}
