package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * DTO de respuesta para métricas del broker
 */
@Value
@Builder
@Schema(description = "Métricas del broker ActiveMQ")
public class BrokerMetricsResponse {
    
    @Schema(description = "ID único del broker", example = "broker-001")
    String brokerId;
    
    @Schema(description = "Nombre del broker", example = "localhost")
    String brokerName;
    
    @Schema(description = "Versión de ActiveMQ", example = "5.18.0")
    String brokerVersion;
    
    @Schema(description = "Uso de CPU en porcentaje", example = "45.2")
    double cpuUsage;
    
    @Schema(description = "Memoria utilizada en bytes", example = "2147483648")
    long memoryUsage;
    
    @Schema(description = "Memoria máxima disponible en bytes", example = "4294967296")
    long maxMemory;
    
    @Schema(description = "Porcentaje de memoria utilizada", example = "50.0")
    double memoryUsagePercentage;
    
    @Schema(description = "Total de conexiones", example = "23")
    int totalConnections;
    
    @Schema(description = "Conexiones activas", example = "18")
    int activeConnections;
    
    @Schema(description = "Total de threads", example = "156")
    int totalThreads;
    
    @Schema(description = "Uso de disco en bytes", example = "1073741824")
    long diskUsage;
    
    @Schema(description = "Máximo uso de disco en bytes", example = "10737418240")
    long maxDiskUsage;
    
    @Schema(description = "Porcentaje de disco utilizado", example = "10.0")
    double diskUsagePercentage;
    
    @Schema(description = "Tiempo de inicio del broker")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant startTime;
    
    @Schema(description = "Tiempo de actividad en milisegundos", example = "432000000")
    long uptimeMillis;
    
    @Schema(description = "Tiempo de actividad formateado", example = "5d 3h")
    String uptimeFormatted;
    
    @Schema(description = "Estado del broker", example = "RUNNING")
    String status;
    
    @Schema(description = "Timestamp de la métrica")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp;
}
