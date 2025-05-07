package com.goorm.tablepick.domain.member.controller;

import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    @Operation(summary = "로그인한 사용자 정보 조회", description = "회원가입 후 추가적인 사용자 정보를 받습니다.")
    public ResponseEntity<MemberResponseDto> getMemberAfterRegistration(
            @AuthenticationPrincipal UserDetails userDetails) {
        MemberResponseDto dto = memberService.getMemberInfo("test@example.com");
        return ResponseEntity.ok(dto);
    }

}