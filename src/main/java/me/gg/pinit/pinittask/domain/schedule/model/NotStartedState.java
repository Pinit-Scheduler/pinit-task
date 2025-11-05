package me.gg.pinit.pinittask.domain.schedule.model;

public class NotStartedState implements ScheduleState{
    public static final String NOT_STARTED = "NOT_STARTED";
    @Override
    public void start(Schedule ctx) {

    }

    @Override
    public void suspend(Schedule ctx) {

    }

    @Override
    public void cancel(Schedule ctx) {

    }

    @Override
    public void finish(Schedule ctx) {

    }
    @Override
    public String toString() {
        return NOT_STARTED;
    }
}
