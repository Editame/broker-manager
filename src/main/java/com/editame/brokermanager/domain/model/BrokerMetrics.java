package com.editame.brokermanager.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Métricas del broker ActiveMQ
 */
@Value
@Builder
public class BrokerMetrics {
    String brokerId;
    String brokerName;
    String brokerVersion;
    
    // Métricas de rendimiento
    double cpuUsage;
    long memoryUsage;
    long maxMemory;
    
    // Métricas de conectividad
    int totalConnections;
    int activeConnections;
    int totalThreads;
    
    // Métricas de almacenamiento
    long diskUsage;
    long maxDiskUsage;
    
    // Métricas de tiempo
    Instant startTime;
    long uptimeMillis;
    
    // Estado del broker
    BrokerStatus status;
    
    Instant timestamp;
    
    public enum BrokerStatus {
        STARTING, RUNNING, STOPPING, STOPPED, ERROR
    }
    
    public double getMemoryUsagePercentage() {
        return maxMemory > 0 ? (double) memoryUsage / maxMemory * 100 : 0;
    }
    
    public double getDiskUsagePercentage() {
        return maxDiskUsage > 0 ? (double) diskUsage / maxDiskUsage * 100 : 0;
    }
    
    public String getUptimeFormatted() {
        long seconds = uptimeMillis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        
        if (days > 0) {
            return String.format("%dd %dh", days, hours);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
