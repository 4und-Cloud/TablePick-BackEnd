package com.goorm.tablepick.domain.member.controller;

import com.goorm.tablepick.domain.board.dto.MyBoardListResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.service.MemberService;
import com.goorm.tablepick.domain.reservation.dto.response.ReservationResponseDto;
import com.goorm.tablepick.global.jwt.JwtProvider;
import com.goorm.tablepick.global.jwt.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenService refreshTokenService;
    private final JwtProvider jwtProvider;

    @GetMapping
    @Operation(summary = "로그인한 사용자 정보 조회", description = "회원가입 후 추가적인 사용자 정보를 받습니다.")
    public ResponseEntity<MemberResponseDto> getMemberAfterRegistration(
            @AuthenticationPrincipal UserDetails userDetails) {
        MemberResponseDto dto = memberService.getMemberInfo("test@example.com");
        return ResponseEntity.ok(dto);
    }

    @PatchMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "사용자 정보 수정", description = "닉네임, 전화번호, 성별, 프로필 사진, 프로필 이미지, 사용자 태그 수정 가능합니다.")
//    @PreAuthorize() //시큐리티 설정하신것들을 어노테이션으로 효율적으로
//    @PostAuthorize()
    public ResponseEntity<Void> updateMember(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody @Valid MemberUpdateRequestDto memberUpdateRequestDto) {
        memberService.updateMemberInfo(userDetails.getUsername(), memberUpdateRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reservations")
    @Operation(summary = "사용자 예약 리스트 조회", description = "사용자 ID를 기준으로 예약 리스트를 반환합니다.")
    public ResponseEntity<List<ReservationResponseDto>> getMemberReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReservationResponseDto> reservationList = memberService.getMemberReservationList(
                userDetails.getUsername());
        return ResponseEntity.ok(reservationList);
    }

    @GetMapping("/boards")
    @Operation(summary = "사용자 게시글 리스트 조회", description = "사용자 ID를 기준으로 게시글 리스트를 반환합니다.")
    public ResponseEntity<List<MyBoardListResponseDto>> getMemberBoards(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<MyBoardListResponseDto> boardList = memberService.getMemberBoardList(userDetails.getUsername());
        return ResponseEntity.ok(boardList);
    }
    
}
