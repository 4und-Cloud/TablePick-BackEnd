package com.goorm.tablepick.domain.member.controller;

import com.goorm.tablepick.domain.board.dto.response.MyBoardListResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberAddtionalInfoRequestDto;
import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.service.MemberService;
import com.goorm.tablepick.domain.reservation.dto.response.ReservationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @Value("${kakao.admin.key}")
    private String kakaoAdminKey;

    @GetMapping
    @Operation(summary = "로그인한 사용자 정보 조회", description = "회원가입 후 추가적인 사용자 정보를 받습니다.")
    public ResponseEntity<MemberResponseDto> getMemberAfterRegistration(
            @AuthenticationPrincipal UserDetails userDetails) {
        MemberResponseDto dto = memberService.getMemberInfo(userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping
    @Operation(summary = "사용자 정보 수정", description = "닉네임, 전화번호, 성별, 프로필 사진, 사용자 태그 수정 가능합니다.")
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

    @PostMapping("/logout")
    @Operation(summary = "사용자 로그아웃", description = "사용자 ID를 기준으로 로그아웃합니다. 쿠키에서 리프레쉬 토큰 삭제됩니다.")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //쿠키 무효화는 security config에서 설정
        Member member = memberService.getMember(getUserName());
        if (member.getProvider().equals("kakao")) {
            callKakaoUnlink(member.getProviderId());
        }

        return ResponseEntity.ok().body("Logged out successfully");
    }

    @PostMapping
    @Operation(summary = "로그인 후 사용자 정보 추가", description = "전화번호, 성별, 사용자 태그 수정 가능합니다.")
    public ResponseEntity<Void> addMemberInfoAfterLogin(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody @Valid MemberAddtionalInfoRequestDto memberAddtionalInfoRequestDto) {
        memberService.addMemberInfo(userDetails.getUsername(), memberAddtionalInfoRequestDto);
        return ResponseEntity.ok().build();
    }

    private String getUserName() {
        // SecurityContext에서 사용자 ID 가져오기
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void callKakaoUnlink(String kakaoUserId) throws Exception {
        URL url = new URL("https://kapi.kakao.com/v1/user/unlink");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "KakaoAK " + kakaoAdminKey);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String params = "target_id_type=user_id&target_id=" + kakaoUserId;
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Kakao unlink failed with response code: " + responseCode);
        }
        conn.disconnect();
    }
}
