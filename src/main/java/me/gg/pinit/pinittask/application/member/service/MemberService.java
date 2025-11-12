package me.gg.pinit.pinittask.application.member.service;

import me.gg.pinit.pinittask.domain.member.exception.MemberNotFoundException;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public ZoneId findMemberZoneById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found")).getZoneId();
    }
}
