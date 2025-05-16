package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    public PagedBoardsResponseDto getBoards(int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size, Sort.by("createdAt").descending());
        Page<Board> boardPage = boardRepository.findAll(pageable);
        return new PagedBoardsResponseDto(boardPage);
    }

    @Override
    @Transactional
    public Long createBoard(BoardRequestDto dto, Member member) {
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("식당이 존재하지 않습니다."));

        Board board = Board.builder()
                .restaurant(restaurant)
                .member(member)
                .content(dto.getContent())
                .build();

        // 이미지 처리
        if (dto.getImages() != null) {
            for (MultipartFile file : dto.getImages()) {
                String storeFileName = convertToFile(file);
                String originalFileName = file.getOriginalFilename();

                BoardImage boardImage = new BoardImage(originalFileName, storeFileName);
                board.addImage(boardImage);
            }
        }

        // 태그 처리
        if (dto.getTagNames() != null) {
            for (String tagName : dto.getTagNames()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));

                BoardTag boardTag = new BoardTag(tag);
                board.addTag(boardTag);
            }
        }

        return boardRepository.save(board).getId();
    }

    @Override
    public BoardDetailResponseDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        List<String> imageUrls = board.getBoardImages().stream()
                .map(BoardImage::getStoreFileName)
                .collect(Collectors.toList());

        List<String> tagNames = board.getBoardTags().stream()
                .map(boardTag -> boardTag.getTag().getName())
                .collect(Collectors.toList());

        return BoardDetailResponseDto.builder()
                .restaurantName(board.getRestaurant().getName())
                .createdAt(board.getCreatedAt())
                .imageUrls(imageUrls)
                .tagNames(tagNames)
                .content(board.getContent())
                .memberNickname(board.getMember().getNickname())
                .build();
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
    public void deleteBoard(Long boardId, Member member) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.NOT_FOUND));

        if (!board.getMember().getId().equals(member.getId())) {
            throw new BoardException(BoardErrorCode.NO_PERMISSION);
        }

        boardRepository.delete(board);
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
    public PagedBoardsResponseDto searchAllByCategory(@Valid BoardCategorySearchRequestDto boardSearchRequestDto) {
        Pageable pageable = PageRequest.of(boardSearchRequestDto.getPage() - 1, 6,
                Sort.by("createdAt").ascending()); //페이지는 0부터 시작
        Page<Board> boardList = boardRepository.findAllByCategory(boardSearchRequestDto.getCategoryId(), pageable);

        return new PagedBoardsResponseDto(boardList);
    }
}
