package ch.bfh.tracesentry.daemon.exception;

import ch.bfh.tracesentry.lib.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TraceSentryException.class)
    public ResponseEntity<ErrorResponse> handleException(TraceSentryException e) {
        return new ResponseEntity<>(e.getErrorResponse(), Objects.requireNonNull(HttpStatus.resolve(e.getErrorResponse().getStatus())));
    }

}
