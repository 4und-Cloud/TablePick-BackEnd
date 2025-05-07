package com.goorm.tablepick.domain.member.service;


import com.goorm.tablepick.domain.member.dto.MemberResponseDto;

public interface MemberService {
    MemberResponseDto getMemberInfo(String username);
    
}
