package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.application.events.DomainEventPublisher;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.ENROLLED_TIME;
import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getNotStartedSchedule;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
    @Mock
    ScheduleRepository scheduleRepository;
    @Mock
    MemberService memberService;
    @Mock
    DomainEventPublisher domainEventPublisher;
    @InjectMocks
    ScheduleService scheduleService;
    Long memberId;
    Long scheduleId;
    Schedule scheduleSample;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        scheduleId = 100L;
        scheduleSample = getNotStartedSchedule(scheduleId, memberId, 1L, "title", "description", ENROLLED_TIME);
    }

    @Test
    void getSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleSample));
        Schedule schedule = scheduleService.getSchedule(memberId, scheduleId);
        assertNotNull(schedule);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void getScheduleList() {
        //given
        when(memberService.findZoneIdOfMember(memberId)).thenReturn(ZoneId.of("Asia/Seoul"));
        when(scheduleRepository.findAllByOwnerIdAndDesignatedStartTimeInstantBetween(eq(memberId), any(), any())).thenReturn(List.of(scheduleSample));

        //when
        List<Schedule> scheduleList = scheduleService.getScheduleList(memberId, ZonedDateTime.of(2025, 10, 1, 0, 0, 0, 0, ZoneId.of("Asia/Seoul")));

        //then
        assertNotNull(scheduleList);
        assertEquals(1, scheduleList.size());
        verify(memberService).findZoneIdOfMember(memberId);
        verify(scheduleRepository).findAllByOwnerIdAndDesignatedStartTimeInstantBetween(eq(memberId), any(), any());
    }

    @Test
    void addSchedule() {
        //given
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Schedule newSchedule = new Schedule(memberId, 1L, "new title", "new description", ENROLLED_TIME);

        //when
        Schedule savedSchedule = scheduleService.addSchedule(newSchedule);

        //then
        assertNotNull(savedSchedule);
        assertEquals("new title", savedSchedule.getTitle());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void updateSchedule() {
        //given
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleSample));
        SchedulePatch schedulePatch = new SchedulePatch().setTitle("new title");

        //when
        Schedule updatedSchedule = scheduleService.updateSchedule(memberId, scheduleId, schedulePatch);

        //then
        assertNotNull(updatedSchedule);
        assertEquals("new title", updatedSchedule.getTitle());
        Assertions.assertThat(updatedSchedule.getDescription()).isEqualTo("description");
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void deleteSchedule() {
        //given
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleSample)).thenReturn(Optional.empty());
        doNothing().when(scheduleRepository).delete(any(Schedule.class));

        //when
        scheduleService.deleteSchedule(memberId, scheduleId);

        //then
        Assertions.assertThatThrownBy(() -> scheduleService.getSchedule(memberId, scheduleId)).isInstanceOf(ScheduleNotFoundException.class).hasMessage("해당 일정을 찾을 수 없습니다.");
        verify(scheduleRepository).delete(scheduleSample);
        verify(domainEventPublisher, atLeast(0)).publish(any());
    }

    @Test
    void detachSchedulesByTaskId_setsTaskIdNull_andDoesNotDelete() {
        Long taskId = 777L;
        Schedule schedule = getNotStartedSchedule(201L, memberId, taskId, "title", "desc", ENROLLED_TIME);
        when(scheduleRepository.findAllByTaskId(taskId)).thenReturn(List.of(schedule));

        scheduleService.detachSchedulesByTaskId(memberId, taskId);

        assertNull(schedule.getTaskId());
        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }
}
