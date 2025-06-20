package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

/**
 * DTO de respuesta para mensaje enviado
 */
@Builder
@Schema(description = "Respuesta de confirmación de mensaje enviado")
public record MessageSentResponse(
    
    @Schema(description = "Nombre de la cola de destino", example = "test.queue")
    String queueName,
    
    @Schema(description = "Mensaje de confirmación", example = "Mensaje enviado exitosamente")
    String message,
    
    @Schema(description = "Timestamp del envío")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp
    
) {}
