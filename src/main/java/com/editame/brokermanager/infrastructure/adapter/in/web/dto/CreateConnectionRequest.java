package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request para crear una nueva conexión
 */
@Builder
public record CreateConnectionRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String name,
    
    @NotBlank(message = "El host es obligatorio")
    @Size(min = 1, max = 255, message = "El host debe tener entre 1 y 255 caracteres")
    String host,
    
    @Min(value = 1, message = "El puerto debe ser mayor a 0")
    @Max(value = 65535, message = "El puerto debe ser menor a 65536")
    int port,
    
    @Size(max = 100, message = "El usuario no puede tener más de 100 caracteres")
    String username,
    
    @Size(max = 255, message = "La contraseña no puede tener más de 255 caracteres")
    String password,
    
    @Size(max = 50, message = "El entorno no puede tener más de 50 caracteres")
    String environment,
    
    @Size(max = 500, message = "La descripción no puede tener más de 500 caracteres")
    String description
) {}
