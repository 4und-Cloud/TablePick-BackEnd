package com.goorm.tablepick.domain.board.entity;

import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardImage> boardImages = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardTag> boardTags = new ArrayList<>();

    @Builder
    public Board(Restaurant restaurant, Member member, String content) {
        this.restaurant = restaurant;
        this.member = member;
        this.content = content;
    }

    public void updateFromDto(BoardRequestDto dto) {
        //this.title = dto.getTitle();
        this.content = dto.getContent();
        //this.category = dto.getCategory();   // 제목, 카테고리는 나중에 사용할 수도 있어서. 일단 남겨두고. 주석 처리
        // 필요시 이미지, 태그 등도 포함
    }

    public void addImage(BoardImage image) {
        boardImages.add(image);
        image.setBoard(this);
    }

    public void addTag(BoardTag tag) {
        boardTags.add(tag);
        tag.setBoard(this);
    }
}
