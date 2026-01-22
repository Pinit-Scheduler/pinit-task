package me.gg.pinit.pinittask.infrastructure.events.task.handler;

import me.gg.pinit.pinittask.domain.task.event.TaskCanceledEvent;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TaskCanceledIntegratedEventHandler {
    private final RabbitEventPublisher rabbitEventPublisher;

    public TaskCanceledIntegratedEventHandler(RabbitEventPublisher rabbitEventPublisher) {
        this.rabbitEventPublisher = rabbitEventPublisher;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TaskCanceledEvent event) {
        rabbitEventPublisher.publish(event);
    }
}
