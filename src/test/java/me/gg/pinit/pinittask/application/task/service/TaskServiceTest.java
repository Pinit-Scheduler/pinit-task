package me.gg.pinit.pinittask.application.task.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.events.DomainEventPublisher;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.task.event.TaskCanceledEvent;
import me.gg.pinit.pinittask.domain.task.event.TaskCompletedEvent;
import me.gg.pinit.pinittask.domain.task.exception.TaskNotFoundException;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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
