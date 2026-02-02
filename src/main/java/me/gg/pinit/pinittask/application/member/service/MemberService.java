package me.gg.pinit.pinittask.application.member.service;

import me.gg.pinit.pinittask.domain.member.exception.MemberNotFoundException;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final Clock clock;

    public MemberService(MemberRepository memberRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ZoneId findZoneIdOfMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found")).getZoneId();
    }

    @Transactional(readOnly = true)
    public ZoneOffset findZoneOffsetAt(Long memberId, Instant instant) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"))
                .getZoneId()
                .getRules()
                .getOffset(instant);
    }

    @Transactional(readOnly = true)
    public ZoneOffset findZoneOffsetOfMember(Long memberId) {
        return findZoneOffsetAt(memberId, Instant.now(clock));
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
    public void enrollMember(Long memberId, String nickname, ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Optional<Member> byId = memberRepository.findById(memberId);
        if (byId.isPresent()) {
            return;
        }
        memberRepository.save(new Member(memberId, nickname, zoneId));
    }

}
