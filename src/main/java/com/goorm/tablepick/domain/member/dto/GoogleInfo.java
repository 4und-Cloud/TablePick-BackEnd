package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleInfo implements OAuthInfo {
    private String nickname;
    private String email;
    private String profileImage;
    private String provider;
    private String providerId; //다른분들이 봣을때 잘모를수잇다 그러니 공통적인 필드값은 관리한느게 저ㅗㅎ다

    public GoogleInfo(String name, String picture, String email, String sub) {
        this.nickname = name;
        this.email = email;
        this.profileImage = picture;
        this.provider = "google";
        this.providerId = sub;

    }

    @Override
    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .roles(AccountRole.USER)
                .isMemberDeleted(false)
                .provider(this.provider)
                .providerId(this.providerId)
                .build();
    }

}
