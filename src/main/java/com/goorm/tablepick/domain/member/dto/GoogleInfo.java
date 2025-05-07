package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleInfo implements OAuthInfo {
    private final Map<String, Object> attributes;

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public Member toEntity() {
        return Member.builder()
                .email(getEmail())
                .nickname((String) attributes.get("name"))
                .profileImage((String) attributes.get("picture"))
                .roles(AccountRole.USER)
                .isMemberDeleted(false)
                .provider(getProvider())
                .providerId(getProviderId())
                .build();
    }

}
