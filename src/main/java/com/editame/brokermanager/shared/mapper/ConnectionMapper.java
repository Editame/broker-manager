package com.editame.brokermanager.shared.mapper;

import com.editame.brokermanager.domain.model.BrokerConnection;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.ConnectionResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversiones de conexiones
 */
@Component
public class ConnectionMapper {
    
    /**
     * Convierte una conexión de dominio a respuesta DTO
     */
    public ConnectionResponse toResponse(BrokerConnection connection) {
        return ConnectionResponse.builder()
            .id(connection.getId())
            .name(connection.getName())
            .host(connection.getHost())
            .port(connection.getPort())
            .username(connection.getUsername())
            .environment(connection.getEnvironment())
            .description(connection.getDescription())
            .active(connection.isActive())
            .jmxUrl(connection.getJmxUrl())
            .requiresAuth(connection.requiresAuth())
            .lastTested(connection.getLastTested())
            .lastTestStatus(connection.getLastTestStatus() != null ? 
                connection.getLastTestStatus().name() : null)
            .lastTestMessage(connection.getLastTestMessage())
            .createdAt(connection.getCreatedAt())
            .updatedAt(connection.getUpdatedAt())
            .build();
    }
}
