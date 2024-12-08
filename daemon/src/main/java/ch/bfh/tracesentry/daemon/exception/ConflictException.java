package ch.bfh.tracesentry.daemon.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends TraceSentryException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
