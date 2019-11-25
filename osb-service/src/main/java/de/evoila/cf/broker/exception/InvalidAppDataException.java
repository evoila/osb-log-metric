package de.evoila.cf.broker.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class InvalidAppDataException extends RuntimeException {

    private String message;
    private long timestamp;

    public InvalidAppDataException() {
        this("InvalidAppDataException: Found invalid App Data properties (null values).", new Date().getTime());
    }

    public InvalidAppDataException(String message, long timestamp) {
        super(message);
        this.message = message;
        this.timestamp = timestamp;
    }

    public InvalidAppDataException(String message, Throwable cause, long timestamp) {
        super(message, cause);
        this.message = message;
        this.timestamp = timestamp;
    }

    public InvalidAppDataException(Throwable cause, HttpStatus status, String message, long timestamp) {
        super(cause);
        this.message = message;
        this.timestamp = timestamp;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}