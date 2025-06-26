package com.editame.brokermanager.application.port.out;

import com.editame.brokermanager.domain.model.BrokerMetrics;
import com.editame.brokermanager.domain.model.Message;
import com.editame.brokermanager.domain.model.Queue;
import com.editame.brokermanager.domain.model.RequeueResult;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de administración del broker
 */
public interface BrokerAdminPort {
    
    /**
     * Obtiene las métricas del broker especificado
     */
    BrokerMetrics getBrokerMetrics(String connectionId);
    
    /**
     * Lista todas las colas del broker especificado
     */
    List<Queue> listQueues(String connectionId);
    
    /**
     * Obtiene información de una cola específica del broker especificado
     */
    Optional<Queue> getQueueInfo(String connectionId, String queueName);
    
    /**
     * Lista los mensajes de una cola del broker especificado
     */
    List<Message> browseMessages(String connectionId, String queueName, int limit, int offset);
    
    /**
     * Envía un mensaje a una cola del broker especificado
     */
    void sendMessage(String connectionId, String queueName, Message message);
    
    /**
     * Elimina un mensaje específico del broker especificado
     */
    boolean deleteMessage(String connectionId, String queueName, String messageId);
    
    /**
     * Purga una cola del broker especificado
     */
    long purgeQueue(String connectionId, String queueName);
    
    /**
     * Pausa una cola
     */
    void pauseQueue(String connectionId, String queueName);
    
    /**
     * Reanuda una cola
     */
    void resumeQueue(String connectionId, String queueName);
    
    /**
     * Elimina una cola
     */
    void deleteQueue(String connectionId, String queueName);
    
    /**
     * Verifica si una cola existe
     */
    boolean queueExists(String connectionId, String queueName);
    
    /**
     * Crea una nueva cola
     */
    void createQueue(String connectionId, String queueName);
    
    /**
     * Reencola mensajes de una cola a otra (con límite de seguridad)
     */
    RequeueResult requeueMessages(String connectionId, String sourceQueue, String targetQueue, List<String> messageIds, int maxBatchSize);
}
