package com.editame.brokermanager.infrastructure.adapter.out.memory;

import com.editame.brokermanager.application.port.out.ConnectionRepository;
import com.editame.brokermanager.domain.model.BrokerConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del repositorio de conexiones
 * Para producción se debería usar una base de datos
 */
@Repository
@Slf4j
public class InMemoryConnectionRepository implements ConnectionRepository {
    
    private final Map<String, BrokerConnection> connections = new ConcurrentHashMap<>();
    
    public InMemoryConnectionRepository() {
        // Repositorio inicializado vacío - Las conexiones se gestionan dinámicamente
        log.info("Repositorio de conexiones inicializado - Listo para gestión dinámica");
    }
    
    @Override
    public BrokerConnection save(BrokerConnection connection) {
        connections.put(connection.getId(), connection);
        log.debug("Conexión guardada: {} ({})", connection.getName(), connection.getId());
        return connection;
    }
    
    @Override
    public Optional<BrokerConnection> findById(String id) {
        return Optional.ofNullable(connections.get(id));
    }
    
    @Override
    public List<BrokerConnection> findAll() {
        return connections.values().stream()
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .toList();
    }
    
    @Override
    public List<BrokerConnection> findActive() {
        return connections.values().stream()
            .filter(BrokerConnection::isActive)
            .toList();
    }
    
    @Override
    public List<BrokerConnection> findByEnvironment(String environment) {
        return connections.values().stream()
            .filter(conn -> environment.equals(conn.getEnvironment()))
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .toList();
    }
    
    @Override
    public void deleteById(String id) {
        BrokerConnection removed = connections.remove(id);
        if (removed != null) {
            log.debug("Conexión eliminada: {} ({})", removed.getName(), id);
        }
    }
    
    @Override
    public boolean existsByName(String name) {
        return connections.values().stream()
            .anyMatch(conn -> name.equals(conn.getName()));
    }
}
