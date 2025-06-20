package com.editame.brokermanager.application.port.out;

import com.editame.brokermanager.domain.model.BrokerMetrics;
import com.editame.brokermanager.domain.model.Queue;
import com.editame.brokermanager.domain.model.Message;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de administración del broker
 */
public interface BrokerAdminPort {
    
    /**
     * Obtiene las métricas del broker
     */
    BrokerMetrics getBrokerMetrics();
    
    /**
     * Lista todas las colas
     */
    List<Queue> listQueues();
    
    /**
     * Obtiene información de una cola específica
     */
    Optional<Queue> getQueueInfo(String queueName);
    
    /**
     * Lista los mensajes de una cola
     */
    List<Message> browseMessages(String queueName, int limit, int offset);
    
    /**
     * Envía un mensaje a una cola
     */
    void sendMessage(String queueName, Message message);
    
    /**
     * Elimina un mensaje específico
     */
    boolean deleteMessage(String queueName, String messageId);
    
    /**
     * Purga una cola
     */
    long purgeQueue(String queueName);
    
    /**
     * Pausa una cola
     */
    void pauseQueue(String queueName);
    
    /**
     * Reanuda una cola
     */
    void resumeQueue(String queueName);
    
    /**
     * Elimina una cola
     */
    void deleteQueue(String queueName);
    
    /**
     * Verifica si una cola existe
     */
    boolean queueExists(String queueName);
    
    /**
     * Crea una nueva cola
     */
    void createQueue(String queueName);
}
