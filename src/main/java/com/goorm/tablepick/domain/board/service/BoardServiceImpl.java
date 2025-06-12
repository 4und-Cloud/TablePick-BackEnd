package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardCreateResponseDto;
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
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import com.goorm.tablepick.global.util.S3Uploader;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;
    private final BoardImageRepository boardImageRepository;
    private final BoardTagRepository boardTagRepository;
    private final TagRepository tagRepository;
    private final RestTemplate restTemplate;
    private final S3Uploader s3Uploader;
    private final S3Client s3Client;

    private static final String BUCKET_NAME = "tablepick-bucket";

    @Override
    public PagedBoardListResponseDto getBoards(int page, int size, Member member) {
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> boardPage;
        if (member != null) {
            long userId = member.getId();
            List<Long> recommendedBoardIds = getRecommendedBoardIds(userId, page, 30);
            boardPage = boardRepository.findBoardsByIdsInOrder(recommendedBoardIds, pageable);
        } else {
            boardPage = boardRepository.findBoardsWithImagesOrderByCreatedAtDesc(pageable);
        }
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
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다."));
        Restaurant restaurant = restaurantRepository.findById(board.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("해당 식당을 찾을 수 없습니다."));
        return BoardDetailResponseDto.from(board, restaurant);
    }

    @Override
    public List<BoardListResponseDto> getBoardsByRestaurant(Long restaurantId) {
        Pageable pageable = PageRequest.of(0, 6);
        Page<Object[]> boardPage = boardRepository.findBoardsWithImagesByRestaurantId(restaurantId, pageable);
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
    @Transactional
    public BoardCreateResponseDto createBoard(BoardRequestDto dto, List<MultipartFile> images, Member member) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("예약한 사용자만 리뷰를 작성할 수 있습니다.");
        }

        Board board = Board.builder()
                .content(dto.getContent())
                .reservation(reservation)
                .member(member)
                .restaurantId(reservation.getRestaurant().getId())
                .build();
        boardRepository.save(board);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                try {
                    String imageUrl = s3Uploader.upload(image);
                    BoardImage boardImage = BoardImage.builder()
                            .imageUrl(imageUrl)
                            .board(board)
                            .build();
                    board.addImage(boardImage);
                    boardImageRepository.save(boardImage);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 업로드 실패", e);
                }
            }
        }

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

        deleteS3ImageFiles(board.getBoardImages());
        boardRepository.delete(board);
    }

    private void deleteS3ImageFiles(List<BoardImage> boardImages) {
        for (BoardImage boardImage : boardImages) {
            try {
                String imageUrl = boardImage.getImageUrl();
                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(fileName)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
                log.info("S3 이미지 삭제 성공: {}", fileName);
            } catch (Exception e) {
                log.error("S3 이미지 삭제 실패: {}", boardImage.getImageUrl(), e);
            }
        }
    }

    public List<Long> getRecommendedBoardIds(Long userId, int page, int size) {
        String url = String.format("http://localhost:8000/post/recommend-for-user/%d?page=%d&size=%d", userId, page,
                size);
        try {
            ResponseEntity<List<Long>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Long>>() {
                    }
            );
            List<Long> boardIds = response.getBody();
            System.out.println("boardIds: " + boardIds);
            return (boardIds != null) ? boardIds : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.error("AI 서버 요청 실패: userId={}, status={}, response={}", userId, e.getStatusCode(),
                    e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("AI 서버에서 추천 게시글 ID를 가져오지 못했습니다. userId={}", userId, e);
            return Collections.emptyList();
        }
    }
}