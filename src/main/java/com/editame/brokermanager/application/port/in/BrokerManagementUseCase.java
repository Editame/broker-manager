package com.editame.brokermanager.application.port.in;

import com.editame.brokermanager.domain.model.BrokerMetrics;
import com.editame.brokermanager.domain.model.Queue;
import com.editame.brokermanager.domain.model.Message;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada para casos de uso de gestión del broker
 */
public interface BrokerManagementUseCase {
    
    /**
     * Obtiene las métricas actuales del broker
     */
    BrokerMetrics getBrokerMetrics();
    
    /**
     * Obtiene todas las colas del broker
     */
    List<Queue> getAllQueues();
    
    /**
     * Obtiene una cola específica por nombre
     */
    Optional<Queue> getQueue(String queueName);
    
    /**
     * Obtiene los mensajes de una cola específica
     */
    List<Message> getQueueMessages(String queueName, int limit, int offset);
    
    /**
     * Envía un mensaje a una cola
     */
    void sendMessage(String queueName, SendMessageCommand command);
    
    /**
     * Elimina un mensaje específico de una cola
     */
    void deleteMessage(String queueName, String messageId);
    
    /**
     * Purga todos los mensajes de una cola
     */
    void purgeQueue(String queueName);
    
    /**
     * Pausa una cola
     */
    void pauseQueue(String queueName);
    
    /**
     * Reanuda una cola pausada
     */
    void resumeQueue(String queueName);
    
    /**
     * Elimina una cola
     */
    void deleteQueue(String queueName);
    
    /**
     * Crea una nueva cola
     */
    void createQueue(String queueName);
    
    /**
     * Comando para enviar mensaje
     */
    @Builder
    record SendMessageCommand(
        String body,
        java.util.Map<String, Object> headers,
        java.util.Map<String, Object> properties,
        int priority,
        long timeToLive,
        boolean persistent
    ) {}
}
