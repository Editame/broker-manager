package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

/**
 * DTO de respuesta para operaciones de cola
 */
@Builder
@Schema(description = "Respuesta de confirmación de operación en cola")
public record QueueOperationResponse(
    
    @Schema(description = "Nombre de la cola", example = "test.queue")
    String queueName,
    
    @Schema(description = "Tipo de operación realizada", example = "PAUSE")
    String operation,
    
    @Schema(description = "Mensaje de confirmación", example = "Cola pausada exitosamente")
    String message,
    
    @Schema(description = "Timestamp de la operación")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp
    
) {}
