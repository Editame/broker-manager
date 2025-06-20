package com.editame.brokermanager.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI
 */
@Configuration
public class SwaggerConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Broker Manager API")
                .description("API REST para gestión de ActiveMQ - Alternativa profesional a QueueExplorer")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Editame Team")
                    .email("support@editame.com")
                    .url("https://github.com/editame/broker-manager"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Servidor de desarrollo"),
                new Server()
                    .url("https://broker-manager.editame.com")
                    .description("Servidor de producción")
            ));
    }
}
