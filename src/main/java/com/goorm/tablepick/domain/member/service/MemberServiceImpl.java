package com.goorm.tablepick.domain.member.service;

import com.goorm.tablepick.domain.board.dto.response.MyBoardListResponseDto;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.reservation.dto.response.ReservationResponseDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import java.util.List;
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

    @Override
    public List<ReservationResponseDto> getMemberReservationList(String username) {
        List<Reservation> reservationList = reservationRepository.findAllByMember_Email(username);

        List<ReservationResponseDto> list = reservationList.stream()
                .map(ReservationResponseDto::new)
                .toList();
        return list;
    }

    @Override
    public List<MyBoardListResponseDto> getMemberBoardList(String username) {
        List<Board> boardList = boardRepository.findAllByMemberEmail(username);

        List<MyBoardListResponseDto> list = boardList.stream()
                .map(MyBoardListResponseDto::new)
                .toList();
        return list;
    }
}