package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * Request para diagnóstico de conexión
 */
@Builder
public record DiagnosticRequest(
    @NotBlank(message = "El host es obligatorio")
    String host,
    
    @Min(value = 1, message = "El puerto debe ser mayor a 0")
    @Max(value = 65535, message = "El puerto debe ser menor a 65536")
    int port,
    
    String username,
    String password,
    
    @Builder.Default
    boolean testPing = true,
    
    @Builder.Default
    boolean testPort = true,
    
    @Builder.Default
    boolean testJmx = true,
    
    @Builder.Default
    int timeoutSeconds = 10
) {}
