package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;

import java.time.ZonedDateTime;

public class InProgressState implements ScheduleState{
    public static final String IN_PROGRESS = "IN_PROGRESS";

    @Override
    public void start(Schedule ctx, ZonedDateTime startTime) {
        throw new IllegalTransitionException("진행 중인 일정을 다시 시작할 수 없습니다.");
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
        return IN_PROGRESS;
    }
}
