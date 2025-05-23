package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.response.BoardCreateResponseDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardListResponseDto;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardImage;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.board.repository.BoardImageRepository;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final ReservationRepository reservationRepository;
    private final BoardImageRepository boardImageRepository;
    private final BoardTagRepository boardTagRepository;
    private final TagRepository tagRepository;

    @Value("${project.upload.path}")
    private String uploadBasePath;

    @Override
    public List<BoardListResponseDto> getBoardsForMainPage() {
        Pageable pageable = PageRequest.of(0, 4);
        Page<Board> boardPage = boardRepository.findBoardsWithImagesOrderByCreatedAtDesc(pageable);
        return boardPage.getContent().stream()
                .map(BoardListResponseDto::from)
                .toList();
    }

    @Override
    public PagedBoardListResponseDto getBoards(int page, int size) {
        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Board> boardPage = boardRepository.findBoardsWithImagesOrderByCreatedAtDesc(pageable);

        Page<BoardListResponseDto> dtoPage = boardPage.map(BoardListResponseDto::from);
        return new PagedBoardListResponseDto(dtoPage);
    }

    @Override
    public BoardDetailResponseDto getBoardDetail(Long boardId) {
        return boardRepository.findById(boardId)
                .map(BoardDetailResponseDto::from)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public BoardCreateResponseDto createBoard(BoardRequestDto dto, List<MultipartFile> images, Member member) {
        // 1. 예약 확인
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));

        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("예약한 사용자만 리뷰을 작성할 수 있습니다.");
        }

        // 2. Board 저장
        Board board = Board.builder()
                .content(dto.getContent())
                .reservation(reservation)
                .member(member)
                .build();
        boardRepository.save(board);

        // 3. 이미지 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String webPath = saveImage(image);
                BoardImage boardImage = BoardImage.builder()
                        .imageUrl(webPath)
                        .board(board)
                        .build();
                board.addImage(boardImage);
                boardImageRepository.save(boardImage);
            }
        }

        // 4. 태그 저장
        List<Long> tagIds = dto.getTagId();
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("태그는 최소 1개 이상 입력해야 합니다.");
        }

        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다: " + tagId));

            Restaurant restaurant = board.getReservation().getRestaurant();
            BoardTag boardTag = BoardTag.builder()
                    .board(board)
                    .tag(tag)
                    .restaurant(restaurant)
                    .build();

            board.addTag(boardTag);
            boardTagRepository.save(boardTag);
        }

        return BoardCreateResponseDto.builder()
                .boardId(board.getId())
                .message("리뷰가 작성되었습니다.")
                .build();
    }



    // 이미지 저장 메서드
    private String saveImage(MultipartFile image) {
        String originalFileName = image.getOriginalFilename();
        String storeFileName = UUID.randomUUID() + "_" + originalFileName;
        String uploadDir = uploadBasePath + "/images/review";

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storeFileName);
            Files.write(filePath, image.getBytes());

            return "/uploads/images/review/" + storeFileName;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }
}
