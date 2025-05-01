package com.goorm.tablepick.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 255, nullable = false)
    private String token;

    @Column(length = 255)
    private String deviceInfo;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime deletedAt;

    @Builder
    public RefreshToken(Member member, String token, String deviceInfo, LocalDateTime createdAt,
                        LocalDateTime expiredAt, LocalDateTime deletedAt) {
        this.member = member;
        this.token = token;
        this.deviceInfo = deviceInfo;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.deletedAt = deletedAt;
    }
}
