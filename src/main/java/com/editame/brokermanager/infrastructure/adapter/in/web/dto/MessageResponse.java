package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

/**
 * DTO de respuesta para información de mensaje
 */
@Value
@Builder
@Schema(description = "Información de un mensaje ActiveMQ")
public class MessageResponse {
    
    @Schema(description = "ID único del mensaje", example = "ID:localhost-12345-1234567890-1:1")
    String id;
    
    @Schema(description = "ID de correlación", example = "corr-12345")
    String correlationId;
    
    @Schema(description = "Destino del mensaje", example = "test.queue")
    String destination;
    
    @Schema(description = "Cuerpo del mensaje", example = "{\"orderId\": 12345, \"amount\": 99.99}")
    String body;
    
    @Schema(description = "Tipo de mensaje", example = "TEXT")
    String type;
    
    @Schema(description = "Headers del mensaje")
    Map<String, Object> headers;
    
    @Schema(description = "Propiedades del mensaje")
    Map<String, Object> properties;
    
    @Schema(description = "Prioridad del mensaje (0-9)", example = "4")
    int priority;
    
    @Schema(description = "Tiempo de vida en milisegundos", example = "3600000")
    long timeToLive;
    
    @Schema(description = "Indica si el mensaje es persistente", example = "true")
    boolean persistent;
    
    @Schema(description = "Número de reenvíos", example = "0")
    int redeliveryCount;
    
    @Schema(description = "Máximo número de reenvíos", example = "6")
    int maxRedeliveries;
    
    @Schema(description = "Timestamp del mensaje")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp;
    
    @Schema(description = "Timestamp de expiración")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant expiration;
    
    @Schema(description = "Delay para reenvío")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant redeliveryDelay;
    
    @Schema(description = "Estado del mensaje", example = "PENDING")
    String status;
    
    @Schema(description = "Indica si el mensaje ha expirado", example = "false")
    boolean expired;
    
    @Schema(description = "Indica si es de alta prioridad", example = "false")
    boolean highPriority;
    
    @Schema(description = "Indica si ha alcanzado el límite de reenvíos", example = "false")
    boolean hasRedeliveryLimit;
    
    @Schema(description = "Edad del mensaje en milisegundos", example = "120000")
    long age;
}
