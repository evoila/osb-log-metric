package de.evoila.cf.broker.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class DashboardBackendRequestException extends RuntimeException {

    private HttpStatus statusCode;
    private String message;
    private long timestamp;

    public DashboardBackendRequestException(String message, HttpStatus statusCode, long timestamp) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public DashboardBackendRequestException(String message, Throwable cause, HttpStatus statusCode, long timestamp) {
        super(message, cause);
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public DashboardBackendRequestException(Throwable cause, HttpStatus statusCode, String message, long timestamp) {
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
