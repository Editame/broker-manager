package com.editame.brokermanager.domain.exception;

public class ActiveMqException extends RuntimeException {
    public ActiveMqException(String message) {
        super(message);
    }
    public ActiveMqException(String message, Throwable cause) {
        super(message, cause);
    }
}
