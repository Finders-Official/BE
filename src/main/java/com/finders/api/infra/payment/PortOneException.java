package com.finders.api.infra.payment;

public class PortOneException extends RuntimeException {

    public PortOneException(String message) {
        super(message);
    }

    public PortOneException(String message, Throwable cause) {
        super(message, cause);
    }
}
