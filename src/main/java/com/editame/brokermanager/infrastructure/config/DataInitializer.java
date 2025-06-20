package com.editame.brokermanager.infrastructure.config;

import com.editame.brokermanager.application.port.out.ConnectionRepository;
import com.editame.brokermanager.domain.model.BrokerConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Inicializador de datos por defecto
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    
    private final ConnectionRepository connectionRepository;
    
    @Override
    public void run(ApplicationArguments args) {
        initializeDefaultConnection();
    }
    
    /**
     * Crea la conexión por defecto si no existe
     */
    private void initializeDefaultConnection() {
        if (connectionRepository.findAll().isEmpty()) {
            log.info("Inicializando conexión por defecto...");
            
            BrokerConnection defaultConnection = BrokerConnection.builder()
                .id(UUID.randomUUID().toString())
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
            
            connectionRepository.save(defaultConnection);
            log.info("Conexión por defecto creada: {}", defaultConnection.getName());
        } else {
            log.debug("Conexiones ya existen, omitiendo inicialización por defecto");
        }
    }
}
