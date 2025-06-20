package com.editame.brokermanager.application.port.in;

import com.editame.brokermanager.domain.model.BrokerConnection;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

/**
 * Casos de uso para gestión de conexiones
 */
public interface ConnectionManagementUseCase {
    
    /**
     * Crea una nueva conexión
     */
    BrokerConnection createConnection(CreateConnectionCommand command);
    
    /**
     * Actualiza una conexión existente
     */
    BrokerConnection updateConnection(String connectionId, UpdateConnectionCommand command);
    
    /**
     * Lista todas las conexiones
     */
    List<BrokerConnection> getAllConnections();
    
    /**
     * Obtiene una conexión por ID
     */
    Optional<BrokerConnection> getConnection(String connectionId);
    
    /**
     * Elimina una conexión
     */
    void deleteConnection(String connectionId);
    
    /**
     * Prueba una conexión
     */
    BrokerConnection testConnection(String connectionId);
    
    /**
     * Prueba una conexión sin guardarla
     */
    BrokerConnection.ConnectionStatus testConnectionConfig(TestConnectionCommand command);
    
    /**
     * Activa una conexión (desactiva las demás)
     */
    BrokerConnection activateConnection(String connectionId);
    
    /**
     * Obtiene la conexión activa actual
     */
    Optional<BrokerConnection> getActiveConnection();
    
    /**
     * Comando para crear conexión
     */
    @Builder
    record CreateConnectionCommand(
        String name,
        String host,
        int port,
        String username,
        String password,
        String environment,
        String description
    ) {}
    
    /**
     * Comando para actualizar conexión
     */
    @Builder
    record UpdateConnectionCommand(
        String name,
        String host,
        int port,
        String username,
        String password,
        String environment,
        String description,
        boolean active
    ) {}
    
    /**
     * Comando para probar conexión
     */
    @Builder
    record TestConnectionCommand(
        String host,
        int port,
        String username,
        String password
    ) {}
}
