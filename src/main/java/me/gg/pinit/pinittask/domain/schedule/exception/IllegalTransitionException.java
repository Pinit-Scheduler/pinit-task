package me.gg.pinit.pinittask.domain.schedule.exception;

public class IllegalTransitionException extends RuntimeException {
    public IllegalTransitionException(String message) {
        super(message);
    }
}
