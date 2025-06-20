package com.editame.brokermanager.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * Resultado de una operación de reencolamiento
 */
@Value
@Builder
public class RequeueResult {
    
    /**
     * Cola origen
     */
    String sourceQueue;
    
    /**
     * Cola destino
     */
    String targetQueue;
    
    /**
     * Total de mensajes solicitados para reencolar
     */
    int totalRequested;
    
    /**
     * Mensajes reencolados exitosamente
     */
    int successfullyRequeued;
    
    /**
     * Mensajes que fallaron al reencolar
     */
    int failed;
    
    /**
     * IDs de mensajes que fallaron
     */
    List<String> failedMessageIds;
    
    /**
     * Tiempo que tomó la operación
     */
    long durationMillis;
    
    /**
     * Timestamp de la operación
     */
    Instant timestamp;
    
    /**
     * Mensaje descriptivo del resultado
     */
    String message;
    
    /**
     * Si la operación fue exitosa
     */
    boolean success;
    
    /**
     * Detalles adicionales o errores
     */
    String details;
}
