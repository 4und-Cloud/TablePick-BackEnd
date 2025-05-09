package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import com.goorm.tablepick.domain.member.enums.Gender;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KakaoInfo implements OAuthInfo {
    private String nickname;
    private String email;
    private String gender;
    private String phoneNumber;
    private LocalDate birthday;
    private String profileImage;
    private String provider;
    private String providerId;

    @Builder
    public KakaoInfo(String nickname, String providerId, String profileImage,
                     LocalDate birthday, String phoneNumber, String gender, String email) {
        this.email = email;
        this.gender = gender;
        this.nickname = nickname;
        this.birthday = birthday;
        this.provider = "kakao";
        this.providerId = providerId;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }

    @Override
    public Member toEntity() {

        return Member.builder()
                .email(this.email)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .birthdate(this.birthday)
                .phoneNumber(this.phoneNumber)
                .gender(Gender.valueOf(this.gender.toUpperCase()))
                .roles(AccountRole.USER)
                .isMemberDeleted(false)
                .provider(this.provider)
                .providerId(this.providerId)
                .build();
    }
}
