package com.rms.exception;

/** A request that is well formed but breaks a business rule. Returned as 400 Bad Request. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
