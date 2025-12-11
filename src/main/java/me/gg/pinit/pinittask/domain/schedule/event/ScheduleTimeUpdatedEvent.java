package me.gg.pinit.pinittask.domain.schedule.event;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.events.DomainEvent;

import java.time.ZonedDateTime;

@Getter
public class ScheduleTimeUpdatedEvent implements DomainEvent {
    private final Long scheduleId;
    private final Long ownerId;
    private final ZonedDateTime scheduledTime;

    public ScheduleTimeUpdatedEvent(Long scheduleId, Long ownerId, ZonedDateTime scheduledTime) {
        this.scheduleId = scheduleId;
        this.ownerId = ownerId;
        this.scheduledTime = scheduledTime;
    }
}
