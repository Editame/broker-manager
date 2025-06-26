package com.editame.brokermanager.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.MBeanServerConnection;

@Configuration
public class JmxConfig {

    @Bean
    public MBeanServerConnection mBeanServerConnection() {
        // Retornar null para permitir arranque sin conexión
        // La conexión real se establecerá dinámicamente
        return null;
    }
}
