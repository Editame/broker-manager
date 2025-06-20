package com.editame.brokermanager.infrastructure.adapter.out.jmx;

import com.editame.brokermanager.application.port.out.ConnectionRepository;
import com.editame.brokermanager.domain.exception.BrokerOperationException;
import com.editame.brokermanager.domain.model.BrokerConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de conexiones JMX para ActiveMQ
 * Ahora usa el sistema de conexiones múltiples
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JmxConnectionManager {
    
    private final ConnectionRepository connectionRepository;
    
    // Fallback values si no hay conexión activa configurada
    @Value("${activemq.jmx.url:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi}")
    private String fallbackJmxUrl;
    
    @Value("${activemq.jmx.username:}")
    private String fallbackUsername;
    
    @Value("${activemq.jmx.password:}")
    private String fallbackPassword;
    
    @Value("${activemq.jmx.connection.timeout:5000}")
    private int connectionTimeout;
    
    /**
     * Obtiene una nueva conexión JMX usando la conexión activa
     */
    public JMXConnector getConnection() {
        try {
            // Obtener la conexión activa
            List<BrokerConnection> activeConnections = connectionRepository.findActive();
            
            String jmxUrl;
            String username;
            String password;
            
            if (!activeConnections.isEmpty()) {
                // Usar la conexión activa
                BrokerConnection activeConnection = activeConnections.get(0);
                jmxUrl = activeConnection.getJmxUrl();
                username = activeConnection.getUsername();
                password = activeConnection.getPassword();
                log.debug("Usando conexión activa: {} ({})", activeConnection.getName(), jmxUrl);
            } else {
                // Usar valores de fallback
                jmxUrl = fallbackJmxUrl;
                username = fallbackUsername;
                password = fallbackPassword;
                log.debug("Usando conexión de fallback: {}", jmxUrl);
            }
            
            log.debug("Estableciendo conexión JMX a: {}", jmxUrl);
            
            JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
            Map<String, Object> environment = new HashMap<>();
            
            // Configurar timeout
            environment.put("jmx.remote.x.request.waiting.timeout", (long) connectionTimeout);
            environment.put("jmx.remote.x.notification.fetch.timeout", (long) connectionTimeout);
            
            // Configurar autenticación si está disponible
            if (username != null && !username.trim().isEmpty()) {
                String[] credentials = {username, password};
                environment.put(JMXConnector.CREDENTIALS, credentials);
                log.debug("Usando autenticación JMX para usuario: {}", username);
            }
            
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, environment);
            log.debug("Conexión JMX establecida exitosamente");
            
            return connector;
            
        } catch (IOException e) {
            log.error("Error al establecer conexión JMX: {}", e.getMessage());
            throw new BrokerOperationException("No se pudo conectar al broker via JMX: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica si la conexión JMX está disponible
     */
    public boolean isConnectionAvailable() {
        try (JMXConnector connector = getConnection()) {
            connector.getMBeanServerConnection().getMBeanCount();
            return true;
        } catch (Exception e) {
            log.warn("Conexión JMX no disponible: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene información de la conexión activa
     */
    public String getActiveConnectionInfo() {
        List<BrokerConnection> activeConnections = connectionRepository.findActive();
        if (!activeConnections.isEmpty()) {
            BrokerConnection active = activeConnections.get(0);
            return String.format("%s (%s:%d)", active.getName(), active.getHost(), active.getPort());
        }
        return "Fallback (localhost:1099)";
    }
}
