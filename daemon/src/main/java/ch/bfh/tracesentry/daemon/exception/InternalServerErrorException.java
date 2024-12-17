package ch.bfh.tracesentry.daemon.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends TraceSentryException {
    public InternalServerErrorException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
