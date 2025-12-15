package me.gg.pinit.pinittask.application.member.service;

import me.gg.pinit.pinittask.domain.member.exception.MemberNotFoundException;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Optional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public ZoneId findZoneIdOfMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found")).getZoneId();
    }

    public Long getNowInProgressScheduleId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"))
                .getNowRunningScheduleId();
    }

    @Transactional
    public void setNowRunningSchedule(Long memberId, Long scheduleId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"))
                .setNowRunningSchedule(scheduleId);
    }

    @Transactional
    public void clearNowRunningSchedule(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"))
                .clearNowRunningSchedule();
    }

    @Transactional
    public void enrollMember(Long memberId, String nickname) {
        Optional<Member> byId = memberRepository.findById(memberId);
        if (byId.isPresent()) {
            return;
        }
        memberRepository.save(new Member(memberId, nickname, ZoneId.of("Asia/Seoul")));
    }
}
