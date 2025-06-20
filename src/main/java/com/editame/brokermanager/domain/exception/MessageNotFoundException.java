package com.editame.brokermanager.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un mensaje
 */
public class MessageNotFoundException extends BrokerDomainException {
    
    public MessageNotFoundException(String message) {
        super(message);
    }
    
    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
