package com.editame.brokermanager.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

/**
 * Modelo de dominio para un mensaje ActiveMQ
 */
@Value
@Builder
public class Message {
    String id;
    String correlationId;
    String destination;
    
    // Contenido
    String body;
    MessageType type;
    Map<String, Object> headers;
    Map<String, Object> properties;
    
    // Metadatos de entrega
    int priority;
    long timeToLive;
    boolean persistent;
    int redeliveryCount;
    int maxRedeliveries;
    
    // Timestamps
    Instant timestamp;
    Instant expiration;
    Instant redeliveryDelay;
    
    // Estado
    MessageStatus status;
    
    public enum MessageType {
        TEXT, BYTES, MAP, OBJECT, STREAM
    }
    
    public enum MessageStatus {
        PENDING, DELIVERED, ACKNOWLEDGED, EXPIRED, DEAD_LETTER
    }
    
    public boolean isExpired() {
        return expiration != null && Instant.now().isAfter(expiration);
    }
    
    public boolean isHighPriority() {
        return priority >= 7;
    }
    
    public boolean hasRedeliveryLimit() {
        return redeliveryCount >= maxRedeliveries;
    }
    
    public long getAge() {
        return timestamp != null ? 
            java.time.Duration.between(timestamp, Instant.now()).toMillis() : 0;
    }
}
