package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleStartedEvent;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;

import java.time.ZonedDateTime;

public class SuspendedState implements ScheduleState {
    public static final String SUSPENDED = "SUSPENDED";

    private static Long idFor(Schedule ctx) {
        return ctx.getId();
    }

    @Override
    public void suspend(Schedule ctx, ZonedDateTime suspendTime) {
        throw new IllegalTransitionException("일시정지된 일정을 다시 일시정지할 수 없습니다.");
    }

    @Override
    public void start(Schedule ctx, ZonedDateTime startTime) {
        DomainEvents.raise(new ScheduleStartedEvent(idFor(ctx), ownerFor(ctx), this.toString()));
        ScheduleHistory history = ctx.getHistory();
        ctx.updateHistoryTo(history.recordStart(startTime));
        ctx.setState(new InProgressState());
    }

    @Override
    public void finish(Schedule ctx, ZonedDateTime finishTime) {
        throw new IllegalTransitionException("일시정지된 일정을 즉시 완료할 수 없습니다.");
    }

    @Override
    public String toString() {
        return SUSPENDED;
    }

    private Long ownerFor(Schedule ctx) {
        return ctx.getOwnerId();
    }

    @Override
    public void cancel(Schedule ctx) {
        DomainEvents.raise(new ScheduleCanceledEvent(idFor(ctx), ownerFor(ctx), this.toString()));
        ScheduleHistory history = ctx.getHistory();
        ctx.updateHistoryTo(history.rollback());
        ctx.setState(new NotStartedState());
    }
}
