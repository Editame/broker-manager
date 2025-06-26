package com.editame.brokermanager.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;

/**
 * Representa una conexión a un broker ActiveMQ
 */
@Value
@Builder
@With
public class BrokerConnection {
    
    /**
     * ID único de la conexión
     */
    String id;
    
    /**
     * Nombre descriptivo de la conexión
     */
    String name;
    
    /**
     * Host del broker
     */
    String host;
    
    /**
     * Puerto JMX del broker
     */
    int port;
    
    /**
     * Usuario para autenticación (opcional)
     */
    String username;
    
    /**
     * Contraseña para autenticación (opcional)
     */
    String password;
    
    /**
     * Entorno (dev, staging, prod, etc.)
     */
    String environment;
    
    /**
     * Descripción adicional
     */
    String description;
    
    /**
     * Si la conexión está activa
     */
    boolean active;
    
    /**
     * ID de la conexión JMX actual (cuando está activa)
     */
    String currentJmxConnectionId;
    
    /**
     * Última vez que se usó la conexión (para TTL)
     */
    Instant lastUsed;
    
    /**
     * Última vez que se probó la conexión
     */
    Instant lastTested;
    
    /**
     * Estado de la última prueba
     */
    ConnectionStatus lastTestStatus;
    
    /**
     * Mensaje del último test
     */
    String lastTestMessage;
    
    /**
     * Cuándo se creó la conexión
     */
    Instant createdAt;
    
    /**
     * Cuándo se actualizó por última vez
     */
    Instant updatedAt;
    
    /**
     * Estados posibles de conexión
     */
    public enum ConnectionStatus {
        CONNECTED,
        DISCONNECTED,
        ERROR,
        TESTING,
        UNKNOWN
    }
    
    /**
     * Obtiene la URL JMX completa
     */
    public String getJmxUrl() {
        return String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port);
    }
    
    /**
     * Verifica si requiere autenticación
     */
    public boolean requiresAuth() {
        return username != null && !username.trim().isEmpty();
    }
    
    /**
     * Crea una copia con estado actualizado
     */
    public BrokerConnection withTestResult(ConnectionStatus status, String message) {
        return this.withLastTested(Instant.now())
                  .withLastTestStatus(status)
                  .withLastTestMessage(message)
                  .withUpdatedAt(Instant.now());
    }
}
