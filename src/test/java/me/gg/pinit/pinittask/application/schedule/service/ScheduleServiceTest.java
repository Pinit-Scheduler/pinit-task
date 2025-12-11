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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
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
        scheduleSample = new Schedule(memberId, "title", "description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
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
        when(memberService.findZoneIdOfMember(memberId)).thenReturn(ZoneId.of("Asia/Seoul"));
        when(scheduleRepository.findAllByOwnerIdAndDesignatedStartTimeBetween(eq(memberId), any(), any(), anyString())).thenReturn(List.of(scheduleSample));
        List<Schedule> scheduleList = scheduleService.getScheduleList(memberId, LocalDate.of(2025, 10, 1));
        assertNotNull(scheduleList);
        assertEquals(1, scheduleList.size());
        verify(memberService).findZoneIdOfMember(memberId);
        verify(scheduleRepository).findAllByOwnerIdAndDesignatedStartTimeBetween(eq(memberId), any(), any(), anyString());
    }

    @Test
    void addSchedule() {
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Schedule newSchedule = new Schedule(memberId, "new title", "new description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
        Schedule savedSchedule = scheduleService.addSchedule(newSchedule);
        assertNotNull(savedSchedule);
        assertEquals("new title", savedSchedule.getTitle());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void updateSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleSample));
        SchedulePatch schedulePatch = new SchedulePatch().setTitle("new title");
        Schedule updatedSchedule = scheduleService.updateSchedule(memberId, scheduleId, schedulePatch);
        assertNotNull(updatedSchedule);
        assertEquals("new title", updatedSchedule.getTitle());
        Assertions.assertThat(updatedSchedule.getDescription()).isEqualTo("description");
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void deleteSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleSample)).thenReturn(Optional.empty());
        doNothing().when(scheduleRepository).delete(any(Schedule.class));
        scheduleService.deleteSchedule(memberId, scheduleId);
        Assertions.assertThatThrownBy(() -> scheduleService.getSchedule(memberId, scheduleId)).isInstanceOf(ScheduleNotFoundException.class).hasMessage("해당 일정을 찾을 수 없습니다.");
        verify(scheduleRepository).delete(scheduleSample);
        verify(domainEventPublisher, atLeast(0)).publish(any());
    }
}
