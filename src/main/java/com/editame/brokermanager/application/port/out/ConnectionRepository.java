package com.editame.brokermanager.application.port.out;

import com.editame.brokermanager.domain.model.BrokerConnection;

import java.util.List;
import java.util.Optional;

/**
 * Puerto para persistencia de conexiones
 */
public interface ConnectionRepository {
    
    /**
     * Guarda una conexión
     */
    BrokerConnection save(BrokerConnection connection);
    
    /**
     * Busca una conexión por ID
     */
    Optional<BrokerConnection> findById(String id);
    
    /**
     * Lista todas las conexiones
     */
    List<BrokerConnection> findAll();
    
    /**
     * Lista conexiones activas
     */
    List<BrokerConnection> findActive();
    
    /**
     * Busca conexiones por entorno
     */
    List<BrokerConnection> findByEnvironment(String environment);
    
    /**
     * Elimina una conexión
     */
    void deleteById(String id);
    
    /**
     * Verifica si existe una conexión con el nombre dado
     */
    boolean existsByName(String name);
}
