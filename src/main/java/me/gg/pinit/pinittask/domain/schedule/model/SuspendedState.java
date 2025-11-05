package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;

import java.time.ZonedDateTime;

public class SuspendedState implements ScheduleState {
    public static final String SUSPENDED = "SUSPENDED";

    @Override
    public void start(Schedule ctx, ZonedDateTime startTime) {

    }

    @Override
    public void suspend(Schedule ctx, ZonedDateTime suspendTime) {
        throw new IllegalTransitionException("일시정지된 일정을 다시 일시정지할 수 없습니다.");
    }

    @Override
    public void cancel(Schedule ctx) {

    }

    @Override
    public void finish(Schedule ctx, ZonedDateTime finishTime) {
        throw new IllegalTransitionException("일시정지된 일정을 완료할 수 없습니다.");
    }

    @Override
    public String toString() {
        return SUSPENDED;
    }
}
