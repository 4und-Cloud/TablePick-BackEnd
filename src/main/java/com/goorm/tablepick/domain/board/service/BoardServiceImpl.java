package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final RestaurantRepository restaurantRepository;
    private final TagRepository tagRepository;
    private final BoardTagRepository boardTagRepository;

    @Override
    public List<BoardListResponseDto> getBoardsForMainPage() {
        Pageable pageable = PageRequest.of(0, 4, Sort.by("createdAt").descending());
        Page<Board> boardPage = boardRepository.findAll(pageable);
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
        // 클라이언트는 1페이지부터 요청하므로 0-based page index로 변환
        Page<Board> boardPage = boardRepository.findAll(pageable);

        return new PagedBoardListResponseDto(boardPage); // 변경된 리턴
    }

    @Override
    public BoardDetailResponseDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        Restaurant restaurant = board.getRestaurant();
        Member member = board.getMember();

        List<String> imageUrls = board.getBoardImages().stream()
                .limit(3) // 최대 3장으로 제한
                .map(BoardImage::getStoreFileName)
                .collect(Collectors.toList());

        List<String> tagNames = board.getBoardTags().stream()
                .map(boardTag -> boardTag.getTag().getName())
                .collect(Collectors.toList());

        // NullPointer 방지
        String restaurantCategoryName = restaurant.getRestaurantCategory() != null
                ? restaurant.getRestaurantCategory().getName()
                : null;

        String createdAtStr = board.getCreatedAt() != null // ✅ Null 체크
                ? board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
                : null;

        return BoardDetailResponseDto.builder()
                .restaurantName(restaurant.getName())
                .restaurantAddress(restaurant.getAddress())
                .restaurantCategoryName(restaurantCategoryName)  // 수정됨
                //.restaurantCategoryName(restaurant.getRestaurantCategory().getName())

                .memberNickname(member.getNickname())
                .memberProfileImage(member.getProfileImage())

                .content(board.getContent())

                .tagNames(tagNames)
                .imageUrls(imageUrls)

                .createdAt(createdAtStr) // ✅ 수정
                //.createdAt(board.getCreatedAt().format(
                 //       java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))) // 포맷팅

                .build();
    }

    @Override
    @Transactional
    public Long createBoard(BoardRequestDto dto, Member member) {
        log.info("🙋‍♂️ member: {}", member); // 이 위치에서 로그를 찍으세요
        if (member == null) {
            throw new BoardException(BoardErrorCode.NO_PERMISSION); // 또는 적절한 인증 관련 에러코드 추가
            //throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }
        log.info("✅ [createBoard] 게시글 생성 요청 시작: {}", dto);

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("식당이 존재하지 않습니다."));

        Board board = Board.builder()
                .restaurant(restaurant)
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
        return savedBoard.getId();
    }



    // 예시: 파일 저장 로직 (단순화)
    private String convertToFile(MultipartFile file) {
        // 저장 로직 구현 필요 (예: S3, 로컬 등)
        return java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
    }

    public List<BoardListResponseDto> getBoardList() {
        List<Board> boards = boardRepository.findAllByOrderByCreatedAtDesc();

        return boards.stream().map(board -> {
            String imageUrl = board.getBoardImages().isEmpty()
                    ? null
                    : "/images/" + board.getBoardImages().get(0).getStoreFileName();

            List<String> tagNames = board.getBoardTags().stream()
                    .map(boardTag -> boardTag.getTag().getName())
                    .collect(Collectors.toList());

            return BoardListResponseDto.builder()
                    .id(board.getId())
                    .content(board.getContent())
                    .restaurantName(board.getRestaurant().getName())
                    .restaurantAddress(board.getRestaurant().getAddress())
                    .imageUrl(imageUrl)
                    .tagNames(tagNames)
                    .build();
        }).collect(Collectors.toList());
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
