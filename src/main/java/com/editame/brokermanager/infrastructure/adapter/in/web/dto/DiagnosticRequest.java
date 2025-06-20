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
    
    boolean testPing,
    boolean testPort,
    boolean testJmx,
    int timeoutSeconds
) {
    public DiagnosticRequest {
        if (testPing == false && testPort == false && testJmx == false) {
            testPing = true;
            testPort = true;
            testJmx = true;
        }
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 10;
        }
    }
}
