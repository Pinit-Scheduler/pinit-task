package me.gg.pinit.pinittask.infrastructure.events.task.mapper;

import me.gg.pinit.pinittask.domain.task.event.TaskCompletedEvent;
import me.gg.pinit.pinittask.infrastructure.events.AmqpEventMapper;
import me.gg.pinit.pinittask.infrastructure.events.task.TaskMessaging;
import me.gg.pinit.pinittask.infrastructure.events.task.dto.TaskCompletedPayload;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class TaskCompletedEventMapper implements AmqpEventMapper<TaskCompletedEvent> {
    @Override
    public Class<TaskCompletedEvent> eventType() {
        return TaskCompletedEvent.class;
    }

    @Override
    public String exchange() {
        return TaskMessaging.DIRECT_EXCHANGE;
    }

    @Override
    public String routingKey() {
        return TaskMessaging.RK_TASK_COMPLETED;
    }

    @Override
    public Object payload(TaskCompletedEvent event) {
        return new TaskCompletedPayload(
                event.taskId(),
                event.ownerId(),
                OffsetDateTime.now().toString(),
                UUID.randomUUID().toString()
        );
    }
}
