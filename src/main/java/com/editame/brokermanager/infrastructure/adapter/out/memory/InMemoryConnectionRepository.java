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
        // Crear conexión por defecto al localhost
        BrokerConnection defaultConnection = BrokerConnection.builder()
            .id("default-localhost")
            .name("Localhost (Default)")
            .host("localhost")
            .port(1099)
            .username(null)
            .password(null)
            .environment("local")
            .description("Conexión por defecto al broker local")
            .active(true)
            .lastTestStatus(BrokerConnection.ConnectionStatus.UNKNOWN)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        
        connections.put(defaultConnection.getId(), defaultConnection);
        log.info("Repositorio inicializado con conexión por defecto: {}", defaultConnection.getName());
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
