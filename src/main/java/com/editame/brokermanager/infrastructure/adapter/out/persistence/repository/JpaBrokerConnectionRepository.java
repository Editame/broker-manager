package com.editame.brokermanager.infrastructure.adapter.out.persistence.repository;

import com.editame.brokermanager.infrastructure.adapter.out.persistence.entity.BrokerConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para conexiones de broker
 */
@Repository
public interface JpaBrokerConnectionRepository extends JpaRepository<BrokerConnectionEntity, String> {
    
    /**
     * Busca conexiones activas
     */
    List<BrokerConnectionEntity> findByActiveTrue();
    
    /**
     * Busca conexiones por entorno
     */
    List<BrokerConnectionEntity> findByEnvironmentOrderByNameAsc(String environment);
    
    /**
     * Verifica si existe una conexión con el nombre dado
     */
    boolean existsByName(String name);
    
    /**
     * Verifica si existe una conexión con el nombre dado excluyendo un ID específico
     */
    @Query("SELECT COUNT(c) > 0 FROM BrokerConnectionEntity c WHERE c.name = :name AND c.id != :excludeId")
    boolean existsByNameAndIdNot(String name, String excludeId);
    
    /**
     * Busca todas las conexiones ordenadas por nombre
     */
    List<BrokerConnectionEntity> findAllByOrderByNameAsc();
}
