package com.editame.brokermanager.infrastructure.adapter.out.persistence;

import com.editame.brokermanager.application.port.out.ConnectionRepository;
import com.editame.brokermanager.domain.model.BrokerConnection;
import com.editame.brokermanager.infrastructure.adapter.out.persistence.entity.BrokerConnectionEntity;
import com.editame.brokermanager.infrastructure.adapter.out.persistence.repository.JpaBrokerConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para el repositorio de conexiones
 */
@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class JpaConnectionRepositoryAdapter implements ConnectionRepository {
    
    private final JpaBrokerConnectionRepository jpaRepository;
    
    @Override
    public BrokerConnection save(BrokerConnection connection) {
        BrokerConnectionEntity entity = toEntity(connection);
        BrokerConnectionEntity saved = jpaRepository.save(entity);
        log.debug("Conexión guardada en BD: {} ({})", saved.getName(), saved.getId());
        return toDomain(saved);
    }
    
    @Override
    public Optional<BrokerConnection> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }
    
    @Override
    public List<BrokerConnection> findAll() {
        return jpaRepository.findAllByOrderByNameAsc().stream()
            .map(this::toDomain)
            .toList();
    }
    
    @Override
    public List<BrokerConnection> findActive() {
        return jpaRepository.findByActiveTrue().stream()
            .map(this::toDomain)
            .toList();
    }
    
    @Override
    public List<BrokerConnection> findByEnvironment(String environment) {
        return jpaRepository.findByEnvironmentOrderByNameAsc(environment).stream()
            .map(this::toDomain)
            .toList();
    }
    
    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
        log.debug("Conexión eliminada de BD: {}", id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
    
    /**
     * Convierte entidad a modelo de dominio
     */
    private BrokerConnection toDomain(BrokerConnectionEntity entity) {
        return BrokerConnection.builder()
            .id(entity.getId())
            .name(entity.getName())
            .host(entity.getHost())
            .port(entity.getPort())
            .username(entity.getUsername())
            .password(entity.getPassword())
            .environment(entity.getEnvironment())
            .description(entity.getDescription())
            .active(entity.getActive())
            .lastTested(entity.getLastTested())
            .lastTestStatus(entity.getLastTestStatus() != null ? 
                BrokerConnection.ConnectionStatus.valueOf(entity.getLastTestStatus().name()) : null)
            .lastTestMessage(entity.getLastTestMessage())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    /**
     * Convierte modelo de dominio a entidad
     */
    private BrokerConnectionEntity toEntity(BrokerConnection domain) {
        return BrokerConnectionEntity.builder()
            .id(domain.getId() != null ? domain.getId() : UUID.randomUUID().toString())
            .name(domain.getName())
            .host(domain.getHost())
            .port(domain.getPort())
            .username(domain.getUsername())
            .password(domain.getPassword())
            .environment(domain.getEnvironment())
            .description(domain.getDescription())
            .active(domain.isActive())
            .lastTested(domain.getLastTested())
            .lastTestStatus(domain.getLastTestStatus() != null ? 
                BrokerConnectionEntity.ConnectionStatus.valueOf(domain.getLastTestStatus().name()) : null)
            .lastTestMessage(domain.getLastTestMessage())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}
