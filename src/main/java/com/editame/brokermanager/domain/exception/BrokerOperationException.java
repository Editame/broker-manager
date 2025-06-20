package com.editame.brokermanager.domain.exception;

/**
 * Excepción para errores en operaciones del broker
 */
public class BrokerOperationException extends BrokerDomainException {
    
    public BrokerOperationException(String message) {
        super(message);
    }
    
    public BrokerOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
