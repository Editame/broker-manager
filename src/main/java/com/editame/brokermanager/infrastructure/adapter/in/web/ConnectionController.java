package com.editame.brokermanager.infrastructure.adapter.in.web;

import com.editame.brokermanager.application.port.in.ConnectionManagementUseCase;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.ConnectionResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.CreateConnectionRequest;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.TestConnectionRequest;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.TestConnectionResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.UpdateConnectionRequest;
import com.editame.brokermanager.shared.mapper.ConnectionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gestión de conexiones
 */
@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Connections", description = "Gestión de conexiones a brokers ActiveMQ")
@CrossOrigin(origins = "*")
public class ConnectionController {
    
    private final ConnectionManagementUseCase connectionManagementUseCase;
    private final ConnectionMapper connectionMapper;
    
    @GetMapping
    @Operation(summary = "Listar todas las conexiones", 
               description = "Obtiene la lista completa de conexiones configuradas")
    public ResponseEntity<List<ConnectionResponse>> getAllConnections() {
        log.info("Solicitando lista de todas las conexiones");
        
        var connections = connectionManagementUseCase.getAllConnections();
        var response = connections.stream()
            .map(connectionMapper::toResponse)
            .toList();
        
        log.info("Se encontraron {} conexiones", response.size());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Obtener conexión activa", 
               description = "Retorna la conexión actualmente activa")
    public ResponseEntity<ConnectionResponse> getActiveConnection() {
        log.info("Solicitando conexión activa");
        
        var activeConnection = connectionManagementUseCase.getActiveConnection();
        
        if (activeConnection.isEmpty()) {
            log.warn("No hay conexión activa");
            return ResponseEntity.notFound().build();
        }
        
        var response = connectionMapper.toResponse(activeConnection.get());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{connectionId}")
    @Operation(summary = "Obtener conexión por ID",
               description = "Retorna una conexión específica por su ID")
    public ResponseEntity<ConnectionResponse> getConnection(
            @Parameter(description = "ID de la conexión", example = "default-localhost")
            @PathVariable String connectionId) {
        log.info("Solicitando conexión: {}", connectionId);
        
        var connection = connectionManagementUseCase.getConnection(connectionId);
        
        if (connection.isEmpty()) {
            log.warn("Conexión no encontrada: {}", connectionId);
            return ResponseEntity.notFound().build();
        }
        
        var response = connectionMapper.toResponse(connection.get());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Crear nueva conexión",
               description = "Crea una nueva conexión a un broker ActiveMQ")
    public ResponseEntity<ConnectionResponse> createConnection(
            @Valid @RequestBody CreateConnectionRequest request) {
        log.info("Creando nueva conexión: {}", request.name());
        
        var command = ConnectionManagementUseCase.CreateConnectionCommand.builder()
            .name(request.name())
            .host(request.host())
            .port(request.port())
            .username(request.username())
            .password(request.password())
            .environment(request.environment())
            .description(request.description())
            .build();
        
        var connection = connectionManagementUseCase.createConnection(command);
        var response = connectionMapper.toResponse(connection);
        
        log.info("Conexión creada exitosamente: {}", connection.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{connectionId}")
    @Operation(summary = "Actualizar conexión",
               description = "Actualiza una conexión existente")
    public ResponseEntity<ConnectionResponse> updateConnection(
            @Parameter(description = "ID de la conexión", example = "default-localhost")
            @PathVariable String connectionId,
            @Valid @RequestBody UpdateConnectionRequest request) {
        log.info("Actualizando conexión: {}", connectionId);
        
        var command = ConnectionManagementUseCase.UpdateConnectionCommand.builder()
            .name(request.name())
            .host(request.host())
            .port(request.port())
            .username(request.username())
            .password(request.password())
            .environment(request.environment())
            .description(request.description())
            .active(request.active())
            .build();
        
        var connection = connectionManagementUseCase.updateConnection(connectionId, command);
        var response = connectionMapper.toResponse(connection);
        
        log.info("Conexión actualizada exitosamente: {}", connectionId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{connectionId}")
    @Operation(summary = "Eliminar conexión",
               description = "Elimina una conexión existente")
    public ResponseEntity<Void> deleteConnection(
            @Parameter(description = "ID de la conexión", example = "default-localhost")
            @PathVariable String connectionId) {
        log.info("Eliminando conexión: {}", connectionId);
        
        connectionManagementUseCase.deleteConnection(connectionId);
        
        log.info("Conexión eliminada exitosamente: {}", connectionId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{connectionId}/test")
    @Operation(summary = "Probar conexión",
               description = "Prueba la conectividad de una conexión específica")
    public ResponseEntity<ConnectionResponse> testConnection(
            @Parameter(description = "ID de la conexión", example = "default-localhost")
            @PathVariable String connectionId) {
        log.info("Probando conexión: {}", connectionId);
        
        var connection = connectionManagementUseCase.testConnection(connectionId);
        var response = connectionMapper.toResponse(connection);
        
        log.info("Prueba de conexión completada: {} - {}", connectionId, connection.getLastTestStatus());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test")
    @Operation(summary = "Probar configuración de conexión",
               description = "Prueba una configuración de conexión sin guardarla")
    public ResponseEntity<TestConnectionResponse> testConnectionConfig(
            @Valid @RequestBody TestConnectionRequest request) {
        log.info("Probando configuración de conexión: {}:{}", request.host(), request.port());
        
        var command = ConnectionManagementUseCase.TestConnectionCommand.builder()
            .host(request.host())
            .port(request.port())
            .username(request.username())
            .password(request.password())
            .build();
        
        var status = connectionManagementUseCase.testConnectionConfig(command);
        
        var response = TestConnectionResponse.builder()
            .status(status.name())
            .success(status == com.editame.brokermanager.domain.model.BrokerConnection.ConnectionStatus.CONNECTED)
            .message(status == com.editame.brokermanager.domain.model.BrokerConnection.ConnectionStatus.CONNECTED ? 
                "Conexión exitosa" : "Error de conexión")
            .timestamp(java.time.Instant.now())
            .build();
        
        log.info("Prueba de configuración completada: {}:{} - {}", request.host(), request.port(), status);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{connectionId}/activate")
    @Operation(summary = "Activar conexión",
               description = "Activa una conexión específica (desactiva las demás)")
    public ResponseEntity<ConnectionResponse> activateConnection(
            @Parameter(description = "ID de la conexión", example = "default-localhost")
            @PathVariable String connectionId) {
        log.info("Activando conexión: {}", connectionId);
        
        var connection = connectionManagementUseCase.activateConnection(connectionId);
        var response = connectionMapper.toResponse(connection);
        
        log.info("Conexión activada exitosamente: {}", connectionId);
        return ResponseEntity.ok(response);
    }
}
