package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;

public interface OAuthInfo {
    String getEmail();

    String getProviderId();

    String getProvider();

    Member toEntity();
}