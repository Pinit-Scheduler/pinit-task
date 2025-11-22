package me.gg.pinit.pinittask.interfaces.exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ErrorResponse(OffsetDateTime timestamp,
                            int status,
                            String error,
                            String message,
                            String path) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC), status, error, message, path);
    }
}

