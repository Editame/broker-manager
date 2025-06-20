package com.editame.brokermanager.infrastructure.adapter.out.jmx;

import com.editame.brokermanager.domain.exception.BrokerOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de conexiones JMX para ActiveMQ
 */
@Component
@Slf4j
public class JmxConnectionManager {
    
    @Value("${activemq.jmx.url:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi}")
    private String jmxUrl;
    
    @Value("${activemq.jmx.username:}")
    private String username;
    
    @Value("${activemq.jmx.password:}")
    private String password;
    
    @Value("${activemq.jmx.connection.timeout:5000}")
    private int connectionTimeout;
    
    /**
     * Obtiene una nueva conexión JMX
     */
    public JMXConnector getConnection() {
        try {
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
}
