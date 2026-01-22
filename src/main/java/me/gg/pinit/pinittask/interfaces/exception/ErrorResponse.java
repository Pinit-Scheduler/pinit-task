package me.gg.pinit.pinittask.interfaces.exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

public record ErrorResponse(OffsetDateTime timestamp,
                            int status,
                            String error,
                            String message,
                            String path,
                            List<ValidationError> errors) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, message, path, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<ValidationError> errors) {
        return new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status,
                error,
                message,
                path,
                Objects.requireNonNullElseGet(errors, List::of)
        );
    }

    public record ValidationError(String field, Object rejectedValue, String reason) {
    }
}
