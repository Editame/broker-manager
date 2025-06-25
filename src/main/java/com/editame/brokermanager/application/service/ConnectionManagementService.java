package com.editame.brokermanager.application.service;

import com.editame.brokermanager.application.port.in.ConnectionManagementUseCase;
import com.editame.brokermanager.application.port.out.ConnectionRepository;
import com.editame.brokermanager.domain.exception.BrokerOperationException;
import com.editame.brokermanager.domain.model.BrokerConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestión de conexiones a brokers
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConnectionManagementService implements ConnectionManagementUseCase {
    
    private final ConnectionRepository connectionRepository;
    
    @Override
    public BrokerConnection createConnection(CreateConnectionCommand command) {
        log.info("Creando nueva conexión: {}", command.name());
        
        // Validar que no exista una conexión con el mismo nombre
        if (connectionRepository.existsByName(command.name())) {
            throw new BrokerOperationException("Ya existe una conexión con el nombre: " + command.name());
        }
        
        // Crear la conexión
        BrokerConnection connection = BrokerConnection.builder()
            .id(UUID.randomUUID().toString())
            .name(command.name())
            .host(command.host())
            .port(command.port())
            .username(command.username())
            .password(command.password())
            .environment(command.environment())
            .description(command.description())
            .active(false)
            .lastTestStatus(BrokerConnection.ConnectionStatus.UNKNOWN)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        
        // Guardar y probar la conexión
        BrokerConnection saved = connectionRepository.save(connection);
        log.info("Conexión creada exitosamente: {}", saved.getId());
        
        // Probar la conexión automáticamente
        return testConnection(saved.getId());
    }
    
    @Override
    public BrokerConnection updateConnection(String connectionId, UpdateConnectionCommand command) {
        log.info("Actualizando conexión: {}", connectionId);
        
        BrokerConnection existing = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new BrokerOperationException("Conexión no encontrada: " + connectionId));
        
        // Verificar nombre único si cambió
        if (!existing.getName().equals(command.name()) && 
            connectionRepository.existsByName(command.name())) {
            throw new BrokerOperationException("Ya existe una conexión con el nombre: " + command.name());
        }
        
        // Actualizar campos
        BrokerConnection updated = existing
            .withName(command.name())
            .withHost(command.host())
            .withPort(command.port())
            .withUsername(command.username())
            .withPassword(command.password())
            .withEnvironment(command.environment())
            .withDescription(command.description())
            .withActive(command.active())
            .withUpdatedAt(Instant.now());
        
        // Si se activó esta conexión, desactivar las demás
        if (command.active() && !existing.isActive()) {
            deactivateAllConnections();
        }
        
        BrokerConnection saved = connectionRepository.save(updated);
        log.info("Conexión actualizada exitosamente: {}", connectionId);
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BrokerConnection> getAllConnections() {
        return connectionRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BrokerConnection> getConnection(String connectionId) {
        return connectionRepository.findById(connectionId);
    }
    
    @Override
    public void deleteConnection(String connectionId) {
        log.info("Eliminando conexión: {}", connectionId);
        
        BrokerConnection connection = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new BrokerOperationException("Conexión no encontrada: " + connectionId));
        
        if (connection.isActive()) {
            throw new BrokerOperationException("No se puede eliminar la conexión activa. Active otra conexión primero.");
        }
        
        connectionRepository.deleteById(connectionId);
        log.info("Conexión eliminada exitosamente: {}", connectionId);
    }
    
    @Override
    public BrokerConnection testConnection(String connectionId) {
        log.info("Probando conexión: {}", connectionId);
        
        BrokerConnection connection = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new BrokerOperationException("Conexión no encontrada: " + connectionId));
        
        BrokerConnection.ConnectionStatus status = testConnectionInternal(
            connection.getHost(),
            connection.getPort(),
            connection.getUsername(),
            connection.getPassword()
        );
        
        String message = switch (status) {
            case CONNECTED -> "Conexión exitosa";
            case ERROR -> "Error de conexión";
            case DISCONNECTED -> "No se pudo conectar";
            default -> "Estado desconocido";
        };
        
        BrokerConnection updated = connection.withTestResult(status, message);
        BrokerConnection saved = connectionRepository.save(updated);
        
        log.info("Prueba de conexión completada: {} - {}", connectionId, status);
        return saved;
    }
    
    @Override
    public BrokerConnection.ConnectionStatus testConnectionConfig(TestConnectionCommand command) {
        log.info("Probando configuración de conexión: {}:{}", command.host(), command.port());
        
        return testConnectionInternal(
            command.host(),
            command.port(),
            command.username(),
            command.password()
        );
    }
    
    @Override
    public BrokerConnection activateConnection(String connectionId) {
        log.info("Activando conexión: {}", connectionId);
        
        BrokerConnection connection = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new BrokerOperationException("Conexión no encontrada: " + connectionId));
        
        // Desactivar todas las conexiones
        deactivateAllConnections();
        
        // Activar la conexión seleccionada
        BrokerConnection activated = connection
            .withActive(true)
            .withUpdatedAt(Instant.now());
        
        BrokerConnection saved = connectionRepository.save(activated);
        log.info("Conexión activada exitosamente: {}", connectionId);
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BrokerConnection> getActiveConnection() {
        List<BrokerConnection> activeConnections = connectionRepository.findActive();
        return activeConnections.isEmpty() ? Optional.empty() : Optional.of(activeConnections.get(0));
    }
    
    /**
     * Prueba una conexión internamente
     */
    private BrokerConnection.ConnectionStatus testConnectionInternal(String host, int port, String username, String password) {
        try {
            String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port);
            log.info("Probando conexión JMX a URL: {}", jmxUrl);
            JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
            
            Map<String, Object> environment = new HashMap<>();
            if (username != null && !username.trim().isEmpty()) {
                log.info("Usando autenticación para usuario: {}", username);
                environment.put(JMXConnector.CREDENTIALS, new String[]{username, password});
            }
            
            try (JMXConnector connector = JMXConnectorFactory.connect(serviceURL, environment)) {
                // Intentar obtener una conexión al MBeanServer
                connector.getMBeanServerConnection();
                log.info("Conexión JMX exitosa a: {}", jmxUrl);
                return BrokerConnection.ConnectionStatus.CONNECTED;
            }
            
        } catch (Exception e) {
            log.error("Error al probar conexión {}:{} - Tipo: {} - Mensaje: {}", 
                     host, port, e.getClass().getName(), e.getMessage());
            e.printStackTrace(); // Imprimir stack trace completo para diagnóstico
            return BrokerConnection.ConnectionStatus.ERROR;
        }
    }
    
    /**
     * Desactiva todas las conexiones
     */
    private void deactivateAllConnections() {
        List<BrokerConnection> activeConnections = connectionRepository.findActive();
        for (BrokerConnection conn : activeConnections) {
            BrokerConnection deactivated = conn
                .withActive(false)
                .withUpdatedAt(Instant.now());
            connectionRepository.save(deactivated);
        }
    }
}
