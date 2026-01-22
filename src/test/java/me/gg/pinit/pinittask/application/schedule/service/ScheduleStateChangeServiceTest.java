package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.events.DomainEventPublisher;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.ENROLLED_TIME;
import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getNotStartedSchedule;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleStateChangeServiceTest {
    @Mock
    ScheduleRepository scheduleRepository;
    @Mock
    MemberService memberService;
    @Mock
    DomainEventPublisher domainEventPublisher;
    @Mock
    DependencyService dependencyService;
    @Mock
    TaskService taskService;

    @InjectMocks
    ScheduleStateChangeService scheduleStateChangeService;

    Long memberId;
    Long scheduleId;
    Schedule scheduleSample;
    ZonedDateTime now;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        scheduleId = 10L;
        scheduleSample = getNotStartedSchedule(scheduleId); // ownerId=1L
        now = ENROLLED_TIME.plusHours(1);
        DomainEvents.getEventsAndClear();
    }

    private void stubFind() {
        when(scheduleRepository.findByIdForUpdate(scheduleId)).thenReturn(Optional.of(scheduleSample));
    }

    private void stubNoDeps() {
        when(dependencyService.getPreviousTaskIds(memberId, scheduleSample.getTaskId())).thenReturn(Collections.emptyList());
        when(taskService.findTasksByIds(memberId, Collections.emptyList())).thenReturn(Collections.emptyList());
    }

    @Test
    void startSchedule_inProgress() {
        stubFind();
        stubNoDeps();
        scheduleStateChangeService.startSchedule(memberId, scheduleId, now);
        Assertions.assertThat(scheduleSample.isInProgress()).isTrue();
        verify(memberService).setNowRunningSchedule(memberId, scheduleId);
        verify(dependencyService).getPreviousTaskIds(memberId, scheduleSample.getTaskId());
        verify(taskService).findTasksByIds(memberId, Collections.emptyList());
    }

    @Test
    void completeSchedule_completed() {
        stubFind();
        scheduleStateChangeService.completeSchedule(memberId, scheduleId, now);
        Assertions.assertThat(scheduleSample.isCompleted()).isTrue();
        verify(taskService).markCompleted(memberId, scheduleSample.getTaskId());
    }

    @Test
    void suspendSchedule_suspended() {
        stubFind();
        stubNoDeps();
        scheduleStateChangeService.startSchedule(memberId, scheduleId, now);
        scheduleStateChangeService.suspendSchedule(memberId, scheduleId, now.plusHours(1));
        Assertions.assertThat(scheduleSample.isSuspended()).isTrue();
    }

    @Test
    void cancelSchedule_backToNotStarted() {
        stubFind();
        stubNoDeps();
        scheduleStateChangeService.startSchedule(memberId, scheduleId, now);
        scheduleStateChangeService.cancelSchedule(memberId, scheduleId);
        Assertions.assertThat(scheduleSample.isNotStarted()).isTrue();
        verify(memberService).clearNowRunningSchedule(memberId);
        verify(taskService).markIncomplete(memberId, scheduleSample.getTaskId());
    }

    @Test
    void cancelSchedule_withoutTask_doesNotTouchTask() {
        scheduleSample = getNotStartedSchedule(scheduleId, memberId, null, "t", "d", ENROLLED_TIME);
        stubFind();
        scheduleStateChangeService.startSchedule(memberId, scheduleId, now);
        scheduleStateChangeService.cancelSchedule(memberId, scheduleId);
        verify(taskService, never()).markIncomplete(anyLong(), anyLong());
    }

    @Test
    void startSchedule_previousNotCompleted_throws() {
        stubFind();
        Long prevId = 99L;
        when(dependencyService.getPreviousTaskIds(memberId, scheduleSample.getTaskId())).thenReturn(List.of(prevId));
        Task prevTask = TaskUtils.newTask(memberId, prevId);
        when(taskService.findTasksByIds(memberId, List.of(prevId))).thenReturn(List.of(prevTask));
        Assertions.assertThatThrownBy(() -> scheduleStateChangeService.startSchedule(memberId, scheduleId, now))
                .isInstanceOf(IllegalStateException.class);
        verify(memberService, never()).setNowRunningSchedule(anyLong(), anyLong());
    }

    @Test
    void startSchedule_ownerMismatch_throws() {
        stubFind();
        Assertions.assertThatThrownBy(() -> scheduleStateChangeService.startSchedule(2L, scheduleId, now))
                .isInstanceOf(IllegalArgumentException.class);
        verify(memberService, never()).setNowRunningSchedule(anyLong(), anyLong());
    }
}
