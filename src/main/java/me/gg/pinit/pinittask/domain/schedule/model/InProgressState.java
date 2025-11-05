package me.gg.pinit.pinittask.domain.schedule.model;

public class InProgressState implements ScheduleState{
    public static final String IN_PROGRESS = "IN_PROGRESS";

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
        return IN_PROGRESS;
    }
}
