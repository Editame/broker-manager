package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import lombok.Builder;

import java.time.Instant;

/**
 * Respuesta con información de conexión
 */
@Builder
public record ConnectionResponse(
    String id,
    String name,
    String host,
    int port,
    String username,
    String environment,
    String description,
    boolean active,
    String jmxUrl,
    boolean requiresAuth,
    Instant lastTested,
    String lastTestStatus,
    String lastTestMessage,
    Instant createdAt,
    Instant updatedAt
) {}
