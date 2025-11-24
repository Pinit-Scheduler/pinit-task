package me.gg.pinit.pinittask.application.statistics.event.handler;

import me.gg.pinit.pinittask.application.statistics.service.StatisticsService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleCanceledEventHandler {
    private final StatisticsService statisticsService;

    public ScheduleCanceledEventHandler(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ScheduleCanceledEvent event) {
        statisticsService.removeElapsedTime(
                event.getOwnerId(),
                event.getTaskType(),
                event.getDuration(),
                event.getStartTime()
        );
    }
}
