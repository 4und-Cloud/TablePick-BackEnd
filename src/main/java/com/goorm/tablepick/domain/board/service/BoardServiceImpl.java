package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.response.BoardCreateResponseDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.RestaurantBoardResponseDto;
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
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
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

    @Value("${project.upload.board-image-path}")
    private String boardImagePath;

    @Override
    public List<BoardListResponseDto> getBoardsForMainPage() {
        Pageable pageable = PageRequest.of(0, 4);
        Page<Object[]> boardPage = boardRepository.findBoardsWithImagesOrderByCreatedAtDesc(pageable);

        return boardPage.getContent().stream()
                .map(objects -> {
                    Board board = (Board) objects[0];
                    Restaurant restaurant = (Restaurant) objects[1];
                    RestaurantCategory category = (RestaurantCategory) objects[2];
                    return BoardListResponseDto.from(board, restaurant, category);
                })
                .toList();
    }

    @Override
    public PagedBoardListResponseDto getBoards(int page, int size) {
        if (page < 0) {
            page = 0;
        }


        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> boardPage = boardRepository.findBoardsWithImagesOrderByCreatedAtDesc(pageable);

        List<BoardListResponseDto> dtoList = boardPage.getContent().stream()
                .map(objects -> {
                    Board board = (Board) objects[0];
                    Restaurant restaurant = (Restaurant) objects[1];
                    RestaurantCategory category = (RestaurantCategory) objects[2];
                    return BoardListResponseDto.from(board, restaurant, category);
                })
                .toList();

        return new PagedBoardListResponseDto(dtoList, boardPage);
    }

    @Override
    public BoardDetailResponseDto getBoardDetail(Long boardId) {
        return boardRepository.findById(boardId)
                .map(BoardDetailResponseDto::from)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다."));
    }

    @Override
    public List<RestaurantBoardResponseDto> getBoardsByRestaurant(Long restaurantId) {
        Pageable pageable = PageRequest.of(0, 4);
        Page<Board> boards = boardRepository.findBoardsWithImagesByRestaurantId(restaurantId, pageable);

        return boards.getContent().stream()
                .map(RestaurantBoardResponseDto::from)
                .toList();
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

        // 2. Board 저장 (restaurantId 추가)
        Board board = Board.builder()
                .content(dto.getContent())
                .reservation(reservation)
                .member(member)
                .restaurantId(reservation.getRestaurant().getId())
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

    @Override
    @Transactional
    public void deleteBoard(Long boardId, Member member) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!board.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("게시글 작성자만 삭제할 수 있습니다.");
        }

        deletePhysicalImageFiles(board.getBoardImages());

        boardRepository.delete(board);
    }


    // 이미지 저장 메서드
    private String saveImage(MultipartFile image) {
        String originalFileName = image.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String storeFileName = UUID.randomUUID() + extension;
        String uploadDir = boardImagePath;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storeFileName);
            Files.write(filePath, image.getBytes());

            return storeFileName;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    // 확장자 추출 메서드 추가
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex);
    }

    // 물리적 파일 삭제 메서드 추가
    private void deletePhysicalImageFiles(List<BoardImage> boardImages) {
        for (BoardImage boardImage : boardImages) {
            try {
                String fileName = boardImage.getImageUrl();
                Path filePath = Paths.get(boardImagePath).resolve(fileName);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("이미지 파일 삭제 성공: {}", fileName);
                } else {
                    log.warn("삭제할 이미지 파일이 존재하지 않음: {}", fileName);
                }
            } catch (IOException e) {
                log.error("이미지 파일 삭제 실패: {}", boardImage.getImageUrl(), e);
                // 파일 삭제 실패해도 DB 삭제는 계속 진행
            }
        }
    }
}
