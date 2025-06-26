package com.editame.brokermanager.infrastructure.adapter.in.web;

import com.editame.brokermanager.application.port.in.BrokerManagementUseCase;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.BrokerMetricsResponse;
import com.editame.brokermanager.shared.mapper.BrokerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Controlador REST para operaciones del broker
 */
@RestController
@RequestMapping("/api/broker")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Broker", description = "Operaciones de gestión del broker ActiveMQ")
@CrossOrigin(origins = "*")
public class BrokerController {
    
    private final BrokerManagementUseCase brokerManagementUseCase;
    private final BrokerMapper brokerMapper;
    
    @GetMapping("/metrics")
    @Operation(summary = "Obtener métricas del broker", 
               description = "Retorna las métricas actuales del broker ActiveMQ especificado por connectionId")
    public ResponseEntity<BrokerMetricsResponse> getBrokerMetrics(
            @RequestParam String connectionId) {
        log.info("Solicitando métricas del broker para conexión: {}", connectionId);
        
        var metrics = brokerManagementUseCase.getBrokerMetrics(connectionId);
        var response = brokerMapper.toResponse(metrics);
        
        log.debug("Métricas del broker obtenidas para {}: CPU={}%, Memoria={}MB", 
                 connectionId, metrics.getCpuUsage(), metrics.getMemoryUsage() / 1024 / 1024);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Verificar salud del broker", 
               description = "Endpoint de health check para verificar el estado del broker especificado")
    public ResponseEntity<HealthResponse> getHealth(
            @RequestParam(required = false) String connectionId) {
        try {
            // Si no se especifica connectionId, intentar usar una conexión activa
            if (connectionId == null) {
                // TODO: Obtener primera conexión activa o retornar error
                return ResponseEntity.badRequest().build();
            }
            
            var metrics = brokerManagementUseCase.getBrokerMetrics(connectionId);
            
            HealthResponse health = HealthResponse.builder()
                .status("UP")
                .brokerName(metrics.getBrokerName())
                .uptime(metrics.getUptimeFormatted())
                .timestamp(metrics.getTimestamp())
                .build();
                
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Error en health check", e);
            
            HealthResponse health = HealthResponse.builder()
                .status("DOWN")
                .error(e.getMessage())
                .timestamp(Instant.now())
                .build();
                
            return ResponseEntity.status(503).body(health);
        }
    }
    
    /**
     * DTO para respuesta de health check
     */
    @lombok.Builder
    public record HealthResponse(
        String status,
        String brokerName,
        String uptime,
        String error,
        Instant timestamp
    ) {}
}
