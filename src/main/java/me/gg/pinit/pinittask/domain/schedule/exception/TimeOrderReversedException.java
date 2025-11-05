package me.gg.pinit.pinittask.domain.schedule.exception;

public class TimeOrderReversedException extends RuntimeException {
    public TimeOrderReversedException(String message) {
        super(message);
    }
}
