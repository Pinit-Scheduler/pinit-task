package me.gg.pinit.pinittask.interfaces.exception;

import lombok.extern.slf4j.Slf4j;
import me.gg.pinit.pinittask.domain.task.exception.TaskNotFoundException;
import me.gg.pinit.pinittask.interfaces.web.TaskControllerV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice(assignableTypes = TaskControllerV1.class)
public class TaskControllerAdvice {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TaskNotFoundException ex, WebRequest request) {
        log.warn("Task not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, WebRequest request) {
        log.warn("Invalid task request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException ex, WebRequest request) {
        log.warn("Task conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex, WebRequest request) {
        log.error("Unexpected task error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, request.getDescription(false));
        return ResponseEntity.status(status).body(body);
    }
}
