package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Map;

/**
 * DTO de request para enviar mensaje
 */

@Builder
@Schema(description = "Request para enviar un mensaje a una cola")
public record SendMessageRequest(
    
    @NotBlank(message = "El cuerpo del mensaje es requerido")
    @Size(max = 1048576, message = "El cuerpo del mensaje no puede exceder 1MB")
    @Schema(description = "Cuerpo del mensaje", 
            example = "{\"orderId\": 12345, \"amount\": 99.99, \"status\": \"pending\"}")
    String body,
    
    @Schema(description = "Headers del mensaje", 
            example = "{\"Content-Type\": \"application/json\", \"Source\": \"web-app\"}")
    Map<String, Object> headers,
    
    @Schema(description = "Propiedades personalizadas del mensaje",
            example = "{\"customerId\": \"cust-001\", \"region\": \"US\"}")
    Map<String, Object> properties,
    
    @Min(value = 0, message = "La prioridad debe estar entre 0 y 9")
    @Max(value = 9, message = "La prioridad debe estar entre 0 y 9")
    @Schema(description = "Prioridad del mensaje (0-9, donde 9 es la más alta)", 
            example = "4", defaultValue = "4")
    int priority,
    
    @Min(value = 0, message = "El tiempo de vida no puede ser negativo")
    @Schema(description = "Tiempo de vida del mensaje en milisegundos (0 = sin expiración)", 
            example = "3600000", defaultValue = "0")
    long timeToLive,
    
    @Schema(description = "Indica si el mensaje debe ser persistente", 
            example = "true", defaultValue = "true")
    boolean persistent,

    String type
    
) {
    
    public SendMessageRequest {
        // Valores por defecto
        if (headers == null) {
            headers = Map.of();
        }
        if (properties == null) {
            properties = Map.of();
        }
        if (priority < 0 || priority > 9) {
            priority = 4; // Prioridad por defecto
        }
        if (timeToLive < 0) {
            timeToLive = 0; // Sin expiración por defecto
        }
    }
    
    /**
     * Constructor con valores por defecto
     */
    public static SendMessageRequest of(String body) {
        return SendMessageRequest.builder()
            .body(body)
            .headers(Map.of())
            .properties(Map.of())
            .priority(4)
            .timeToLive(0)
            .persistent(true)
            .build();
    }
}
