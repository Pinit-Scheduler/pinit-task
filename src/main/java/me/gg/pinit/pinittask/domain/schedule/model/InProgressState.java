package me.gg.pinit.pinittask.domain.schedule.model;

import java.time.ZonedDateTime;

public class InProgressState implements ScheduleState{
    public static final String IN_PROGRESS = "IN_PROGRESS";

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
        return IN_PROGRESS;
    }
}
