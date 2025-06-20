package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta para información de cola
 */
@Value
@Builder
@Schema(description = "Información de una cola ActiveMQ")
public class QueueResponse {
    
    @Schema(description = "Nombre de la cola", example = "test.queue")
    String name;
    
    @Schema(description = "Tipo de cola", example = "QUEUE")
    String type;
    
    @Schema(description = "Estado de la cola", example = "ACTIVE")
    String status;
    
    @Schema(description = "Número de mensajes en la cola", example = "25")
    long queueSize;
    
    @Schema(description = "Total de mensajes encolados", example = "150")
    long enqueueCount;
    
    @Schema(description = "Total de mensajes desencolados", example = "125")
    long dequeueCount;
    
    @Schema(description = "Mensajes en tránsito", example = "2")
    long inflightCount;
    
    @Schema(description = "Mensajes expirados", example = "0")
    long expiredCount;
    
    @Schema(description = "Número de consumidores", example = "2")
    int consumerCount;
    
    @Schema(description = "Número de productores", example = "1")
    int producerCount;
    
    @Schema(description = "Límite de memoria en bytes", example = "67108864")
    long memoryLimit;
    
    @Schema(description = "Porcentaje de memoria utilizada", example = "15")
    int memoryPercentUsage;
    
    @Schema(description = "Indica si la cola está pausada", example = "false")
    boolean paused;
    
    @Schema(description = "Fecha de creación de la cola")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant createdAt;
    
    @Schema(description = "Timestamp del último mensaje")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant lastMessageTime;
    
    @Schema(description = "Grupos de mensajes")
    List<MessageGroupResponse> messageGroups;
    
    @Schema(description = "Indica si la cola está vacía", example = "false")
    boolean empty;
    
    @Schema(description = "Indica si tiene consumidores", example = "true")
    boolean hasConsumers;
    
    @Schema(description = "Indica si tiene alta carga", example = "false")
    boolean highLoad;
    
    @Schema(description = "Tasa de procesamiento", example = "0.83")
    double processingRate;
    
    @Value
    @Builder
    @Schema(description = "Información de grupo de mensajes")
    public static class MessageGroupResponse {
        @Schema(description = "ID del grupo", example = "group-001")
        String id;
        
        @Schema(description = "Nombre del grupo", example = "Priority Messages")
        String name;
        
        @Schema(description = "Número de mensajes en el grupo", example = "5")
        int messageCount;
    }
}
