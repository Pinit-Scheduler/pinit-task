package me.gg.pinit.pinittask.domain.task.vo;

import jakarta.persistence.*;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;
import me.gg.pinit.pinittask.domain.datetime.ZonedDateAttribute;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

@Embeddable
public class TemporalConstraint {
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "date", column = @Column(name = "deadline_date")),
            @AttributeOverride(name = "offsetId", column = @Column(name = "deadline_offset_id"))
    })
    private ZonedDateAttribute deadline;
    @Convert(converter = DurationConverter.class)
    @Column(name = "expected_duration")
    private Duration duration;

    protected TemporalConstraint() {
    }

    public TemporalConstraint(ZonedDateTime deadline, Duration duration) {
        this(ZonedDateAttribute.from(deadline), duration);
    }

    public TemporalConstraint(LocalDate deadlineDate, ZoneOffset offset, Duration duration) {
        this(ZonedDateAttribute.of(deadlineDate, offset), duration);
    }

    public TemporalConstraint(ZonedDateAttribute deadline, Duration duration) {
        this.deadline = Objects.requireNonNull(deadline, "deadline must not be null");
        this.duration = Objects.requireNonNull(duration, "duration must not be null");
    }

    public TemporalConstraint changeDeadline(ZonedDateTime newDeadline) {
        return new TemporalConstraint(newDeadline, this.duration);
    }

    public TemporalConstraint changeDeadline(LocalDate newDeadlineDate, ZoneOffset offset) {
        return new TemporalConstraint(newDeadlineDate, offset, this.duration);
    }

    /**
     * Returns deadline at start-of-day using stored zone offset.
     * Time component is always 00:00 and region ZoneId information is not preserved.
     */
    public ZonedDateTime getDeadline() {
        return deadline.toZonedDateTime();
    }

    public LocalDate getDeadlineDate() {
        return deadline.getDate();
    }

    public ZoneOffset getDeadlineOffset() {
        return deadline.getOffset();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TemporalConstraint that = (TemporalConstraint) o;
        return Objects.equals(deadline, that.deadline) && Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deadline, duration);
    }
}
