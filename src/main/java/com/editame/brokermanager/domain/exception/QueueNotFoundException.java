package com.editame.brokermanager.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una cola
 */
public class QueueNotFoundException extends BrokerDomainException {
    
    public QueueNotFoundException(String message) {
        super(message);
    }
    
    public QueueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
