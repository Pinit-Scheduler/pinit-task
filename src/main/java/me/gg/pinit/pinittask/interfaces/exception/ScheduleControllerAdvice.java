package me.gg.pinit.pinittask.interfaces.exception;

import lombok.extern.slf4j.Slf4j;
import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleAlreadyRemovedException;
import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;
import me.gg.pinit.pinittask.domain.member.exception.DuplicatedScheduleRunningException;
import me.gg.pinit.pinittask.domain.member.exception.MemberNotFoundException;
import me.gg.pinit.pinittask.domain.member.exception.ObjectiveNotNullException;
import me.gg.pinit.pinittask.domain.member.exception.ObjectiveNotPositiveException;
import me.gg.pinit.pinittask.domain.schedule.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ScheduleControllerAdvice {
    @ExceptionHandler({
            IllegalTitleException.class,
            IllegalDescriptionException.class,
            IllegalChangeException.class,
            IllegalTransitionException.class,
            TimeOrderReversedException.class,
            StartNotRecordedException.class,
            ObjectiveNotPositiveException.class,
            ObjectiveNotNullException.class,
            DuplicatedScheduleRunningException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.warn("Client error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({MemberNotFoundException.class, ScheduleNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ScheduleAlreadyRemovedException.class)
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, WebRequest request) {
        log.warn("Conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = Objects.requireNonNullElse(ex.getBindingResult().getFieldError(), ex.getBindingResult().getGlobalError())
                .getDefaultMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, request.getDescription(false));
        return ResponseEntity.status(status).body(body);
    }
}
