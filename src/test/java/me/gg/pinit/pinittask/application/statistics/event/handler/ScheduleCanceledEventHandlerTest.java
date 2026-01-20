package me.gg.pinit.pinittask.application.statistics.event.handler;

import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.statistics.service.StatisticsService;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.domain.schedule.model.CompletedState;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import me.gg.pinit.pinittask.domain.task.model.TaskUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleCanceledEventHandlerTest {
    private final Long scheduleId = 11L;
    private final Long ownerId = 22L;
    @Mock
    StatisticsService statisticsService;
    @Mock
    ScheduleService scheduleService;
    @Mock
    TaskService taskService;
    @Mock
    Schedule schedule;
    @InjectMocks
    ScheduleCanceledEventHandler handler;

    @Test
    void ignoresCancelWhenScheduleWasNotCompleted() {
        //given
        ScheduleCanceledEvent event = new ScheduleCanceledEvent(scheduleId, ownerId, "IN_PROGRESS");

        //when
        handler.on(event);

        //then
        verifyNoInteractions(scheduleService, statisticsService);
    }

    @Test
    void removesElapsedTimeWhenCompletedScheduleIsCanceled() {
        //given
        ZonedDateTime designatedStart = ZonedDateTime.now();
        ScheduleHistory history = new ScheduleHistory(null, Duration.ofMinutes(30));
        ScheduleCanceledEvent event = new ScheduleCanceledEvent(scheduleId, ownerId, CompletedState.COMPLETED);
        Task task = TaskUtils.newTask(ownerId, 33L);
        task.changeTaskType(TaskType.ADMIN_TASK);

        when(scheduleService.getSchedule(ownerId, scheduleId)).thenReturn(schedule);
        when(schedule.getTaskId()).thenReturn(task.getId());
        when(schedule.getHistory()).thenReturn(history);
        when(schedule.getDesignatedStartTime()).thenReturn(designatedStart);
        when(taskService.getTask(ownerId, task.getId())).thenReturn(task);

        //when
        handler.on(event);

        //then
        verify(scheduleService).getSchedule(ownerId, scheduleId);
        verify(statisticsService).removeElapsedTime(ownerId, TaskType.ADMIN_TASK, history.getElapsedTime(), designatedStart);
    }
}
