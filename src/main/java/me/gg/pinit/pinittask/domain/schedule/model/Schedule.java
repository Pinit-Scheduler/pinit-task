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
import me.gg.pinit.pinittask.domain.schedule.exception.TimeOrderReversedException;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
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
    private String title;
    @Getter
    private String description;

    @Column(name = "designated_start_time_utc", columnDefinition = "DATETIME(6)", nullable = false)
    @Convert(converter = InstantToDatetime6UtcConverter.class)
    private Instant designatedStartTimeInstant;

    @Getter
    @Embedded
    private TemporalConstraint temporalConstraint;
    @Getter
    @Embedded
    private ImportanceConstraint importanceConstraint;
    @Getter
    @Embedded
    private ScheduleHistory history = ScheduleHistory.zero();

    @Convert(converter = ScheduleStateConverter.class)
    private ScheduleState state;

    protected Schedule() {}

    public Schedule(Long ownerId, String title, String description, ZonedDateTime zdt, TemporalConstraint tc, ImportanceConstraint ic) {
        this.ownerId = ownerId;
        setTitle(title);
        setDescription(description);
        this.temporalConstraint = tc;
        this.importanceConstraint = ic;
        setDesignatedStartTime(zdt);
        this.state = new NotStartedState();
    }

    public ZonedDateTime getDesignatedStartTime() {
        return designatedStartTimeInstant.atOffset(ZoneOffset.UTC).toZonedDateTime();
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
        patch.importance().ifPresent(this::changeImportance);
        patch.urgency().ifPresent(this::changeUrgency);
        patch.title().ifPresent(this::setTitle);
        patch.description().ifPresent(this::setDescription);
        patch.deadline().ifPresent(this::changeDeadline);
        patch.date().ifPresent(this::setDesignatedStartTime);
        patch.taskType().ifPresent(this::changeTaskType);
    }

    public void deleteSchedule() {
        checkCanDelete();
        DomainEvents.raise(new ScheduleDeletedEvent(this.id, this.ownerId));
    }

    public void setTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    public void setDesignatedStartTime(ZonedDateTime zdt) {
        validateStartTime(zdt);
        this.designatedStartTimeInstant = zdt.toInstant();
        DomainEvents.raise(new ScheduleTimeUpdatedEvent(this.id, this.ownerId, zdt));
    }

    public void setDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void changeDeadline(ZonedDateTime newDeadline) {
        validateDeadline(newDeadline);
        this.temporalConstraint = this.temporalConstraint.changeDeadline(newDeadline);
    }

    public void changeTaskType(TaskType newTaskType) {
        this.temporalConstraint = this.temporalConstraint.changeTaskType(newTaskType);
    }

    public void changeImportance(int newImportance) {
        this.importanceConstraint = this.importanceConstraint.changeImportance(newImportance);
    }

    public void changeUrgency(int newUrgency) {
        this.importanceConstraint = this.importanceConstraint.changeUrgency(newUrgency);
    }

    public String getState() {
        return this.state.toString();
    }

    /**
     * Schedule 패키지 밖에서 사용하지 말 것
     * @param state
     */
    void setState(ScheduleState state) {
        this.state = state;
    }

    void updateHistoryTo(ScheduleHistory history) {
        this.history = history;
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
        }else if (description.length() > 100) {
            throw new IllegalDescriptionException("설명의 길이는 1자 이상 100자 이하여야 합니다.");
        }
    }

    private void validateStartTime(ZonedDateTime zdt) {
        if(zdt.isAfter(this.temporalConstraint.getDeadline())) {
            throw new TimeOrderReversedException("일정의 날짜는 데드라인을 초과할 수 없습니다.");
        }
    }

    private void validateDeadline(ZonedDateTime newDeadline) {
        if (newDeadline.isBefore(this.getDesignatedStartTime())) {
            throw new TimeOrderReversedException("데드라인은 일정 등록 날짜보다 앞설 수 없습니다.");
        }
    }
}

/**
 * 현재 진행중인 일정이 있는지 확인은 어떤 방식으로 해야 하지?
 * 이러면 일정 -> 멤버 방향으로 의존관계가 필요해진다.
 * 모든 일정은 해당 일정 생성자의 통계를 알고 있도록? 너무 많지 않나?
 * 일정 도메인
 * - 목표 일일 작업 시간
 * - 현재 진행하고 있는 일정 << 얘 혼자만 좀 위치가 붕 뜬다. 통계도 아니다 이건
 * 결국 일정이 회원을 알아야 하는 것은 변함이 없음
 * 피할 수 없는 것 -> 알게 할 필요 있음
 * 직접 아는 것이 아니라, 해당 서비스에게 위임하게 만들 수 있나?
 */
