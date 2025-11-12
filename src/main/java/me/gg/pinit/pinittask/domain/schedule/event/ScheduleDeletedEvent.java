package me.gg.pinit.pinittask.domain.schedule.event;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.events.DomainEvent;

@Getter
public class ScheduleDeletedEvent implements DomainEvent {
    private Long scheduleId;
    private Long ownerId;

    public ScheduleDeletedEvent(Long scheduleId, Long ownerId) {
        this.scheduleId = scheduleId;
        this.ownerId = ownerId;
    }
}
