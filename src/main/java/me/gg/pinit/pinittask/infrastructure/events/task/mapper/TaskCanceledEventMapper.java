package me.gg.pinit.pinittask.infrastructure.events.task.mapper;

import me.gg.pinit.pinittask.domain.task.event.TaskCanceledEvent;
import me.gg.pinit.pinittask.infrastructure.events.AmqpEventMapper;
import me.gg.pinit.pinittask.infrastructure.events.task.TaskMessaging;
import me.gg.pinit.pinittask.infrastructure.events.task.dto.TaskCanceledPayload;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class TaskCanceledEventMapper implements AmqpEventMapper<TaskCanceledEvent> {
    @Override
    public Class<TaskCanceledEvent> eventType() {
        return TaskCanceledEvent.class;
    }

    @Override
    public String exchange() {
        return TaskMessaging.DIRECT_EXCHANGE;
    }

    @Override
    public String routingKey() {
        return TaskMessaging.RK_TASK_CANCELED;
    }

    @Override
    public Object payload(TaskCanceledEvent event) {
        return new TaskCanceledPayload(
                event.taskId(),
                event.ownerId(),
                OffsetDateTime.now().toString(),
                UUID.randomUUID().toString()
        );
    }
}
