package com.editame.brokermanager.infrastructure.adapter.out.jmx;

import com.editame.brokermanager.application.port.out.ConnectionRepository;
import com.editame.brokermanager.domain.exception.BrokerOperationException;
import com.editame.brokermanager.domain.model.BrokerConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de sesiones JMX con TTL automático
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JmxSessionManager {
    
    private final ConnectionRepository connectionRepository;
    
    // Mapa: nuestro UUID → JMXConnector activo
    private final Map<String, JMXConnector> activeSessions = new ConcurrentHashMap<>();
    
    // TTL en horas - 3 horas fijas
    private static final int SESSION_TTL_HOURS = 3;
    
    /**
     * Obtiene una conexión JMX para el connectionId especificado
     * Si no existe, la crea. Si existe, actualiza lastUsed.
     */
    public JMXConnector getConnection(String ourConnectionId) {
        log.debug("Solicitando conexión JMX para UUID: {}", ourConnectionId);
        
        // 1. Buscar el registro en BD para obtener el JMX Connection ID actual
        BrokerConnection connection = connectionRepository.findById(ourConnectionId)
            .orElseThrow(() -> new BrokerOperationException("Conexión no encontrada: " + ourConnectionId));
        
        // 2. Si ya tiene JMX Connection ID, verificar si la sesión sigue activa
        if (connection.getCurrentJmxConnectionId() != null) {
            JMXConnector existingConnector = activeSessions.get(ourConnectionId);
            if (existingConnector != null && isConnectionAlive(existingConnector)) {
                log.debug("Reutilizando conexión JMX existente - UUID: {} → JMX ID: {}", 
                         ourConnectionId, connection.getCurrentJmxConnectionId());
                updateLastUsed(ourConnectionId);
                return existingConnector;
            }
        }
        
        // 3. Crear nueva sesión JMX
        log.info("Creando nueva sesión JMX para UUID: {}", ourConnectionId);
        return createNewSession(ourConnectionId, connection);
    }
    
    /**
     * Crea una nueva sesión JMX
     */
    private JMXConnector createNewSession(String ourConnectionId, BrokerConnection connection) {
        try {
            // Configurar timeouts globales del sistema
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
            System.setProperty("sun.rmi.transport.connectionTimeout", "5000");
            
            // Crear JMXConnector usando la configuración de la conexión
            JMXServiceURL serviceURL = new JMXServiceURL(connection.getJmxUrl());
            Map<String, Object> environment = new HashMap<>();
            
            // Configurar autenticación si es necesario
            if (connection.getUsername() != null && !connection.getUsername().trim().isEmpty()) {
                String[] credentials = {connection.getUsername(), connection.getPassword()};
                environment.put(JMXConnector.CREDENTIALS, credentials);
            }
            
            // Configurar timeouts muy agresivos para fallar rápido (5 segundos)
            environment.put("jmx.remote.x.request.waiting.timeout", 5000L); // 5 segundos
            environment.put("jmx.remote.x.notification.fetch.timeout", 5000L); // 5 segundos
            environment.put("com.sun.jndi.rmi.factory.timeout", 5000L); // 5 segundos
            environment.put("sun.rmi.transport.tcp.responseTimeout", 5000L); // 5 segundos
            environment.put("sun.rmi.transport.connectionTimeout", 5000L); // 5 segundos
            
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, environment);
            String jmxConnectionId = connector.getConnectionId();
            
            log.info("✅ Nueva sesión JMX creada - UUID: {} → JMX ID: {}", ourConnectionId, jmxConnectionId);
            
            // Guardar en memoria
            activeSessions.put(ourConnectionId, connector);
            
            // Actualizar BD con el nuevo JMX Connection ID y estado activo
            updateConnectionInDatabase(ourConnectionId, jmxConnectionId, true);
            
            return connector;
            
        } catch (Exception e) {
            log.error("❌ Error creando sesión JMX para UUID {}: {}", ourConnectionId, e.getMessage());
            
            // Marcar como inactiva en BD
            updateConnectionInDatabase(ourConnectionId, null, false);
            
            throw new BrokerOperationException("No se pudo crear sesión JMX: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica si una conexión JMX está viva
     */
    private boolean isConnectionAlive(JMXConnector connector) {
        try {
            connector.getMBeanServerConnection().getMBeanCount();
            return true;
        } catch (Exception e) {
            log.debug("Conexión JMX no está viva: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza el lastUsed de una conexión usando nuestro UUID
     */
    private void updateLastUsed(String ourConnectionId) {
        try {
            BrokerConnection connection = connectionRepository.findById(ourConnectionId).orElse(null);
            if (connection != null) {
                BrokerConnection updated = connection.withLastUsed(Instant.now());
                connectionRepository.save(updated);
                log.debug("Actualizado lastUsed para UUID: {}", ourConnectionId);
            }
        } catch (Exception e) {
            log.warn("Error actualizando lastUsed para UUID {}: {}", ourConnectionId, e.getMessage());
        }
    }
    
    /**
     * Actualiza la conexión en la base de datos usando nuestro UUID
     */
    private void updateConnectionInDatabase(String ourConnectionId, String jmxConnectionId, boolean active) {
        try {
            BrokerConnection connection = connectionRepository.findById(ourConnectionId).orElse(null);
            if (connection != null) {
                BrokerConnection updated = connection
                    .withCurrentJmxConnectionId(jmxConnectionId)
                    .withActive(active)
                    .withLastUsed(Instant.now())
                    .withUpdatedAt(Instant.now());
                
                connectionRepository.save(updated);
                log.debug("Actualizada conexión en BD - UUID: {}, JMX ID: {}, Active: {}", 
                         ourConnectionId, jmxConnectionId, active);
            }
        } catch (Exception e) {
            log.error("Error actualizando conexión en BD UUID {}: {}", ourConnectionId, e.getMessage());
        }
    }
    
    /**
     * Limpieza automática de sesiones inactivas (cada hora)
     */
    @Scheduled(fixedRate = 3600000) // Cada hora (3600000 ms)
    public void cleanupInactiveSessions() {
        log.debug("Iniciando limpieza de sesiones inactivas...");
        
        Instant cutoff = Instant.now().minus(SESSION_TTL_HOURS, ChronoUnit.HOURS);
        
        // Buscar conexiones que han expirado
        connectionRepository.findActive().forEach(connection -> {
            if (connection.getLastUsed() != null && connection.getLastUsed().isBefore(cutoff)) {
                log.info("🕒 Sesión expirada por TTL (3 horas) - UUID: {}, Última uso: {}", 
                        connection.getId(), connection.getLastUsed());
                
                closeSession(connection.getId());
            }
        });
        
        log.debug("Limpieza de sesiones completada");
    }
    
    /**
     * Cierra una sesión específica usando nuestro UUID
     */
    public void closeSession(String ourConnectionId) {
        log.info("Cerrando sesión JMX para UUID: {}", ourConnectionId);
        
        // Cerrar conexión JMX
        JMXConnector connector = activeSessions.remove(ourConnectionId);
        if (connector != null) {
            try {
                connector.close();
                log.debug("✅ Conexión JMX cerrada para UUID: {}", ourConnectionId);
            } catch (IOException e) {
                log.warn("Error cerrando conexión JMX para UUID {}: {}", ourConnectionId, e.getMessage());
            }
        }
        
        // Actualizar BD como inactiva
        updateConnectionInDatabase(ourConnectionId, null, false);
    }
    
    /**
     * Cierra todas las sesiones (para shutdown)
     */
    public void closeAllSessions() {
        log.info("Cerrando todas las sesiones JMX...");
        
        activeSessions.keySet().forEach(this::closeSession);
        
        log.info("✅ Todas las sesiones JMX cerradas");
    }
}
