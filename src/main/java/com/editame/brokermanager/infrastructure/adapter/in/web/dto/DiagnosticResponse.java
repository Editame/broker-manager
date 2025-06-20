package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Respuesta de diagnóstico de conexión
 */
@Builder
public record DiagnosticResponse(
    String host,
    int port,
    boolean overallSuccess,
    String overallMessage,
    List<DiagnosticStep> steps,
    Instant timestamp,
    long totalTimeMs
) {
    
    @Builder
    public record DiagnosticStep(
        String name,
        String description,
        boolean success,
        String message,
        String details,
        long timeMs
    ) {}
}
