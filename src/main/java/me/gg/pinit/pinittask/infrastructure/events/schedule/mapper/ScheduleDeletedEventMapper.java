package me.gg.pinit.pinittask.infrastructure.events.schedule.mapper;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleDeletedEvent;
import me.gg.pinit.pinittask.infrastructure.events.AmqpEventMapper;
import me.gg.pinit.pinittask.infrastructure.events.schedule.ScheduleMessaging;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleDeletedPayload;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduleDeletedEventMapper implements AmqpEventMapper<ScheduleDeletedEvent> {
    @Override
    public Class<ScheduleDeletedEvent> eventType() {
        return ScheduleDeletedEvent.class;
    }

    @Override
    public String exchange() {
        return ScheduleMessaging.DIRECT_EXCHANGE;
    }

    @Override
    public String routingKey() {
        return ScheduleMessaging.RK_SCHEDULE_DELETED;
    }

    @Override
    public Object payload(ScheduleDeletedEvent event) {
        return new ScheduleDeletedPayload(
                event.getScheduleId(),
                event.getOwnerId(),
                LocalDateTime.now().toString(),
                UUID.randomUUID().toString()
        );
    }
}
