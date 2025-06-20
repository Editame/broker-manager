package com.editame.brokermanager.shared.mapper;

import com.editame.brokermanager.domain.model.Queue;
import com.editame.brokermanager.domain.model.Message;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.QueueResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper para convertir entre modelos de dominio y DTOs de colas
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface QueueMapper {
    
    @Mapping(target = "type", expression = "java(queue.getType().name())")
    @Mapping(target = "status", expression = "java(queue.getStatus().name())")
    @Mapping(target = "empty", expression = "java(queue.isEmpty())")
    @Mapping(target = "hasConsumers", expression = "java(queue.hasConsumers())")
    @Mapping(target = "highLoad", expression = "java(queue.isHighLoad())")
    @Mapping(target = "processingRate", expression = "java(queue.getProcessingRate())")
    @Mapping(target = "messageGroups", source = "messageGroups")
    QueueResponse toResponse(Queue queue);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "messageCount", source = "messageCount")
    QueueResponse.MessageGroupResponse toMessageGroupResponse(Queue.MessageGroup messageGroup);
    
    @Mapping(target = "type", expression = "java(message.getType().name())")
    @Mapping(target = "status", expression = "java(message.getStatus().name())")
    @Mapping(target = "expired", expression = "java(message.isExpired())")
    @Mapping(target = "highPriority", expression = "java(message.isHighPriority())")
    @Mapping(target = "hasRedeliveryLimit", expression = "java(message.hasRedeliveryLimit())")
    @Mapping(target = "age", expression = "java(message.getAge())")
    MessageResponse toMessageResponse(Message message);
}
