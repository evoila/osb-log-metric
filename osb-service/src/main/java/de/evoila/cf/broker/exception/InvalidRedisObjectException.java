package de.evoila.cf.broker.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class InvalidRedisObjectException extends RuntimeException {

    private String message;
    private long timestamp;

    public InvalidRedisObjectException() {
        this("InvalidRedisObjectException: The LogMetric-Redis Object found in the database has invalid properties (null values).", new Date().getTime());
    }

    public InvalidRedisObjectException(String message, long timestamp) {
        super(message);
        this.message = message;
        this.timestamp = timestamp;
    }

    public InvalidRedisObjectException(String message, Throwable cause, long timestamp) {
        super(message, cause);
        this.message = message;
        this.timestamp = timestamp;
    }

    public InvalidRedisObjectException(Throwable cause, HttpStatus status, String message, long timestamp) {
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