package com.goorm.tablepick.domain.member.service;

import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final BoardRepository boardRepository;

    @Override
    public MemberResponseDto getMemberInfo(String username) {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return MemberResponseDto.toDto(member);
    }

    @Override
    @Transactional
    public void updateMemberInfo(String username, MemberUpdateRequestDto memberUpdateRequestDto) {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Member updatedMember = member.updateMember(memberUpdateRequestDto);
        memberRepository.save(updatedMember);
    }

}