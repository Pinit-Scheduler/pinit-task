package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class ScheduleServiceTest {


    @Autowired
    ScheduleService scheduleService;

    @Autowired
    ScheduleRepository scheduleRepository;

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
    void getSchedule() {
        Schedule schedule = scheduleService.getSchedule(member.getId(), scheduleSample.getId());

        assertNotNull(schedule);
    }

    @Test
    void getScheduleList() {
        List<Schedule> scheduleList = scheduleService.getScheduleList(scheduleSample.getOwnerId(), LocalDate.of(2025, 10, 1));

        assertNotNull(scheduleList);
        assertEquals(1, scheduleList.size());
    }

    @Test
    void addSchedule() {
        Schedule newSchedule = new Schedule(member.getId(), "new title", "new description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
        Schedule savedSchedule = scheduleService.addSchedule(newSchedule);

        assertNotNull(savedSchedule);
        assertEquals("new title", savedSchedule.getTitle());
    }

    @Test
    void updateSchedule() {
        SchedulePatch schedulePatch = new SchedulePatch().setTitle("new title");
        Schedule updatedSchedule = scheduleService.updateSchedule(member.getId(), scheduleSample.getId(), schedulePatch);

        assertNotNull(updatedSchedule);
        assertEquals("new title", updatedSchedule.getTitle());
        Assertions.assertThat(updatedSchedule.getDescription()).isEqualTo(scheduleSample.getDescription());
    }

    @Test
    void deleteSchedule() {
        scheduleService.deleteSchedule(member.getId(), scheduleSample.getId());

        Assertions.assertThatThrownBy(() -> scheduleService.getSchedule(member.getId(), scheduleSample.getId()))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessage("해당 일정을 찾을 수 없습니다.");
    }


}