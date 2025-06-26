package com.editame.brokermanager.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entidad JPA para conexiones de broker
 */
@Entity
@Table(name = "broker_connections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerConnectionEntity {
    
    @Id
    private String id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String host;
    
    @Column(nullable = false)
    private Integer port;
    
    @Column
    private String username;
    
    @Column
    private String password;
    
    @Column
    private String environment;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Boolean active;
    
    @Column(name = "current_jmx_connection_id")
    private String currentJmxConnectionId;
    
    @Column(name = "last_used")
    private Instant lastUsed;
    
    @Column(name = "last_tested")
    private Instant lastTested;
    
    @Column(name = "last_test_status")
    @Enumerated(EnumType.STRING)
    private ConnectionStatus lastTestStatus;
    
    @Column(name = "last_test_message")
    private String lastTestMessage;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    /**
     * Estados de conexión
     */
    public enum ConnectionStatus {
        CONNECTED,
        DISCONNECTED,
        ERROR,
        TESTING,
        UNKNOWN
    }
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
