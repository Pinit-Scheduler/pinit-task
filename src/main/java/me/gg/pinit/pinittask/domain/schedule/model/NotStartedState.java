package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;

import java.time.Duration;
import java.time.ZonedDateTime;

public class NotStartedState implements ScheduleState{
    public static final String NOT_STARTED = "NOT_STARTED";
    @Override
    public void start(Schedule ctx, ZonedDateTime startTime) {
        ScheduleHistory history = ctx.getHistory();
        ctx.updateHistoryTo(history.recordStart(startTime));
        ctx.setState(new InProgressState());
    }

    @Override
    public void suspend(Schedule ctx, ZonedDateTime suspendTime) {
        throw new IllegalStateException("시작되지 않은 일정을 일시정지할 수 없습니다.");
    }

    @Override
    public void cancel(Schedule ctx) {
        throw new IllegalTransitionException("시작되지 않은 일정을 취소할 수 없습니다.");
    }

    @Override
    public void finish(Schedule ctx, ZonedDateTime finishTime) {
        DomainEvents.raise(new ScheduleCompletedEvent(ownerFor(ctx), taskTypeFor(ctx), elapsedTimeFor(ctx)));
        ctx.setState(new CompletedState());
    }

    @Override
    public String toString() {
        return NOT_STARTED;
    }

    private Long ownerFor(Schedule ctx) {
        return ctx.getOwnerId();
    }

    private Duration elapsedTimeFor(Schedule ctx) {
        return ctx.getHistory().getElapsedTime();
    }

    private TaskType taskTypeFor(Schedule ctx) {
        return ctx.getTemporalConstraint().getTaskType();
    }
}
