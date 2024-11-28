package ch.bfh.tracesentry.daemon.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends TraceSentryException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
