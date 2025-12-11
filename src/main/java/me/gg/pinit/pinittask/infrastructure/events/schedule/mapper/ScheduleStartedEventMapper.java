package me.gg.pinit.pinittask.infrastructure.events.schedule.mapper;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleStartedEvent;
import me.gg.pinit.pinittask.infrastructure.events.AmqpEventMapper;
import me.gg.pinit.pinittask.infrastructure.events.schedule.ScheduleMessaging;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleStartedPayload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ScheduleStartedEventMapper implements AmqpEventMapper<ScheduleStartedEvent> {
    @Override
    public Class<ScheduleStartedEvent> eventType() {
        return ScheduleStartedEvent.class;
    }

    @Override
    public String exchange() {
        return ScheduleMessaging.DIRECT_EXCHANGE;
    }

    @Override
    public String routingKey() {
        return ScheduleMessaging.RK_SCHEDULE_STARTED;
    }

    @Override
    public Object payload(ScheduleStartedEvent event) {
        return new ScheduleStartedPayload(
                event.getScheduleId(),
                event.getOwnerId(),
                event.getBeforeState(),
                LocalDateTime.now().toString(),
                UUID.randomUUID().toString()
        );
    }
}
