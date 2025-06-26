package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request para probar una conexión
 */
@Builder
public record TestConnectionRequest(
    @NotBlank(message = "El host es obligatorio")
    @Size(min = 1, max = 255, message = "El host debe tener entre 1 y 255 caracteres")
    String host,
    
    @Min(value = 1, message = "El puerto debe ser mayor a 0")
    @Max(value = 65535, message = "El puerto debe ser menor a 65536")
    int port,
    
    @Size(max = 100, message = "El usuario no puede tener más de 100 caracteres")
    String username,
    
    @Size(max = 255, message = "La contraseña no puede tener más de 255 caracteres")
    String password
) {}
