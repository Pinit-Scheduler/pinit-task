package me.gg.pinit.pinittask.application.statistics.event.handler;

import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.statistics.service.StatisticsService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleCompletedEventHandler {
    private final StatisticsService statisticsService;
    private final ScheduleService scheduleService;

    public ScheduleCompletedEventHandler(StatisticsService statisticsService, ScheduleService scheduleService) {
        this.statisticsService = statisticsService;
        this.scheduleService = scheduleService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ScheduleCompletedEvent event) {
        Schedule schedule = scheduleService.getSchedule(event.getOwnerId(), event.getScheduleId());
        statisticsService.addElapsedTime(
                event.getOwnerId(),
                schedule.getScheduleType(),
                schedule.getHistory().getElapsedTime(),
                schedule.getDesignatedStartTime()
        );
    }
}
