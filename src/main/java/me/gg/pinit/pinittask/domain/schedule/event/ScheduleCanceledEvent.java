package me.gg.pinit.pinittask.domain.schedule.event;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;

import java.time.Duration;
import java.time.ZonedDateTime;

@Getter
public class ScheduleCanceledEvent implements DomainEvent {
    private final Long scheduleId;
    private final Long ownerId;
    private final TaskType taskType;
    private final Duration duration;
    private final ZonedDateTime startTime;

    public ScheduleCanceledEvent(Long scheduleId, Long ownerId, TaskType taskType, Duration duration, ZonedDateTime startTime) {
        this.scheduleId = scheduleId;
        this.ownerId = ownerId;
        this.taskType = taskType;
        this.duration = duration;
        this.startTime = startTime;
    }
}
