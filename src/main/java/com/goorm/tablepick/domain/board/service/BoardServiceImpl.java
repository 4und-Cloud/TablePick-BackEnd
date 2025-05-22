package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardCreateResponseDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardsResponseDto;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
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

    private final String uploadDir = "/Users/gihongjeong/Desktop/test_image_upload";

    @Override
    public List<BoardListResponseDto> getBoardsForMainPage() {
        Pageable pageable = PageRequest.of(0, 4, Sort.by("createdAt").descending());

        Page<Board> boardPage = boardRepository.findBoardsWithImages(pageable);
        return boardPage.getContent().stream()
                .map(BoardListResponseDto::from)
                .toList();
    }

    @Override
    public PagedBoardListResponseDto getBoards(int page, int size) {
        // page가 1보다 작으면 강제로 1로 설정 (or throw new IllegalArgumentException)
        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        // imageUrl 있는 게시글만 조회
        Page<Board> boardPage = boardRepository.findBoardsWithImages(pageable);

        return new PagedBoardListResponseDto(boardPage);
    }

    public List<BoardListResponseDto> getBoardList() {
        List<Board> boards = boardRepository.findAllByOrderByCreatedAtDesc();

        return boards.stream().map(board -> {
            // Reservation과 Restaurant null 체크 추가
            Reservation reservation = board.getReservation();
            Restaurant restaurant = reservation != null ? reservation.getRestaurant() : null;

            // 이미지 URL 처리
            String imageUrl = board.getBoardImages().stream()
                    .map(image -> {
                        if (image.getImageUrl() != null) return image.getImageUrl();
                        else return image.getStoreFileName(); // 대체용
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null); // 없으면 null

            // 태그 리스트
            List<String> tagNames = board.getBoardTags().stream()
                    .map(boardTag -> boardTag.getTag().getName())
                    .filter(Objects::nonNull)
                    .toList();

            return BoardListResponseDto.builder()
                    .id(board.getId())
                    .content(board.getContent())
                    .restaurantName(restaurant != null ? restaurant.getName() : null) // 수정
                    .restaurantAddress(restaurant != null ? restaurant.getAddress() : null) // 수정
                    .restaurantCategoryName(
                            restaurant != null && restaurant.getRestaurantCategory() != null
                                    ? restaurant.getRestaurantCategory().getName()
                                    : null
                    ) // 수정
                    .memberNickname(board.getMember().getNickname())
                    .memberProfileImage(board.getMember().getProfileImage())
                    .imageUrl(imageUrl) // 수정됨
                    .tagNames(tagNames)
                    .build();
        }).toList();
    }

    @Override
    public BoardDetailResponseDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        // Reservation과 Restaurant null 체크 추가
        Reservation reservation = board.getReservation();
        Restaurant restaurant = reservation != null ? reservation.getRestaurant() : null;

        Member member = board.getMember();

        List<String> imageUrls = board.getBoardImages().stream()

                .map(image -> {
                    // 우선 imageUrl이 있으면 사용, 없으면 storeFileName 사용
                    if (image.getImageUrl() != null) return image.getImageUrl();
                    return image.getStoreFileName();
                })
                .filter(Objects::nonNull) // null 제거
                .limit(3) // 최대 3장으로 제한
                .toList();


        List<String> tagNames = board.getBoardTags().stream()
                .map(boardTag -> boardTag.getTag().getName())
                .filter(Objects::nonNull) // (선택) null 태그 방지
                .toList();

        // NullPointer 방지
        String restaurantCategoryName = (restaurant != null && restaurant.getRestaurantCategory() != null)
                ? restaurant.getRestaurantCategory().getName()
                : null;

        String restaurantName = restaurant != null ? restaurant.getName() : null; // 추가
        String restaurantAddress = restaurant != null ? restaurant.getAddress() : null; // 추가

        // 작성일 null 방지 + 포맷
        String createdAtStr = board.getCreatedAt() != null // Null 체크
                ? board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
                : null;

        return BoardDetailResponseDto.builder()
                .restaurantName(restaurantName) // 수정
                .restaurantAddress(restaurantAddress) // 수정
                .restaurantCategoryName(restaurantCategoryName) // 수정
                .memberNickname(member.getNickname())
                .memberProfileImage(member.getProfileImage())
                .content(board.getContent())
                .tagNames(tagNames)
                .imageUrls(imageUrls)
                .createdAt(createdAtStr) // 수정
                .build();
    }

    @Override
    @Transactional
    public BoardCreateResponseDto createBoard(BoardRequestDto dto, List<MultipartFile> images, Member member) {
        // 1. 예약 확인
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));

        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("예약한 사용자만 게시글을 작성할 수 있습니다.");
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
                String originalFileName = image.getOriginalFilename();
                String storeFileName = UUID.randomUUID() + "_" + originalFileName;
                Path filePath = Paths.get(uploadDir, storeFileName);
                try {
                    Files.write(filePath, image.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패", e);
                }
                BoardImage boardImage = new BoardImage(originalFileName, storeFileName);
                board.addImage(boardImage); // 연관관계 설정
                boardImageRepository.save(boardImage);
            }
        }

        // 4. 태그 저장
        List<String> tagNames = dto.getTagNames();
        if (tagNames == null || tagNames.isEmpty()) {
            throw new IllegalArgumentException("태그는 최소 1개 이상 입력해야 합니다.");
        }

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName))); // 태그가 없으면 생성
            BoardTag boardTag = new BoardTag(board, tag);
            board.addTag(boardTag);
            boardTagRepository.save(boardTag);
        }

        return BoardCreateResponseDto.builder()
                .boardId(board.getId())
                .content(board.getContent())
                .imageUrls(board.getBoardImages().stream()
                        .map(image -> image.getImageUrl() != null ? image.getImageUrl() : image.getStoreFileName())
                        .toList())
                .tags(board.getBoardTags().stream()
                        .map(bt -> bt.getTag().getName())
                        .toList())
                .writerNickname(member.getNickname())
                .writerProfileImageUrl(member.getProfileImage())
                .createdAt(board.getCreatedAt())
                .build();
    }

    @Override
    public void updateBoard(Long boardId, BoardRequestDto dto, Member member) {
        throw new UnsupportedOperationException("updateBoard() 아직 구현되지 않았습니다.");
    }

    @Override
    public void deleteBoard(Long boardId, Member member) {
        throw new UnsupportedOperationException("deleteBoard() 아직 구현되지 않았습니다.");
    }

    @Override
    public PagedBoardsResponseDto searchAllByCategory(@Valid BoardCategorySearchRequestDto boardSearchRequestDto) {
        Pageable pageable = PageRequest.of(boardSearchRequestDto.getPage() - 1, 6,
                Sort.by("createdAt").ascending()); //페이지는 0부터 시작 - 이게 맞나. 이게 맞는지?
        Page<Board> boardList = boardRepository.findAllByCategory(boardSearchRequestDto.getCategoryId(), pageable);

        return new PagedBoardsResponseDto(boardList);
    }
}
