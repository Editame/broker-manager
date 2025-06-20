package com.editame.brokermanager.application.service;

import com.editame.brokermanager.application.port.in.BrokerManagementUseCase;
import com.editame.brokermanager.application.port.out.BrokerAdminPort;
import com.editame.brokermanager.domain.model.BrokerMetrics;
import com.editame.brokermanager.domain.model.Queue;
import com.editame.brokermanager.domain.model.Message;
import com.editame.brokermanager.domain.exception.QueueNotFoundException;
import com.editame.brokermanager.domain.exception.MessageNotFoundException;
import com.editame.brokermanager.domain.exception.BrokerOperationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación para gestión del broker
 * Implementa los casos de uso de negocio
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrokerManagementService implements BrokerManagementUseCase {
    
    private final BrokerAdminPort brokerAdminPort;
    
    @Override
    @Cacheable(value = "brokerMetrics", unless = "#result == null")
    public BrokerMetrics getBrokerMetrics() {
        log.debug("Obteniendo métricas del broker");
        try {
            return brokerAdminPort.getBrokerMetrics();
        } catch (Exception e) {
            log.error("Error al obtener métricas del broker", e);
            throw new BrokerOperationException("No se pudieron obtener las métricas del broker", e);
        }
    }
    
    @Override
    @Cacheable(value = "queues", unless = "#result.isEmpty()")
    public List<Queue> getAllQueues() {
        log.debug("Obteniendo todas las colas");
        try {
            List<Queue> queues = brokerAdminPort.listQueues();
            log.info("Se encontraron {} colas", queues.size());
            return queues;
        } catch (Exception e) {
            log.error("Error al obtener las colas", e);
            throw new BrokerOperationException("No se pudieron obtener las colas", e);
        }
    }
    
    @Override
    @Cacheable(value = "queue", key = "#queueName")
    public Optional<Queue> getQueue(String queueName) {
        log.debug("Obteniendo información de la cola: {}", queueName);
        validateQueueName(queueName);
        
        try {
            return brokerAdminPort.getQueueInfo(queueName);
        } catch (Exception e) {
            log.error("Error al obtener información de la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudo obtener información de la cola: " + queueName, e);
        }
    }
    
    @Override
    public List<Message> getQueueMessages(String queueName, int limit, int offset) {
        log.debug("Obteniendo mensajes de la cola: {} (limit: {}, offset: {})", queueName, limit, offset);
        validateQueueName(queueName);
        validatePaginationParams(limit, offset);
        
        if (!brokerAdminPort.queueExists(queueName)) {
            throw new QueueNotFoundException("La cola no existe: " + queueName);
        }
        
        try {
            List<Message> messages = brokerAdminPort.browseMessages(queueName, limit, offset);
            log.info("Se obtuvieron {} mensajes de la cola: {}", messages.size(), queueName);
            return messages;
        } catch (Exception e) {
            log.error("Error al obtener mensajes de la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudieron obtener los mensajes de la cola: " + queueName, e);
        }
    }
    
    @Override
    @CacheEvict(value = {"queues", "queue"}, allEntries = true)
    public void sendMessage(String queueName, SendMessageCommand command) {
        log.debug("Enviando mensaje a la cola: {}", queueName);
        validateQueueName(queueName);
        validateSendMessageCommand(command);
        
        try {
            Message message = Message.builder()
                .destination(queueName)
                .body(command.body())
                .headers(command.headers())
                .properties(command.properties())
                .priority(command.priority())
                .timeToLive(command.timeToLive())
                .persistent(command.persistent())
                .timestamp(Instant.now())
                .status(Message.MessageStatus.PENDING)
                .build();
                
            brokerAdminPort.sendMessage(queueName, message);
            log.info("Mensaje enviado exitosamente a la cola: {}", queueName);
        } catch (Exception e) {
            log.error("Error al enviar mensaje a la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudo enviar el mensaje a la cola: " + queueName, e);
        }
    }
    
    @Override
    @CacheEvict(value = {"queues", "queue"}, allEntries = true)
    public void deleteMessage(String queueName, String messageId) {
        log.debug("Eliminando mensaje {} de la cola: {}", messageId, queueName);
        validateQueueName(queueName);
        validateMessageId(messageId);
        
        if (!brokerAdminPort.queueExists(queueName)) {
            throw new QueueNotFoundException("La cola no existe: " + queueName);
        }
        
        try {
            boolean deleted = brokerAdminPort.deleteMessage(queueName, messageId);
            if (!deleted) {
                throw new MessageNotFoundException("Mensaje no encontrado: " + messageId);
            }
            log.info("Mensaje {} eliminado de la cola: {}", messageId, queueName);
        } catch (MessageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar mensaje {} de la cola: {}", messageId, queueName, e);
            throw new BrokerOperationException("No se pudo eliminar el mensaje", e);
        }
    }
    
    @Override
    @CacheEvict(value = {"queues", "queue"}, allEntries = true)
    public void purgeQueue(String queueName) {
        log.debug("Purgando cola: {}", queueName);
        validateQueueName(queueName);
        
        if (!brokerAdminPort.queueExists(queueName)) {
            throw new QueueNotFoundException("La cola no existe: " + queueName);
        }
        
        try {
            long purgedCount = brokerAdminPort.purgeQueue(queueName);
            log.info("Cola {} purgada. {} mensajes eliminados", queueName, purgedCount);
        } catch (Exception e) {
            log.error("Error al purgar la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudo purgar la cola: " + queueName, e);
        }
    }
    
    @Override
    @CacheEvict(value = {"queues", "queue"}, allEntries = true)
    public void pauseQueue(String queueName) {
        log.debug("Pausando cola: {}", queueName);
        validateQueueName(queueName);
        
        if (!brokerAdminPort.queueExists(queueName)) {
            throw new QueueNotFoundException("La cola no existe: " + queueName);
        }
        
        try {
            brokerAdminPort.pauseQueue(queueName);
            log.info("Cola pausada: {}", queueName);
        } catch (Exception e) {
            log.error("Error al pausar la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudo pausar la cola: " + queueName, e);
        }
    }
    
    @Override
    @CacheEvict(value = {"queues", "queue"}, allEntries = true)
    public void resumeQueue(String queueName) {
        log.debug("Reanudando cola: {}", queueName);
        validateQueueName(queueName);
        
        if (!brokerAdminPort.queueExists(queueName)) {
            throw new QueueNotFoundException("La cola no existe: " + queueName);
        }
        
        try {
            brokerAdminPort.resumeQueue(queueName);
            log.info("Cola reanudada: {}", queueName);
        } catch (Exception e) {
            log.error("Error al reanudar la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudo reanudar la cola: " + queueName, e);
        }
    }
    
    @Override
    @CacheEvict(value = {"queues", "queue"}, allEntries = true)
    public void deleteQueue(String queueName) {
        log.debug("Eliminando cola: {}", queueName);
        validateQueueName(queueName);
        
        if (!brokerAdminPort.queueExists(queueName)) {
            throw new QueueNotFoundException("La cola no existe: " + queueName);
        }
        
        try {
            brokerAdminPort.deleteQueue(queueName);
            log.info("Cola eliminada: {}", queueName);
        } catch (Exception e) {
            log.error("Error al eliminar la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudo eliminar la cola: " + queueName, e);
        }
    }
    
    @Override
    @CacheEvict(value = {"queues", "queue"}, allEntries = true)
    public void createQueue(String queueName) {
        log.debug("Creando cola: {}", queueName);
        validateQueueName(queueName);
        
        if (brokerAdminPort.queueExists(queueName)) {
            throw new BrokerOperationException("La cola ya existe: " + queueName);
        }
        
        try {
            brokerAdminPort.createQueue(queueName);
            log.info("Cola creada: {}", queueName);
        } catch (Exception e) {
            log.error("Error al crear la cola: {}", queueName, e);
            throw new BrokerOperationException("No se pudo crear la cola: " + queueName, e);
        }
    }
    
    // Métodos de validación
    private void validateQueueName(String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la cola no puede estar vacío");
        }
    }
    
    private void validateMessageId(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del mensaje no puede estar vacío");
        }
    }
    
    private void validatePaginationParams(int limit, int offset) {
        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 1000");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("El offset no puede ser negativo");
        }
    }
    
    private void validateSendMessageCommand(SendMessageCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando de envío no puede ser nulo");
        }
        if (command.body() == null || command.body().trim().isEmpty()) {
            throw new IllegalArgumentException("El cuerpo del mensaje no puede estar vacío");
        }
        if (command.priority() < 0 || command.priority() > 9) {
            throw new IllegalArgumentException("La prioridad debe estar entre 0 y 9");
        }
    }
}
