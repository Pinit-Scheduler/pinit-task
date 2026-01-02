package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.application.schedule.dto.ScheduleDependencyAdjustCommand;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleAdjustmentServiceTest {

    @Mock
    DependencyService dependencyService;
    @Mock
    ScheduleService scheduleService;
    @InjectMocks
    ScheduleAdjustmentService scheduleAdjustmentService;

    @Test
    @DisplayName("createSchedule: 사이클 체크 후 일정 추가 및 의존 저장")
    void createSchedule() {
        Long memberId = 1L;
        ZonedDateTime now = ZonedDateTime.now();
        ScheduleDependencyAdjustCommand command = new ScheduleDependencyAdjustCommand(
                null,
                memberId,
                "TITLE",
                "DESC",
                now.plusHours(4),
                5,
                6,
                TaskType.DEEP_WORK,
                now,
                Collections.emptyList(),
                List.of(new DependencyDto(null, 10L, 11L), new DependencyDto(null, 11L, 12L))
        );
        when(dependencyService.checkCycle(eq(memberId), anyList(), anyList())).thenReturn(false);
        when(scheduleService.addSchedule(any(Schedule.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduleAdjustmentService.createSchedule(memberId, command);

        ArgumentCaptor<List> removedCap = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> addedCap = ArgumentCaptor.forClass(List.class);
        verify(dependencyService).checkCycle(eq(memberId), removedCap.capture(), addedCap.capture());
        assertTrue(removedCap.getValue().isEmpty());
        assertEquals(2, addedCap.getValue().size());
        Dependency firstAdded = (Dependency) addedCap.getValue().get(0);
        assertEquals(10L, firstAdded.getFromId());
        assertEquals(11L, firstAdded.getToId());

        ArgumentCaptor<Schedule> scheduleCap = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleService).addSchedule(scheduleCap.capture());
        Schedule saved = scheduleCap.getValue();
        assertEquals("TITLE", saved.getTitle());
        assertEquals("DESC", saved.getDescription());
        assertEquals(memberId, saved.getOwnerId());

        ArgumentCaptor<List> saveCap = ArgumentCaptor.forClass(List.class);
        verify(dependencyService).saveAll(saveCap.capture());
        assertEquals(2, saveCap.getValue().size());

        verify(dependencyService, never()).deleteAll(anyList());
        verify(scheduleService, never()).updateSchedule(anyLong(), anyLong(), any(SchedulePatch.class));
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
        assertEquals(9, patch.importance().orElse(-1));
        assertEquals(3, patch.difficulty().orElse(-1));
        assertEquals(TaskType.QUICK_TASK, patch.taskType().orElse(null));

        verify(dependencyService).deleteAll(anyList());
        verify(dependencyService).saveAll(anyList());
        verify(scheduleService, never()).addSchedule(any(Schedule.class));
    }
}