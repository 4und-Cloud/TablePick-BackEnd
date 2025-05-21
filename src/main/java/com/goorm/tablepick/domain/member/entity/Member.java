package com.goorm.tablepick.domain.member.entity;

import com.goorm.tablepick.domain.member.dto.MemberAddtionalInfoRequestDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import com.goorm.tablepick.domain.member.enums.Gender;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthdate;

    private String phoneNumber;

    private String profileImage;

    private Boolean isMemberDeleted;

    @Setter
    @OneToOne
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTag> memberTags;

    @Enumerated(EnumType.STRING)
    private AccountRole roles;

    private String provider;

    private String providerId;

    // FCM 토큰 필드 추가
    @Column(length = 255)
    private String fcmToken;

    public void updateRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
        refreshToken.setMember(this);
    }

    public Member updateMember(MemberUpdateRequestDto dto) {
        this.nickname = dto.getNickname();
        this.phoneNumber = dto.getPhoneNumber();
        this.gender = dto.getGender();
        this.birthdate = dto.getBirthdate();
        this.profileImage = dto.getProfileImage();
        this.memberTags = dto.getMemberTags();
        return this;
    }

    // FCM 토큰 업데이트 메서드
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // FCM 토큰 삭제 메서드
    public void removeFcmToken() {
        this.fcmToken = null;
    }

    public void addMemberInfo(MemberAddtionalInfoRequestDto dto, List<MemberTag> newMemberTags) {
        this.phoneNumber = dto.getPhoneNumber();
        this.gender = dto.getGender();
        this.birthdate = dto.getBirthdate();

        if (this.memberTags == null) {
            this.memberTags = new ArrayList<>();
        } else {
            this.memberTags.clear(); // 기존 값 제거 (orphanRemoval 작동)
        }

        for (MemberTag tag : newMemberTags) {
            tag.setMember(this); // 양방향 연관관계 설정
            this.memberTags.add(tag);
        }

    }
}
