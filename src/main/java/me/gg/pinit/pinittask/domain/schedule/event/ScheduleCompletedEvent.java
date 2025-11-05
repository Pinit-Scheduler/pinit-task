package me.gg.pinit.pinittask.domain.schedule.event;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;

import java.time.Duration;

@Getter
public class ScheduleCompletedEvent implements DomainEvent {
    private final Long ownerId;
    private final TaskType taskType;
    private final Duration duration;
    public ScheduleCompletedEvent(Long ownerId, TaskType taskType, Duration duration) {
        this.ownerId = ownerId;
        this.taskType = taskType;
        this.duration = duration;
    }
}
