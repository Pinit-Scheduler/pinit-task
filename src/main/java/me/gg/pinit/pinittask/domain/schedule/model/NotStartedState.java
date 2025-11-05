package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;

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
        ScheduleHistory history = ctx.getHistory();
        ctx.updateHistoryTo(history.recordStop(suspendTime));
        ctx.setState(new NotStartedState());
    }

    @Override
    public void cancel(Schedule ctx) {
        ScheduleHistory history = ctx.getHistory();
        ctx.updateHistoryTo(history.rollback());
        ctx.setState(new NotStartedState());
    }

    @Override
    public void finish(Schedule ctx, ZonedDateTime finishTime) {
        ScheduleHistory history = ctx.getHistory();
        ctx.updateHistoryTo(history.recordStop(finishTime));
        ctx.setState(new CompletedState());
    }

    @Override
    public String toString() {
        return NOT_STARTED;
    }
}
