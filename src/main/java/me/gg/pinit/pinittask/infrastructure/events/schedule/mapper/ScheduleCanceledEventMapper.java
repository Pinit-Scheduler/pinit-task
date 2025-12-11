package me.gg.pinit.pinittask.infrastructure.events.schedule.mapper;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.infrastructure.events.AmqpEventMapper;
import me.gg.pinit.pinittask.infrastructure.events.schedule.ScheduleMessaging;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleCanceledPayload;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduleCanceledEventMapper implements AmqpEventMapper<ScheduleCanceledEvent> {
    @Override
    public Class<ScheduleCanceledEvent> eventType() {
        return ScheduleCanceledEvent.class;
    }

    @Override
    public String exchange() {
        return ScheduleMessaging.DIRECT_EXCHANGE;
    }

    @Override
    public String routingKey() {
        return ScheduleMessaging.RK_SCHEDULE_CANCELED;
    }

    @Override
    public Object payload(ScheduleCanceledEvent event) {
        return new ScheduleCanceledPayload(event.getScheduleId(), event.getOwnerId(), event.getBeforeState(), LocalDateTime.now().toString(), UUID.randomUUID().toString());
    }
}
