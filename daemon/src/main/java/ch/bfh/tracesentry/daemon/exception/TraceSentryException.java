package ch.bfh.tracesentry.daemon.exception;

import ch.bfh.tracesentry.lib.exception.ErrorResponse;
import org.springframework.http.HttpStatus;

public class TraceSentryException extends RuntimeException {
    private final HttpStatus status;

    public TraceSentryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.getMessage(), this.status.value());
    }

}
