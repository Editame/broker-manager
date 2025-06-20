package com.editame.brokermanager.shared.mapper;

import com.editame.brokermanager.domain.model.BrokerMetrics;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.BrokerMetricsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper para convertir entre modelos de dominio y DTOs del broker
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BrokerMapper {
    
    @Mapping(target = "status", expression = "java(metrics.getStatus().name())")
    @Mapping(target = "uptimeFormatted", expression = "java(metrics.getUptimeFormatted())")
    @Mapping(target = "memoryUsagePercentage", expression = "java(metrics.getMemoryUsagePercentage())")
    @Mapping(target = "diskUsagePercentage", expression = "java(metrics.getDiskUsagePercentage())")
    BrokerMetricsResponse toResponse(BrokerMetrics metrics);
}
