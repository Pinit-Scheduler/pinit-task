package me.gg.pinit.pinittask.domain.schedule.model;

import java.time.ZonedDateTime;

public class NotStartedState implements ScheduleState{
    public static final String NOT_STARTED = "NOT_STARTED";
    @Override
    public void start(Schedule ctx, ZonedDateTime startTime) {

    }

    @Override
    public void suspend(Schedule ctx, ZonedDateTime suspendTime) {

    }

    @Override
    public void cancel(Schedule ctx) {

    }

    @Override
    public void finish(Schedule ctx, ZonedDateTime finishTime) {

    }
    @Override
    public String toString() {
        return NOT_STARTED;
    }
}
