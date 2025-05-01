package com.groom.tablepick.domain.tag.entity;

import com.groom.tablepick.domain.board.entity.BoardTag;
import com.groom.tablepick.domain.member.entity.MemberTag;
import com.groom.tablepick.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

}
