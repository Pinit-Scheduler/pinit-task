package me.gg.pinit.pinittask.domain.member.exception;

public class DuplicatedScheduleRunningException extends RuntimeException {
    Long scheduleId;

    public DuplicatedScheduleRunningException(String message) {
        super(message);
    }

    public DuplicatedScheduleRunningException(String message, Long scheduleId) {
        super(message);
        this.scheduleId = scheduleId;
    }
}
