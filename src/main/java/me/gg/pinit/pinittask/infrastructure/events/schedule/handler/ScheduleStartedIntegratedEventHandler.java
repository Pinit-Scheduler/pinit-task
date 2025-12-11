package me.gg.pinit.pinittask.infrastructure.events.schedule.handler;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleStartedEvent;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleStartedIntegratedEventHandler {
    private final RabbitEventPublisher rabbitEventPublisher;

    public ScheduleStartedIntegratedEventHandler(RabbitEventPublisher rabbitEventPublisher) {
        this.rabbitEventPublisher = rabbitEventPublisher;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ScheduleStartedEvent event) {
        rabbitEventPublisher.publish(event);
    }
}
