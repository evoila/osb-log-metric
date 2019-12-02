package de.evoila.cf.broker.exception;

import org.springframework.http.HttpStatus;

public class CouldNotRequestTokenException extends RuntimeException {

    private HttpStatus statusCode;
    private String message;
    private long timestamp;

    public CouldNotRequestTokenException(String message, HttpStatus statusCode, long timestamp) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public CouldNotRequestTokenException(String message, Throwable cause, HttpStatus statusCode, long timestamp) {
        super(message, cause);
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public CouldNotRequestTokenException(Throwable cause, HttpStatus statusCode, String message, long timestamp) {
        super(cause);
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
