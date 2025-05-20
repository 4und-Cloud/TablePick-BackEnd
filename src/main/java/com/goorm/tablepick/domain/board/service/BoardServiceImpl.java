package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
import com.goorm.tablepick.domain.board.dto.request.BoardCreateResponseDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardsResponseDto;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardImage;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.board.exception.BoardErrorCode;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import com.goorm.tablepick.global.exception.BoardException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final RestaurantRepository restaurantRepository;
    private final TagRepository tagRepository;
    private final BoardTagRepository boardTagRepository;
    private final ReservationRepository reservationRepository;

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
    public BoardCreateResponseDto createBoard(BoardRequestDto dto, Member member) {
        log.info("🙋‍♂️ member: {}", member); // 이 위치에서 로그를 찍으세요
        if (member == null) {
            throw new BoardException(BoardErrorCode.NO_PERMISSION); // 또는 적절한 인증 관련 에러코드 추가
            //throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }
        log.info("✅ [createBoard] 게시글 생성 요청 시작: {}", dto);

        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약 정보가 존재하지 않습니다."));

        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new BoardException(BoardErrorCode.NO_PERMISSION); // ✅ 예약자 본인만 작성 가능
        }

        Board board = Board.builder()
                .reservation(reservation)
                .member(member)
                .content(dto.getContent())
                .build();

        log.info("✅ [createBoard] Board 객체 생성 완료");

        // 이미지 처리
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            log.info("✅ [createBoard] 이미지 개수: {}", dto.getImages().size());
            for (MultipartFile file : dto.getImages()) {
                if (!file.isEmpty()) {
                    String storeFileName = convertToFile(file);
                    String originalFileName = file.getOriginalFilename();
                    BoardImage boardImage = new BoardImage(originalFileName, storeFileName);
                    board.addImage(boardImage);
                }
            }
        }

        // 태그 처리
        if (dto.getTagNames() != null) {
            log.info("✅ [createBoard] 태그 개수: {}", dto.getTagNames().size());
            for (String tagName : dto.getTagNames()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                board.addTag(new BoardTag(tag));
            }
        }

        Board savedBoard = boardRepository.save(board);

        log.info("✅ [createBoard] 게시글 저장 완료. 생성된 ID: {}", savedBoard.getId());

        return BoardCreateResponseDto.builder()
                .boardId(savedBoard.getId())
                .content(savedBoard.getContent())
                .imageUrls(savedBoard.getBoardImages().stream()
                        .map(BoardImage::getImageUrl)
                        .toList())
                .tags(savedBoard.getBoardTags().stream()
                        .map(bt -> bt.getTag().getName())
                        .toList())
                .writerNickname(member.getNickname())
                .writerProfileImageUrl(member.getProfileImage())
                .createdAt(savedBoard.getCreatedAt())
                .build();
    }



    // 예시: 파일 저장 로직 (단순화)
    private String convertToFile(MultipartFile file) {
        // 저장 로직 구현 필요 (예: S3, 로컬 등)
        return java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
    }




    @Override
    @Transactional
    public void updateBoard(Long boardId, BoardRequestDto dto, Member member) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.NOT_FOUND));

        if (!board.getMember().getId().equals(member.getId())) {
            throw new BoardException(BoardErrorCode.NO_PERMISSION);
        }

        board.updateFromDto(dto); // → Board 엔티티에 updateFromDto() 메서드가 있어야 함
        boardRepository.save(board);
    }

    @Override
    public void deleteBoard(Long boardId, Member member) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.NOT_FOUND));

        if (!board.getMember().getId().equals(member.getId())) {
            throw new BoardException(BoardErrorCode.NO_PERMISSION);
        }

        boardRepository.delete(board);
    }

    @Override
    public PagedBoardsResponseDto searchAllByCategory(@Valid BoardCategorySearchRequestDto boardSearchRequestDto) {
        Pageable pageable = PageRequest.of(boardSearchRequestDto.getPage() - 1, 6,
                Sort.by("createdAt").ascending()); //페이지는 0부터 시작 - 이게 맞나. 이게 맞는지?
        Page<Board> boardList = boardRepository.findAllByCategory(boardSearchRequestDto.getCategoryId(), pageable);

        return new PagedBoardsResponseDto(boardList);
    }
}
