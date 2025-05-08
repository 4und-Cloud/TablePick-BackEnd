package com.goorm.tablepick.domain.member.service;


import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import jakarta.validation.Valid;

public interface MemberService {
    MemberResponseDto getMemberInfo(String username);

    void updateMemberInfo(String username, @Valid MemberUpdateRequestDto memberUpdateRequestDto);
}
