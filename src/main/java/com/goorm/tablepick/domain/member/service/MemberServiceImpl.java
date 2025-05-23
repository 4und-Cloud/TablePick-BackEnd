package com.goorm.tablepick.domain.member.service;

import com.goorm.tablepick.domain.board.dto.response.MyBoardListResponseDto;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.member.dto.MemberAddtionalInfoRequestDto;
import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.entity.MemberTag;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.member.repository.MemberTagRepository;
import com.goorm.tablepick.domain.reservation.dto.response.ReservationResponseDto;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final BoardRepository boardRepository;
    private final MemberTagRepository memberTagRepository;
    private final TagRepository tagRepository;

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
        List<Reservation> reservationList = reservationRepository.findAllByMemberEmail(username);

        List<ReservationResponseDto> list = reservationList.stream()
                .map(ReservationResponseDto::new)
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public List<MyBoardListResponseDto> getMemberBoardList(String username) {
        List<Board> boardList = boardRepository.findAllByMemberEmail(username);

        List<MyBoardListResponseDto> list = boardList.stream()
                .map(MyBoardListResponseDto::new)
                .collect(Collectors.toList());

        return list;
    }

    @Override
    @Transactional
    public void addMemberInfo(String username, MemberAddtionalInfoRequestDto dto) {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<MemberTag> memberTagList = dto.getMemberTags().stream()
                .map(tagId -> {
                    Tag tag = tagRepository.findById(tagId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다: " + tagId));
                    return new MemberTag(member, tag);
                })
                .collect(Collectors.toList());

        member.addMemberInfo(dto, memberTagList);

        memberTagRepository.saveAll(memberTagList);
        // memberRepository.save(member); → 변경 감지로 생략 가능
    }

    @Override
    public Member getMember(String email) {
        return memberRepository.findByEmail(email).get();
    }
}