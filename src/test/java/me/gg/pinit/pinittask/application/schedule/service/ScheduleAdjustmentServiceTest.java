package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.application.schedule.dto.ScheduleDependencyAdjustCommand;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleAdjustmentServiceTest {

    @Mock
    DependencyService dependencyService;
    @Mock
    ScheduleService scheduleService;
    @Mock
    TaskService taskService;
    @InjectMocks
    ScheduleAdjustmentService scheduleAdjustmentService;

    @Test
    @DisplayName("createSchedule: 작업 없이 일정만 생성")
    void createSchedule() {
        Long memberId = 1L;
        ZonedDateTime now = ZonedDateTime.now();
        ScheduleDependencyAdjustCommand command = new ScheduleDependencyAdjustCommand(
                null,
                memberId,
                null,
                "TITLE",
                "DESC",
                now.plusHours(4),
                5,
                5,
                TaskType.DEEP_WORK,
                now,
                Collections.emptyList(),
                Collections.emptyList()
        );
        when(scheduleService.addSchedule(any(Schedule.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduleAdjustmentService.createSchedule(memberId, command);

        ArgumentCaptor<Schedule> scheduleCap = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleService).addSchedule(scheduleCap.capture());
        Schedule saved = scheduleCap.getValue();
        assertEquals("TITLE", saved.getTitle());
        assertEquals("DESC", saved.getDescription());
        assertEquals(memberId, saved.getOwnerId());

        verifyNoInteractions(taskService);
        verifyNoInteractions(dependencyService);
        verify(scheduleService, never()).updateSchedule(anyLong(), anyLong(), any(SchedulePatch.class));
    }

    @Test
    @DisplayName("createScheduleLegacy: Task와 의존관계 함께 생성 (V0)")
    void createScheduleLegacy_withTaskAndDependencies() {
        Long memberId = 4L;
        ZonedDateTime now = ZonedDateTime.now();
        ScheduleDependencyAdjustCommand command = new ScheduleDependencyAdjustCommand(
                null,
                memberId,
                null,
                "TITLE",
                "DESC",
                now.plusHours(2),
                3,
                5,
                TaskType.DEEP_WORK,
                now,
                Collections.emptyList(),
                List.of(new DependencyDto(null, 10L, 11L))
        );
        Task newTask = mock(Task.class);
        when(newTask.getId()).thenReturn(999L);
        when(taskService.createTask(any(Task.class))).thenReturn(newTask);
        when(scheduleService.addSchedule(any(Schedule.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduleAdjustmentService.createScheduleLegacy(memberId, command);

        verify(dependencyService).checkCycle(eq(memberId), anyList(), anyList());
        verify(taskService).createTask(any(Task.class));
        verify(scheduleService).addSchedule(any(Schedule.class));
        verify(dependencyService).saveAll(anyList());
    }

    @Test
    @DisplayName("createSchedule: 의존 관계 요청 시 예외")
    void createScheduleWithDependenciesThrows() {
        Long memberId = 9L;
        ZonedDateTime now = ZonedDateTime.now();
        ScheduleDependencyAdjustCommand command = new ScheduleDependencyAdjustCommand(
                null,
                memberId,
                null,
                "TITLE",
                "DESC",
                now.plusHours(4),
                5,
                5,
                TaskType.DEEP_WORK,
                now,
                List.of(new DependencyDto(null, 1L, 2L)),
                Collections.emptyList()
        );

        assertThrows(IllegalArgumentException.class, () -> scheduleAdjustmentService.createSchedule(memberId, command));
        verifyNoInteractions(scheduleService);
    }

    @Test
    @DisplayName("adjustSchedule: 사이클 체크 후 일정 패치 및 의존 삭제/추가")
    void adjustSchedule() {
        Long memberId = 2L;
        Long scheduleId = 99L;
        ZonedDateTime now = ZonedDateTime.now();
        ScheduleDependencyAdjustCommand command = new ScheduleDependencyAdjustCommand(
                scheduleId,
                memberId,
                null,
                "NEW_TITLE",
                "NEW_DESC",
                now.plusHours(8),
                9,
                3,
                TaskType.QUICK_TASK,
                now.plusMinutes(30),
                List.of(new DependencyDto(1L, 200L, 201L), new DependencyDto(2L, 201L, 202L)),
                List.of(new DependencyDto(3L, 300L, 301L))
        );
        Schedule current = mock(Schedule.class);
        when(current.getTaskId()).thenReturn(10L);
        when(scheduleService.getSchedule(memberId, scheduleId)).thenReturn(current);
        when(dependencyService.checkCycle(eq(memberId), anyList(), anyList())).thenReturn(false);
        when(scheduleService.updateSchedule(eq(memberId), eq(scheduleId), any(SchedulePatch.class))).thenReturn(mock(Schedule.class));

        scheduleAdjustmentService.adjustSchedule(memberId, command);

        ArgumentCaptor<List> removedCap = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> addedCap = ArgumentCaptor.forClass(List.class);
        verify(dependencyService).checkCycle(eq(memberId), removedCap.capture(), addedCap.capture());
        assertEquals(2, removedCap.getValue().size());
        assertEquals(1, addedCap.getValue().size());

        ArgumentCaptor<SchedulePatch> patchCap = ArgumentCaptor.forClass(SchedulePatch.class);
        verify(scheduleService).updateSchedule(eq(memberId), eq(scheduleId), patchCap.capture());
        SchedulePatch patch = patchCap.getValue();
        assertEquals("NEW_TITLE", patch.title().orElse(null));
        assertEquals("NEW_DESC", patch.description().orElse(null));
        assertEquals(now.plusMinutes(30), patch.designatedStartTime().orElse(null));

        verify(dependencyService).deleteAll(anyList());
        verify(dependencyService).saveAll(anyList());
        verify(scheduleService, never()).addSchedule(any(Schedule.class));
    }

    @Test
    @DisplayName("createSchedule: 작업 ID가 포함되면 예외 발생")
    void createScheduleWithExistingTask() {
        Long memberId = 3L;
        Long taskId = 901L;
        ZonedDateTime now = ZonedDateTime.now();
        ScheduleDependencyAdjustCommand command = new ScheduleDependencyAdjustCommand(
                null,
                memberId,
                taskId,
                "TITLE",
                "DESC",
                now.plusHours(2),
                4,
                8,
                TaskType.ADMIN_TASK,
                now,
                Collections.emptyList(),
                Collections.emptyList()
        );
        assertThrows(IllegalArgumentException.class, () -> scheduleAdjustmentService.createSchedule(memberId, command));
        verifyNoInteractions(taskService);
        verify(scheduleService, never()).addSchedule(any(Schedule.class));
    }
}
