package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;

import java.time.Duration;

public class CompletedState implements ScheduleState{
    public static final String COMPLETED = "COMPLETED";
    @Override
    public void start(Schedule ctx) {
        throw new IllegalTransitionException("완료된 일정을 시작할 수 없습니다.");
    }

    @Override
    public void suspend(Schedule ctx) {
        throw new IllegalTransitionException("완료된 일정을 일시정지할 수 없습니다.");
    }

    @Override
    public void cancel(Schedule ctx) {
        DomainEvents.raise(new ScheduleCanceledEvent(ownerFor(ctx), taskTypeFor(ctx), elapsedTimeFor(ctx)));
        ctx.setState(new NotStartedState());
    }

    @Override
    public void finish(Schedule ctx) {
        throw new IllegalTransitionException("완료된 일정을 다시 완료할 수 없습니다.");
    }

    @Override
    public String toString() {
        return COMPLETED;
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
