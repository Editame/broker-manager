package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

/**
 * DTO de respuesta para operación de purga
 */
@Builder
@Schema(description = "Respuesta de confirmación de purga de cola")
public record PurgeResponse(
    
    @Schema(description = "Nombre de la cola purgada", example = "test.queue")
    String queueName,
    
    @Schema(description = "Mensaje de confirmación", example = "Cola purgada exitosamente")
    String message,
    
    @Schema(description = "Número de mensajes eliminados", example = "25")
    long purgedCount,
    
    @Schema(description = "Timestamp de la operación")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp
    
) {}
