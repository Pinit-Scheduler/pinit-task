package me.gg.pinit.pinittask.infrastructure.events.schedule.mapper;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleTimeUpdatedEvent;
import me.gg.pinit.pinittask.infrastructure.events.AmqpEventMapper;
import me.gg.pinit.pinittask.infrastructure.events.schedule.ScheduleMessaging;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleTimeUpdatedPayload;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduleTimeUpdatedEventMapper implements AmqpEventMapper<ScheduleTimeUpdatedEvent> {
    @Override
    public Class<ScheduleTimeUpdatedEvent> eventType() {
        return ScheduleTimeUpdatedEvent.class;
    }

    @Override
    public String exchange() {
        return ScheduleMessaging.DIRECT_EXCHANGE;
    }

    @Override
    public String routingKey() {
        return ScheduleMessaging.RK_SCHEDULE_TIME_UPDATED;
    }

    @Override
    public Object payload(ScheduleTimeUpdatedEvent event) {
        return new ScheduleTimeUpdatedPayload(
                event.getScheduleId(),
                event.getOwnerId(),
                event.getScheduledTime().toString(),
                LocalDateTime.now().toString(),
                UUID.randomUUID().toString()
        );
    }
}
