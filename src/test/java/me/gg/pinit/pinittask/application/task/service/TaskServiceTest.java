package me.gg.pinit.pinittask.application.task.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.events.DomainEventPublisher;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleStateChangeService;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.task.event.TaskCanceledEvent;
import me.gg.pinit.pinittask.domain.task.event.TaskCompletedEvent;
import me.gg.pinit.pinittask.domain.task.exception.TaskNotFoundException;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import me.gg.pinit.pinittask.interfaces.dto.TaskCursorPageResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    DependencyService dependencyService;
    @Mock
    ScheduleService scheduleService;
    @Mock
    ScheduleStateChangeService scheduleStateChangeService;
    @Mock
    DomainEventPublisher domainEventPublisher;
    @InjectMocks
    TaskService taskService;

    @AfterEach
    void clearDomainEvents() {
        DomainEvents.getEventsAndClear();
    }

    @Test
    void deleteTask_alsoDeletesSchedules_whenFlagTrue() {
        Long ownerId = 1L;
        Long taskId = 10L;
        Task task = buildTask(ownerId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(ownerId, taskId, true);

        verify(scheduleService).deleteSchedulesByTaskId(ownerId, taskId);
        verify(scheduleService, never()).detachSchedulesByTaskId(any(), any());
        verify(dependencyService).deleteWithTaskId(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_detachesSchedules_whenFlagFalse() {
        Long ownerId = 2L;
        Long taskId = 20L;
        Task task = buildTask(ownerId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(ownerId, taskId, false);

        verify(scheduleService).detachSchedulesByTaskId(ownerId, taskId);
        verify(scheduleService, never()).deleteSchedulesByTaskId(any(), any());
        verify(dependencyService).deleteWithTaskId(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_throwsWhenOwnerDoesNotMatch() {
        Long ownerId = 3L;
        Long taskId = 30L;
        Task task = buildTask(ownerId + 1);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Assertions.assertThatThrownBy(() -> taskService.deleteTask(ownerId, taskId, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member does not own the task");

        verifyNoInteractions(scheduleService, dependencyService, domainEventPublisher);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void getTask_throwsTaskNotFound_whenRepositoryIsEmpty() {
        Long ownerId = 11L;
        Long taskId = 111L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> taskService.getTask(ownerId, taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("해당 작업을 찾을 수 없습니다.");
    }

    @Test
    void markCompleted_setsFlagAndPublishesEvent() {
        Long ownerId = 5L;
        Long taskId = 50L;
        Task task = buildTask(ownerId);
        ReflectionTestUtils.setField(task, "id", taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(scheduleService.findByTaskId(taskId)).thenReturn(Optional.empty());

        taskService.markCompleted(ownerId, taskId);

        Assertions.assertThat(task.isCompleted()).isTrue();
        ArgumentCaptor<DomainEvent> domainEventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(domainEventCaptor.capture());
        Assertions.assertThat(domainEventCaptor.getValue()).isInstanceOf(TaskCompletedEvent.class);
        TaskCompletedEvent event = (TaskCompletedEvent) domainEventCaptor.getValue();
        Assertions.assertThat(event.taskId()).isEqualTo(taskId);
        Assertions.assertThat(event.ownerId()).isEqualTo(ownerId);
    }

    @Test
    void markCompleted_throwsWhenLinkedScheduleInProgress() {
        Long ownerId = 7L;
        Long taskId = 70L;
        Task task = buildTask(ownerId);
        ReflectionTestUtils.setField(task, "id", taskId);
        Schedule schedule = mock(Schedule.class);
        when(schedule.getOwnerId()).thenReturn(ownerId);
        when(schedule.isInProgress()).thenReturn(true);
        when(scheduleService.findByTaskId(taskId)).thenReturn(Optional.of(schedule));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Assertions.assertThatThrownBy(() -> taskService.markCompleted(ownerId, taskId))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(scheduleStateChangeService);
    }

    @Test
    void markCompleted_finishesLinkedScheduleWhenNotCompleted() {
        Long ownerId = 8L;
        Long taskId = 80L;
        Task task = buildTask(ownerId);
        ReflectionTestUtils.setField(task, "id", taskId);
        Schedule schedule = mock(Schedule.class);
        when(schedule.getOwnerId()).thenReturn(ownerId);
        when(schedule.isInProgress()).thenReturn(false);
        when(schedule.isSuspended()).thenReturn(false);
        when(schedule.isCompleted()).thenReturn(false);
        when(schedule.getDesignatedStartTime()).thenReturn(ZonedDateTime.now());
        when(scheduleService.findByTaskId(taskId)).thenReturn(Optional.of(schedule));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.markCompleted(ownerId, taskId);

        verify(schedule).finish(ArgumentMatchers.any());
    }

    @Test
    void getTasks_readyOnlyUsesInboundFilter() {
        Long ownerId = 9L;
        PageRequest pageable = PageRequest.of(0, 5);
        when(taskRepository.findAllByOwnerIdAndInboundDependencyCountAndCompletedFalse(ownerId, 0, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<Task> result = taskService.getTasks(ownerId, pageable, true);

        Assertions.assertThat(result.getContent()).isEmpty();
        verify(taskRepository).findAllByOwnerIdAndInboundDependencyCountAndCompletedFalse(ownerId, 0, pageable);
        verify(taskRepository, never()).findAllByOwnerId(ownerId, pageable);
    }

    @Test
    void getTasks_returnsAllWhenNotReadyOnly() {
        Long ownerId = 10L;
        PageRequest pageable = PageRequest.of(1, 3);
        when(taskRepository.findAllByOwnerId(ownerId, pageable)).thenReturn(new PageImpl<>(List.of()));

        taskService.getTasks(ownerId, pageable, false);

        verify(taskRepository).findAllByOwnerId(ownerId, pageable);
        verify(taskRepository, never()).findAllByOwnerIdAndInboundDependencyCountAndCompletedFalse(anyLong(), anyInt(), any());
    }

    @Test
    void getTasksByCursor_returnsNextCursorWhenPageFull() {
        Long ownerId = 50L;
        Task t1 = buildTask(ownerId);
        ReflectionTestUtils.setField(t1, "id", 1L);
        Task t2 = buildTask(ownerId);
        ReflectionTestUtils.setField(t2, "id", 2L);
        when(taskRepository.findNextByCursor(eq(ownerId), eq(true), any(), any(), any()))
                .thenReturn(List.of(t1, t2));

        TaskCursorPageResponse resp = taskService.getTasksByCursor(ownerId, 2, null, true);

        Assertions.assertThat(resp.hasNext()).isTrue();
        Assertions.assertThat(resp.nextCursor()).contains("|2");
        verify(taskRepository).findNextByCursor(eq(ownerId), eq(true), any(), any(), any());
    }

    @Test
    void getTasksByCursor_noNextWhenSmallerThanSize() {
        Long ownerId = 51L;
        Task t1 = buildTask(ownerId);
        ReflectionTestUtils.setField(t1, "id", 5L);
        when(taskRepository.findNextByCursor(eq(ownerId), eq(false), any(), any(), any()))
                .thenReturn(List.of(t1));

        TaskCursorPageResponse resp = taskService.getTasksByCursor(ownerId, 2, null, false);

        Assertions.assertThat(resp.hasNext()).isFalse();
        Assertions.assertThat(resp.nextCursor()).isNull();
    }

    @Test
    void markIncomplete_setsFlagAndPublishesEvent() {
        Long ownerId = 6L;
        Long taskId = 60L;
        Task task = buildTask(ownerId);
        ReflectionTestUtils.setField(task, "id", taskId);
        ReflectionTestUtils.setField(task, "completed", true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.markIncomplete(ownerId, taskId);

        Assertions.assertThat(task.isCompleted()).isFalse();
        ArgumentCaptor<DomainEvent> domainEventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(domainEventCaptor.capture());
        Assertions.assertThat(domainEventCaptor.getValue()).isInstanceOf(TaskCanceledEvent.class);
        TaskCanceledEvent event = (TaskCanceledEvent) domainEventCaptor.getValue();
        Assertions.assertThat(event.taskId()).isEqualTo(taskId);
        Assertions.assertThat(event.ownerId()).isEqualTo(ownerId);
    }

    private Task buildTask(Long ownerId) {
        return new Task(
                ownerId,
                "title",
                "desc",
                new TemporalConstraint(ZonedDateTime.now().plusDays(1), Duration.ZERO, TaskType.DEEP_WORK),
                new ImportanceConstraint(5, 5)
        );
    }
}
