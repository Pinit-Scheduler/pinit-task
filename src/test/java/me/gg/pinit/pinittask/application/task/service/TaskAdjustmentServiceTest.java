package me.gg.pinit.pinittask.application.task.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.application.task.dto.TaskDependencyAdjustCommand;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.patch.TaskPatch;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskAdjustmentService")
class TaskAdjustmentServiceTest {

    @Mock
    DependencyService dependencyService;
    @Mock
    TaskService taskService;
    @InjectMocks
    TaskAdjustmentService taskAdjustmentService;

    @Test
    @DisplayName("createTask는 사이클을 확인하고 작업을 저장한 뒤 의존성을 저장한다")
    void createTask_checksCycleThenCreatesTaskAndSavesDependencies() {
        Long memberId = 10L;
        ZonedDateTime dueDate = ZonedDateTime.parse("2024-01-10T09:00:00Z");
        TaskDependencyAdjustCommand command = new TaskDependencyAdjustCommand(
                null,
                memberId,
                "new task",
                "new description",
                dueDate,
                5,
                5,
                List.of(),
                List.of(
                        new DependencyDto(null, 1L, 2L),
                        new DependencyDto(null, 3L, 4L)
                )
        );
        Task savedTask = new Task(
                memberId,
                "new task",
                "new description",
                new TemporalConstraint(dueDate, Duration.ZERO),
                new ImportanceConstraint(5, 5)
        );
        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        Task result = taskAdjustmentService.createTask(memberId, command);

        assertThat(result).isSameAs(savedTask);

        ArgumentCaptor<List<Dependency>> addedDependenciesCaptor = dependencyListCaptor();
        ArgumentCaptor<Task> createdTaskCaptor = ArgumentCaptor.forClass(Task.class);
        ArgumentCaptor<List<Dependency>> savedDependenciesCaptor = dependencyListCaptor();

        InOrder inOrder = inOrder(taskService, dependencyService);
        inOrder.verify(taskService).createTask(createdTaskCaptor.capture());
        inOrder.verify(dependencyService).assertNoCycle(eq(memberId), eq(List.of()), addedDependenciesCaptor.capture());
        inOrder.verify(dependencyService).saveAll(savedDependenciesCaptor.capture());

        Task createdTask = createdTaskCaptor.getValue();
        assertThat(createdTask.getOwnerId()).isEqualTo(memberId);
        assertThat(createdTask.getTitle()).isEqualTo("new task");
        assertThat(createdTask.getDescription()).isEqualTo("new description");
        assertThat(createdTask.getDueDate()).isEqualTo(dueDate);
        assertThat(createdTask.getImportanceConstraint().getImportance()).isEqualTo(5);
        assertThat(createdTask.getImportanceConstraint().getDifficulty()).isEqualTo(5);

        List<Dependency> addedDependencies = addedDependenciesCaptor.getValue();
        assertThat(addedDependencies)
                .extracting(Dependency::getFromId, Dependency::getToId)
                .containsExactly(
                        tuple(1L, 2L),
                        tuple(3L, 4L)
                );
        assertThat(savedDependenciesCaptor.getValue()).isSameAs(addedDependencies);
    }

    @Test
    @DisplayName("updateTask는 사이클 확인 후 작업을 수정하고 의존성 삭제/저장을 수행한다")
    void updateTask_checksCycleAndAdjustsDependenciesAroundUpdate() {
        Long memberId = 20L;
        Long taskId = 200L;
        ZonedDateTime dueDate = ZonedDateTime.parse("2024-02-15T12:00:00Z");
        TaskDependencyAdjustCommand command = new TaskDependencyAdjustCommand(
                taskId,
                memberId,
                "updated title",
                "updated description",
                dueDate,
                7,
                8,
                List.of(
                        new DependencyDto(1L, 5L, 6L)
                ),
                List.of(
                        new DependencyDto(2L, 7L, 8L),
                        new DependencyDto(3L, 9L, 10L)
                )
        );
        Task updatedTask = new Task(
                memberId,
                "updated title",
                "updated description",
                new TemporalConstraint(dueDate, Duration.ZERO),
                new ImportanceConstraint(7, 8)
        );
        when(taskService.updateTask(anyLong(), anyLong(), any(TaskPatch.class))).thenReturn(updatedTask);

        Task result = taskAdjustmentService.updateTask(memberId, command);

        assertThat(result).isSameAs(updatedTask);

        ArgumentCaptor<List<Dependency>> removedDependenciesCaptor = dependencyListCaptor();
        ArgumentCaptor<List<Dependency>> addedDependenciesCaptor = dependencyListCaptor();
        ArgumentCaptor<TaskPatch> patchCaptor = ArgumentCaptor.forClass(TaskPatch.class);
        ArgumentCaptor<List<Dependency>> deletedDependenciesCaptor = dependencyListCaptor();
        ArgumentCaptor<List<Dependency>> savedDependenciesCaptor = dependencyListCaptor();

        InOrder inOrder = inOrder(dependencyService, taskService);
        inOrder.verify(dependencyService).assertNoCycle(eq(memberId), removedDependenciesCaptor.capture(), addedDependenciesCaptor.capture());
        inOrder.verify(taskService).updateTask(eq(memberId), eq(taskId), patchCaptor.capture());
        inOrder.verify(dependencyService).deleteAll(deletedDependenciesCaptor.capture());
        inOrder.verify(dependencyService).saveAll(savedDependenciesCaptor.capture());

        TaskPatch patch = patchCaptor.getValue();
        assertThat(patch.title()).contains("updated title");
        assertThat(patch.description()).contains("updated description");
        assertThat(patch.dueDate()).contains(dueDate);
        assertThat(patch.importance()).contains(7);
        assertThat(patch.difficulty()).contains(8);

        List<Dependency> removedDependencies = removedDependenciesCaptor.getValue();
        assertThat(removedDependencies)
                .extracting(Dependency::getFromId, Dependency::getToId)
                .containsExactly(tuple(5L, 6L));

        List<Dependency> addedDependencies = addedDependenciesCaptor.getValue();
        assertThat(addedDependencies)
                .extracting(Dependency::getFromId, Dependency::getToId)
                .containsExactly(
                        tuple(7L, 8L),
                        tuple(9L, 10L)
                );

        assertThat(deletedDependenciesCaptor.getValue()).isSameAs(removedDependencies);
        assertThat(savedDependenciesCaptor.getValue()).isSameAs(addedDependencies);
    }

    @Test
    @DisplayName("updateTask에서 0 플레이스홀더 사용 시 예외")
    void updateTask_rejectsPlaceholderZero() {
        Long memberId = 1L;
        Long taskId = 10L;
        TaskDependencyAdjustCommand command = new TaskDependencyAdjustCommand(
                taskId,
                memberId,
                "title",
                "desc",
                ZonedDateTime.now(),
                5,
                5,
                List.of(),
                List.of(new DependencyDto(null, 0L, 2L))
        );

        assertThrows(IllegalArgumentException.class, () -> taskAdjustmentService.updateTask(memberId, command));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<Dependency>> dependencyListCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
