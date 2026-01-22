package me.gg.pinit.pinittask.application.statistics.event.handler;

import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.statistics.service.StatisticsService;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.task.model.Task;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleCompletedEventHandler {
    private final StatisticsService statisticsService;
    private final ScheduleService scheduleService;
    private final TaskService taskService;

    public ScheduleCompletedEventHandler(StatisticsService statisticsService, ScheduleService scheduleService, TaskService taskService) {
        this.statisticsService = statisticsService;
        this.scheduleService = scheduleService;
        this.taskService = taskService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ScheduleCompletedEvent event) {
        Schedule schedule = scheduleService.getSchedule(event.getOwnerId(), event.getScheduleId());
        if (schedule.getTaskId() == null) {
            return;
        }
        Task task = taskService.getTask(event.getOwnerId(), schedule.getTaskId());
        statisticsService.addElapsedTime(
                event.getOwnerId(),
                task.getTemporalConstraint().getTaskType(),
                schedule.getHistory().getElapsedTime(),
                schedule.getDesignatedStartTime()
        );
    }
}
