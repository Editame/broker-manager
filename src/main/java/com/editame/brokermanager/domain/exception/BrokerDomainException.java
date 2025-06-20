package com.editame.brokermanager.domain.exception;

/**
 * Excepción base para el dominio del broker
 */
public abstract class BrokerDomainException extends RuntimeException {
    
    protected BrokerDomainException(String message) {
        super(message);
    }
    
    protected BrokerDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
