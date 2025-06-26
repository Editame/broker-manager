package com.editame.brokermanager.infrastructure.adapter.out.jmx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.management.remote.JMXConnector;

/**
 * Gestor de conexiones JMX - Ahora delega al SessionManager
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JmxConnectionManager {
    
    private final JmxSessionManager sessionManager;
    
    /**
     * Obtiene una conexión JMX para el connectionId especificado
     * Delega al SessionManager que maneja TTL automáticamente
     */
    public JMXConnector getConnection(String connectionId) {
        log.debug("Delegando obtención de conexión al SessionManager para: {}", connectionId);
        return sessionManager.getConnection(connectionId);
    }
    
    /**
     * Cierra una sesión específica
     */
    public void closeConnection(String connectionId) {
        log.debug("Delegando cierre de conexión al SessionManager para: {}", connectionId);
        sessionManager.closeSession(connectionId);
    }
}
