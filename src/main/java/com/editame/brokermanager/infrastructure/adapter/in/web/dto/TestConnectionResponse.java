package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import lombok.Builder;

import java.time.Instant;

/**
 * Respuesta de prueba de conexión
 */
@Builder
public record TestConnectionResponse(
    String status,
    boolean success,
    String message,
    Instant timestamp
) {}
