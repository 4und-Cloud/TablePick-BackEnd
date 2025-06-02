package com.goorm.tablepick.domain.board.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "board_keyword_category", uniqueConstraints = {
        @UniqueConstraint(columnNames = "keyword")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardKeywordCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String keyword;

    @OneToMany
    private List<BoardKeyword> boardKeywords;
}