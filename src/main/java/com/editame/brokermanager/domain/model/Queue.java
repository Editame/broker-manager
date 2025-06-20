package com.editame.brokermanager.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.List;

/**
 * Modelo de dominio para una cola ActiveMQ
 */
@Value
@Builder
@With
public class Queue {
    String name;
    QueueType type;
    QueueStatus status;
    
    // Métricas de mensajes
    long queueSize;
    long enqueueCount;
    long dequeueCount;
    long inflightCount;
    long expiredCount;
    
    // Métricas de consumidores
    int consumerCount;
    int producerCount;
    
    // Configuración
    long memoryLimit;
    int memoryPercentUsage;
    boolean paused;
    
    // Metadatos
    Instant createdAt;
    Instant lastMessageTime;
    List<MessageGroup> messageGroups;
    
    public enum QueueType {
        QUEUE, TOPIC, TEMP_QUEUE, TEMP_TOPIC
    }
    
    public enum QueueStatus {
        ACTIVE, PAUSED, FULL, ERROR
    }
    
    @Value
    @Builder
    public static class MessageGroup {
        String id;
        String name;
        int messageCount;
    }
    
    public boolean isEmpty() {
        return queueSize == 0;
    }
    
    public boolean hasConsumers() {
        return consumerCount > 0;
    }
    
    public boolean isHighLoad() {
        return queueSize > 100 || memoryPercentUsage > 80;
    }
    
    public double getProcessingRate() {
        return enqueueCount > 0 ? (double) dequeueCount / enqueueCount : 0;
    }
}
