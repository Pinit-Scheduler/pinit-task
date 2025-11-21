package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.*;

@SpringBootTest
@Transactional
public class ScheduleStateChangeServiceTest {
    @Autowired
    ScheduleStateChangeService scheduleStateChangeService;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    MemberRepository memberRepository;

    Member member;

    Schedule scheduleSample;

    @BeforeEach
    void setUp() {
        member = new Member("haha", "hoho", Duration.ofDays(2), ZoneId.of("Asia/Seoul"));
        memberRepository.save(member);
        scheduleSample = new Schedule(member.getId(), "title", "description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
        scheduleRepository.save(scheduleSample);
    }

    @Test
    void startSchedule() {
        scheduleStateChangeService.startSchedule(member.getId(), scheduleSample.getId(), ENROLLED_TIME.plusHours(1));
        Schedule startedSchedule = scheduleService.getSchedule(member.getId(), scheduleSample.getId());
        Assertions.assertThat(startedSchedule.isInProgress()).isTrue();
    }

    @Test
    void completeSchedule() {
        scheduleStateChangeService.completeSchedule(member.getId(), scheduleSample.getId(), ENROLLED_TIME.plusHours(1));
        Schedule completedSchedule = scheduleService.getSchedule(member.getId(), scheduleSample.getId());
        Assertions.assertThat(completedSchedule.isCompleted()).isTrue();
    }

    @Test
    void suspendSchedule() {
        //given
        scheduleStateChangeService.startSchedule(member.getId(), scheduleSample.getId(), ENROLLED_TIME.plusHours(1));

        //when
        scheduleStateChangeService.suspendSchedule(member.getId(), scheduleSample.getId(), ENROLLED_TIME.plusHours(2));
        Schedule suspendedSchedule = scheduleService.getSchedule(member.getId(), scheduleSample.getId());

        //then
        Assertions.assertThat(suspendedSchedule.isSuspended()).isTrue();
    }

    @Test
    void cancelSchedule() {
        //given
        scheduleStateChangeService.startSchedule(member.getId(), scheduleSample.getId(), ENROLLED_TIME.plusHours(1));

        //when
        scheduleStateChangeService.cancelSchedule(member.getId(), scheduleSample.getId());
        Schedule canceledSchedule = scheduleService.getSchedule(member.getId(), scheduleSample.getId());

        //then
        Assertions.assertThat(canceledSchedule.isNotStarted()).isTrue();
    }
}
