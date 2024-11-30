package ch.bfh.tracesentry.daemon.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableException extends TraceSentryException {
    public UnprocessableException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
