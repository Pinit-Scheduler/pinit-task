package me.gg.pinit.pinittask.infrastructure.events.schedule.mapper;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.infrastructure.events.AmqpEventMapper;
import me.gg.pinit.pinittask.infrastructure.events.schedule.ScheduleMessaging;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleCompletedPayload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ScheduleCompletedEventMapper implements AmqpEventMapper<ScheduleCompletedEvent> {
    @Override
    public Class<ScheduleCompletedEvent> eventType() {
        return ScheduleCompletedEvent.class;
    }

    @Override
    public String exchange() {
        return ScheduleMessaging.DIRECT_EXCHANGE;
    }

    @Override
    public String routingKey() {
        return ScheduleMessaging.RK_SCHEDULE_COMPLETED;
    }

    @Override
    public Object payload(ScheduleCompletedEvent event) {
        return new ScheduleCompletedPayload(
                event.getScheduleId(),
                event.getOwnerId(),
                event.getBeforeState(),
                LocalDateTime.now().toString(),
                UUID.randomUUID().toString()
        );
    }
}
