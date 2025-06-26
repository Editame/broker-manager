package com.editame.brokermanager.infrastructure.config;

import com.editame.brokermanager.application.port.out.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
        // No inicializar conexiones automáticamente
        // Las conexiones se gestionan dinámicamente desde el frontend
        log.info("Broker Manager iniciado - Listo para gestionar conexiones dinámicamente");
    }
}
