package me.gg.pinit.pinittask.domain.schedule.model;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.InstantToDatetime6UtcConverter;
import me.gg.pinit.pinittask.domain.converter.service.ScheduleStateConverter;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleDeletedEvent;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleTimeUpdatedEvent;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalChangeException;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalDescriptionException;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTitleException;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_schedule_task", columnNames = {"task_id"})
        }
)
public class Schedule {
    @Id
    @Column(name = "schedule_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @Version
    private Long version;

    @Getter
    private Long ownerId;

    @Getter
    @Column(name = "task_id")
    private Long taskId;

    @Getter
    private String title;
    @Getter
    private String description;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;

    @Column(name = "designated_start_time_utc", columnDefinition = "DATETIME(6)", nullable = false)
    @Convert(converter = InstantToDatetime6UtcConverter.class)
    private Instant designatedStartTime;

    @Getter
    @Embedded
    private ScheduleHistory history = ScheduleHistory.zero();

    @Convert(converter = ScheduleStateConverter.class)
    private ScheduleState state;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "DATETIME(6)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(columnDefinition = "DATETIME(6)")
    private Instant updatedAt;

    protected Schedule() {
    }

    public Schedule(Long ownerId, Long taskId, String title, String description, ZonedDateTime designatedStartTime, ScheduleType scheduleType) {
        this.ownerId = ownerId;
        this.taskId = taskId;
        setTitle(title);
        setDescription(description);
        setScheduleType(scheduleType);
        setDesignatedStartTime(designatedStartTime);
        this.history = ScheduleHistory.zero();
        this.state = new NotStartedState();
    }

    public ZonedDateTime getDesignatedStartTime() {
        return designatedStartTime.atOffset(ZoneOffset.UTC).toZonedDateTime();
    }

    public void start(ZonedDateTime startTime) {
        state.start(this, startTime);
    }

    public void suspend(ZonedDateTime suspendTime) {
        state.suspend(this, suspendTime);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void finish(ZonedDateTime finishTime) {
        state.finish(this, finishTime);
    }

    public boolean isCompleted() {
        return state instanceof CompletedState;
    }

    public boolean isNotStarted() {
        return state instanceof NotStartedState;
    }

    public boolean isInProgress() {
        return state instanceof InProgressState;
    }

    public boolean isSuspended() {
        return state instanceof SuspendedState;
    }

    public void patch(SchedulePatch patch) {
        checkStateIsNotCompleted();
        patch.title().ifPresent(this::setTitle);
        patch.description().ifPresent(this::setDescription);
        patch.designatedStartTime().ifPresent(this::setDesignatedStartTime);
        patch.scheduleType().ifPresent(this::setScheduleType);
    }

    public void deleteSchedule() {
        checkCanDelete();
        DomainEvents.raise(new ScheduleDeletedEvent(this.id, this.ownerId, this.taskId));
    }

    public void detachTask() {
        this.taskId = null;
    }

    public void setTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    public void setDesignatedStartTime(ZonedDateTime zdt) {
        if (zdt == null) {
            throw new IllegalArgumentException("일정 시작 시각은 null일 수 없습니다.");
        }
        this.designatedStartTime = zdt.toInstant();
        DomainEvents.raise(new ScheduleTimeUpdatedEvent(this.id, this.ownerId, zdt));
    }

    public void setDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        if (scheduleType == null) {
            throw new IllegalArgumentException("일정 유형은 null일 수 없습니다.");
        }
        this.scheduleType = scheduleType;
    }

    public String getState() {
        return this.state.toString();
    }

    /**
     * Schedule 패키지 밖에서 사용하지 말 것
     * @param state state to apply
     */
    void setState(ScheduleState state) {
        this.state = state;
    }

    void updateHistoryTo(ScheduleHistory history) {
        this.history = history;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void checkStateIsNotCompleted() {
        if (!isNotStarted()) {
            throw new IllegalChangeException("시작되지 않은 일정만 수정할 수 있습니다.");
        }
    }

    private void checkCanDelete() {
        if (!(isNotStarted() || isCompleted())) {
            throw new IllegalChangeException("시작되지 않았거나 완료된 일정만 삭제할 수 있습니다.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalTitleException("제목의 길이는 1자 이상 20자 이하여야 합니다.");
        } else if (title.length() > 20) {
            throw new IllegalTitleException("제목의 길이는 1자 이상 20자 이하여야 합니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalDescriptionException("설명의 길이는 1자 이상 100자 이하여야 합니다.");
        } else if (description.length() > 100) {
            throw new IllegalDescriptionException("설명의 길이는 1자 이상 100자 이하여야 합니다.");
        }
    }
}
