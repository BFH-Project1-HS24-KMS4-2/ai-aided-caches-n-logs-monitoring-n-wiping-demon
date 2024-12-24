package ch.bfh.tracesentry.daemon.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends TraceSentryException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
