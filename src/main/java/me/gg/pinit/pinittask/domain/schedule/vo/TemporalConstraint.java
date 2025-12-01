package me.gg.pinit.pinittask.domain.schedule.vo;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;
import me.gg.pinit.pinittask.domain.datetime.ZonedDateTimeAttribute;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

@Embeddable
public class TemporalConstraint {
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dateTime", column = @Column(name = "deadline_time")),
            @AttributeOverride(name = "zoneId", column = @Column(name = "deadline_zone_id"))
    })
    private ZonedDateTimeAttribute deadline;
    @Convert(converter = DurationConverter.class)
    @Column(name = "expected_duration")
    private Duration duration;
    @Getter
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    protected TemporalConstraint() {
    }

    public TemporalConstraint(ZonedDateTime deadline, Duration duration, TaskType taskType) {
        this.deadline = ZonedDateTimeAttribute.from(deadline);
        this.duration = duration;
        this.taskType = taskType;
    }

    public TemporalConstraint changeDeadline(ZonedDateTime newDeadline) {
        return new TemporalConstraint(newDeadline, this.duration, this.taskType);
    }

    public TemporalConstraint changeTaskType(TaskType newTaskType) {
        return new TemporalConstraint(this.getDeadline(), this.duration, newTaskType);
    }

    public ZonedDateTime getDeadline() {
        return deadline.toZonedDateTime();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TemporalConstraint that = (TemporalConstraint) o;
        return Objects.equals(deadline, that.deadline) && Objects.equals(duration, that.duration) && taskType == that.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deadline, duration, taskType);
    }
}
