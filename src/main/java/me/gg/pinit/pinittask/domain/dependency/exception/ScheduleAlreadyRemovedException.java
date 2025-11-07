package me.gg.pinit.pinittask.domain.dependency.exception;

public class ScheduleAlreadyRemovedException extends RuntimeException {
    public ScheduleAlreadyRemovedException(String message) {
        super(message);
    }
}
