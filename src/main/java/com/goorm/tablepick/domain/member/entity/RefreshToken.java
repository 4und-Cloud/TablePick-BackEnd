package com.goorm.tablepick.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 255, nullable = false)
    private String token;

    @Column(length = 255)
    private String deviceInfo;

    @CreationTimestamp
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
