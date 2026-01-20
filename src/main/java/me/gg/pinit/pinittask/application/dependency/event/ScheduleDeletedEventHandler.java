package me.gg.pinit.pinittask.application.dependency.event;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleDeletedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleDeletedEventHandler {
    private final DependencyService dependencyService;

    public ScheduleDeletedEventHandler(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ScheduleDeletedEvent event) {
        if (event.getTaskId() != null) {
            dependencyService.deleteWithTaskId(event.getTaskId());
        }
    }
}
