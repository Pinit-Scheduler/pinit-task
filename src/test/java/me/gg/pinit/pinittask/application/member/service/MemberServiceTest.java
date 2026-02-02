package me.gg.pinit.pinittask.application.member.service;

import me.gg.pinit.pinittask.domain.member.exception.DuplicatedScheduleRunningException;
import me.gg.pinit.pinittask.domain.member.exception.MemberNotFoundException;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.model.MemberUtils;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    Clock clock;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("회원의 ZoneId 조회 성공")
    void findZoneIdOfMember_success() {
        // given
        Member member = MemberUtils.getMemberSample();
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        ZoneId zoneId = memberService.findZoneIdOfMember(memberId);

        // then
        assertEquals(ZoneId.of("Asia/Seoul"), zoneId);
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("회원 ZoneId 조회 실패 - 회원 없음")
    void findZoneIdOfMember_memberNotFound() {
        // given
        Long memberId = 99L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class, () -> memberService.findZoneIdOfMember(memberId));
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("실행 중 일정 설정 성공")
    void setNowRunningSchedule_success() {
        // given
        Member member = MemberUtils.getMemberSample();
        Long memberId = 2L;
        Long scheduleId = 10L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        memberService.setNowRunningSchedule(memberId, scheduleId);

        // then
        assertEquals(scheduleId, member.getNowRunningScheduleId());
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("실행 중 일정 설정 실패 - 회원 없음")
    void setNowRunningSchedule_memberNotFound() {
        // given
        Long memberId = 3L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class, () -> memberService.setNowRunningSchedule(memberId, 11L));
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("실행 중 일정 설정 실패 - 이미 실행 중 일정 존재")
    void setNowRunningSchedule_duplicated() {
        // given
        Member member = MemberUtils.getMemberSample();
        member.setNowRunningSchedule(20L); // 이미 설정됨
        Long memberId = 4L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when & then
        DuplicatedScheduleRunningException ex = assertThrows(DuplicatedScheduleRunningException.class,
                () -> memberService.setNowRunningSchedule(memberId, 30L));
        assertEquals("이미 실행 중인 일정이 존재합니다.", ex.getMessage());
        assertEquals(20L, member.getNowRunningScheduleId()); // 기존 값 유지
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("실행 중 일정 초기화 성공")
    void clearNowRunningSchedule_success() {
        // given
        Member member = MemberUtils.getMemberSample();
        member.setNowRunningSchedule(100L);
        Long memberId = 5L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        memberService.clearNowRunningSchedule(memberId);

        // then
        assertNull(member.getNowRunningScheduleId());
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("실행 중 일정 초기화 실패 - 회원 없음")
    void clearNowRunningSchedule_memberNotFound() {
        // given
        Long memberId = 6L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class, () -> memberService.clearNowRunningSchedule(memberId));
        verify(memberRepository).findById(memberId);
    }

}
