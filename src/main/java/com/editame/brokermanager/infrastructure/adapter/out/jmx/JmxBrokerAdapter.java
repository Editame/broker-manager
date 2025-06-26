package com.editame.brokermanager.infrastructure.adapter.out.jmx;

import com.editame.brokermanager.application.port.out.BrokerAdminPort;
import com.editame.brokermanager.domain.exception.BrokerOperationException;
import com.editame.brokermanager.domain.model.BrokerMetrics;
import com.editame.brokermanager.domain.model.Message;
import com.editame.brokermanager.domain.model.Queue;
import com.editame.brokermanager.domain.model.RequeueResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador JMX para conectar con ActiveMQ
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JmxBrokerAdapter implements BrokerAdminPort {
    
    private final JmxConnectionManager connectionManager;
    
    /**
     * Método auxiliar para obtener JMXConnector usando nuestro UUID
     * El JmxConnectionManager internamente maneja el mapeo UUID → JMX Connection ID
     */
    private JMXConnector getJmxConnector(String ourConnectionId) {
        return connectionManager.getConnection(ourConnectionId);
    }
    
    @Override
    public BrokerMetrics getBrokerMetrics(String connectionId) {
        log.debug("Obteniendo métricas del broker via JMX para conexión: {}", connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            // Obtener métricas del broker
            ObjectName brokerObjectName = new ObjectName("org.apache.activemq:type=Broker,brokerName=*");
            Set<ObjectInstance> brokerInstances = connection.queryMBeans(brokerObjectName, null);
            
            if (brokerInstances.isEmpty()) {
                throw new BrokerOperationException("No se encontró el broker ActiveMQ");
            }
            
            ObjectInstance brokerInstance = brokerInstances.iterator().next();
            ObjectName brokerName = brokerInstance.getObjectName();
            
            // Métricas básicas del broker
            String brokerId = (String) connection.getAttribute(brokerName, "BrokerId");
            String brokerNameStr = (String) connection.getAttribute(brokerName, "BrokerName");
            String brokerVersion = (String) connection.getAttribute(brokerName, "BrokerVersion");
            
            // Métricas de conectividad - manejar tanto Integer como Long
            Object totalConnectionsObj = connection.getAttribute(brokerName, "TotalConnectionsCount");
            Integer totalConnections = totalConnectionsObj instanceof Long ? 
                ((Long) totalConnectionsObj).intValue() : (Integer) totalConnectionsObj;
                
            Object currentConnectionsObj = connection.getAttribute(brokerName, "CurrentConnectionsCount");
            Integer currentConnections = currentConnectionsObj instanceof Long ? 
                ((Long) currentConnectionsObj).intValue() : (Integer) currentConnectionsObj;
            
            // Métricas de memoria del sistema
            MemoryMXBean memoryBean = ManagementFactory.newPlatformMXBeanProxy(
                connection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
            long memoryUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long memoryMax = memoryBean.getHeapMemoryUsage().getMax();
            
            // Métricas de CPU del sistema
            double cpuUsage = getCpuUsage(connection);
            
            // Métricas de tiempo
            Long uptime = safeConvertToLong(connection.getAttribute(brokerName, "UptimeMillis"));
            
            return BrokerMetrics.builder()
                .brokerId(brokerId)
                .brokerName(brokerNameStr)
                .brokerVersion(brokerVersion)
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsed)
                .maxMemory(memoryMax)
                .totalConnections(totalConnections != null ? totalConnections : 0)
                .activeConnections(currentConnections != null ? currentConnections : 0)
                .totalThreads(getActiveThreadCount(connection))
                .diskUsage(getDiskUsage(connection, brokerName))
                .maxDiskUsage(getMaxDiskUsage(connection, brokerName))
                .startTime(Instant.now().minusMillis(uptime != null ? uptime : 0))
                .uptimeMillis(uptime != null ? uptime : 0)
                .status(BrokerMetrics.BrokerStatus.RUNNING)
                .timestamp(Instant.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error al obtener métricas del broker para conexión: {}", connectionId, e);
            throw new BrokerOperationException("Error al conectar con el broker via JMX", e);
        }
    }
    
    @Override
    public List<Queue> listQueues(String connectionId) {
        log.debug("Listando colas via JMX para conexión: {}", connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            // Buscar todas las colas
            ObjectName queuePattern = new ObjectName("org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=*");
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queuePattern, null);
            
            List<Queue> queues = new ArrayList<>();
            
            for (ObjectInstance queueInstance : queueInstances) {
                ObjectName queueName = queueInstance.getObjectName();
                Queue queue = buildQueueFromMBean(connection, queueName);
                queues.add(queue);
            }
            
            log.info("Se encontraron {} colas para conexión: {}", queues.size(), connectionId);
            return queues;
            
        } catch (Exception e) {
            log.error("Error al listar colas para conexión: {}", connectionId, e);
            throw new BrokerOperationException("Error al obtener las colas via JMX", e);
        }
    }
    
    @Override
    public Optional<Queue> getQueueInfo(String connectionId, String queueName) {
        log.debug("Obteniendo información de la cola: {} para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
            
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
            
            if (queueInstances.isEmpty()) {
                return Optional.empty();
            }
            
            ObjectName queueObjName = queueInstances.iterator().next().getObjectName();
            Queue queue = buildQueueFromMBean(connection, queueObjName);
            
            return Optional.of(queue);
            
        } catch (Exception e) {
            log.error("Error al obtener información de la cola: {} para conexión: {}", queueName, connectionId, e);
            throw new BrokerOperationException("Error al obtener información de la cola via JMX", e);
        }
    }
    
    @Override
    public List<Message> browseMessages(String connectionId, String queueName, int limit, int offset) {
        log.debug("Navegando mensajes de la cola: {} para conexión: {} (limit: {}, offset: {})", queueName, connectionId, limit, offset);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
            
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
            
            if (queueInstances.isEmpty()) {
                return Collections.emptyList();
            }
            
            ObjectName queueObjName = queueInstances.iterator().next().getObjectName();
            
            // Usar la operación browse para obtener mensajes
            Object[] params = {};
            String[] signature = {};
            
            CompositeData[] messages = (CompositeData[]) connection.invoke(
                queueObjName, "browse", params, signature);
            
            if (messages == null) {
                return Collections.emptyList();
            }
            
            return Arrays.stream(messages)
                .skip(offset)
                .limit(limit)
                .map(this::buildMessageFromCompositeData)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error al navegar mensajes de la cola: {}", queueName, e);
            throw new BrokerOperationException("Error al obtener mensajes via JMX", e);
        }
    }
    
    @Override
    public void sendMessage(String connectionId, String queueName, Message message) {
        log.debug("Enviando mensaje a la cola: {} para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
            
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
            
            if (queueInstances.isEmpty()) {
                throw new BrokerOperationException("Cola no encontrada: " + queueName);
            }
            
            ObjectName queueObjName = queueInstances.iterator().next().getObjectName();
            
            // Preparar parámetros para enviar mensaje (simplificado)
            Object[] params = {
                message.getBody()
            };
            String[] signature = {"java.lang.String"};
            
            connection.invoke(queueObjName, "sendTextMessage", params, signature);
            log.info("Mensaje enviado exitosamente a la cola: {}", queueName);
            
        } catch (Exception e) {
            log.error("Error al enviar mensaje a la cola: {}", queueName, e);
            throw new BrokerOperationException("Error al enviar mensaje via JMX", e);
        }
    }
    
    @Override
    public boolean deleteMessage(String connectionId, String queueName, String messageId) {
        log.debug("Eliminando mensaje {} de la cola: {} para conexión: {}", messageId, queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
            
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
            
            if (queueInstances.isEmpty()) {
                return false;
            }
            
            ObjectName queueObjName = queueInstances.iterator().next().getObjectName();
            
            Object[] params = {messageId};
            String[] signature = {"java.lang.String"};
            
            Boolean result = (Boolean) connection.invoke(queueObjName, "removeMessage", params, signature);
            
            log.info("Mensaje {} eliminado de la cola: {} - Resultado: {}", messageId, queueName, result);
            return result != null ? result : false;
            
        } catch (Exception e) {
            log.error("Error al eliminar mensaje {} de la cola: {}", messageId, queueName, e);
            throw new BrokerOperationException("Error al eliminar mensaje via JMX", e);
        }
    }
    
    @Override
    public long purgeQueue(String connectionId, String queueName) {
        log.debug("Purgando cola: {} para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
            
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
            
            if (queueInstances.isEmpty()) {
                throw new BrokerOperationException("Cola no encontrada: " + queueName);
            }
            
            ObjectName queueObjName = queueInstances.iterator().next().getObjectName();
            
            // Obtener el tamaño actual antes de purgar
            Long currentSize = safeConvertToLong(connection.getAttribute(queueObjName, "QueueSize"));
            
            // Purgar la cola
            connection.invoke(queueObjName, "purge", new Object[]{}, new String[]{});
            
            log.info("Cola {} purgada. {} mensajes eliminados", queueName, currentSize);
            return currentSize != null ? currentSize : 0;
            
        } catch (Exception e) {
            log.error("Error al purgar la cola: {}", queueName, e);
            throw new BrokerOperationException("Error al purgar cola via JMX", e);
        }
    }
    
    @Override
    public void pauseQueue(String connectionId, String queueName) {
        log.debug("Pausando cola: {} para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
            
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
            
            if (queueInstances.isEmpty()) {
                throw new BrokerOperationException("Cola no encontrada: " + queueName);
            }
            
            ObjectName queueObjName = queueInstances.iterator().next().getObjectName();
            connection.invoke(queueObjName, "pause", new Object[]{}, new String[]{});
            
            log.info("Cola pausada: {}", queueName);
            
        } catch (Exception e) {
            log.error("Error al pausar la cola: {}", queueName, e);
            throw new BrokerOperationException("Error al pausar cola via JMX", e);
        }
    }
    
    @Override
    public void resumeQueue(String connectionId, String queueName) {
        log.debug("Reanudando cola: {} para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
            
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
            
            if (queueInstances.isEmpty()) {
                throw new BrokerOperationException("Cola no encontrada: " + queueName);
            }
            
            ObjectName queueObjName = queueInstances.iterator().next().getObjectName();
            connection.invoke(queueObjName, "resume", new Object[]{}, new String[]{});
            
            log.info("Cola reanudada: {}", queueName);
            
        } catch (Exception e) {
            log.error("Error al reanudar la cola: {}", queueName, e);
            throw new BrokerOperationException("Error al reanudar cola via JMX", e);
        }
    }
    
    @Override
    public void deleteQueue(String connectionId, String queueName) {
        log.debug("Eliminando cola: {} para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            ObjectName brokerObjectName = new ObjectName("org.apache.activemq:type=Broker,brokerName=*");
            Set<ObjectInstance> brokerInstances = connection.queryMBeans(brokerObjectName, null);
            
            if (brokerInstances.isEmpty()) {
                throw new BrokerOperationException("Broker no encontrado");
            }
            
            ObjectName brokerObjName = brokerInstances.iterator().next().getObjectName();
            
            Object[] params = {queueName};
            String[] signature = {"java.lang.String"};
            
            connection.invoke(brokerObjName, "removeQueue", params, signature);
            
            log.info("Cola eliminada: {}", queueName);
            
        } catch (Exception e) {
            log.error("Error al eliminar la cola: {}", queueName, e);
            throw new BrokerOperationException("Error al eliminar cola via JMX", e);
        }
    }
    
    @Override
    public boolean queueExists(String connectionId, String queueName) {
        log.info("🔍 Verificando existencia de cola: '{}' para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            // Usar el mismo patrón que listQueues para consistencia
            ObjectName queuePattern = new ObjectName("org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=*");
            Set<ObjectInstance> queueInstances = connection.queryMBeans(queuePattern, null);
            
            log.info("🔍 Total de colas encontradas en JMX: {}", queueInstances.size());
            
            // Buscar la cola específica por nombre
            for (ObjectInstance queueInstance : queueInstances) {
                ObjectName queueObjectName = queueInstance.getObjectName();
                try {
                    String existingQueueName = (String) connection.getAttribute(queueObjectName, "Name");
                    log.debug("🔍 Comparando '{}' con '{}'", queueName, existingQueueName);
                    
                    if (queueName.equals(existingQueueName)) {
                        log.info("✅ Cola encontrada: '{}'", queueName);
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error al obtener nombre de cola desde MBean: {}", queueObjectName, e);
                }
            }
            
            log.warn("❌ Cola NO encontrada: '{}'", queueName);
            return false;
            
        } catch (Exception e) {
            log.error("💥 Error al verificar existencia de la cola: '{}'", queueName, e);
            return false;
        }
    }
    
    @Override
    public void createQueue(String connectionId, String queueName) {
        log.debug("Creando cola: {} para conexión: {}", queueName, connectionId);
        
        JMXConnector connector = getJmxConnector(connectionId);
        try {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            // Buscar el broker
            ObjectName brokerObjectName = new ObjectName("org.apache.activemq:type=Broker,brokerName=*");
            Set<ObjectInstance> brokerInstances = connection.queryMBeans(brokerObjectName, null);
            
            if (brokerInstances.isEmpty()) {
                throw new BrokerOperationException("No se encontró el broker ActiveMQ");
            }
            
            ObjectName brokerObjName = brokerInstances.iterator().next().getObjectName();
            
            // Crear la cola usando la operación addQueue del broker
            Object[] params = {queueName};
            String[] signature = {"java.lang.String"};
            
            connection.invoke(brokerObjName, "addQueue", params, signature);
            log.info("Cola creada exitosamente: {}", queueName);
            
        } catch (Exception e) {
            log.error("Error al crear la cola: {}", queueName, e);
            throw new BrokerOperationException("Error al crear la cola via JMX", e);
        }
    }

    @Override
    public RequeueResult requeueMessages(String connectionId, String sourceQueue, String targetQueue, List<String> messageIds, int maxBatchSize) {
        log.info("Iniciando reencolamiento de {} mensajes de {} a {}", messageIds.size(), sourceQueue, targetQueue);

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;
        List<String> failedIds = new ArrayList<>();

        // LÍMITE DE SEGURIDAD: Máximo 500 mensajes por operación
        final int SAFETY_LIMIT = 500;
        int actualBatchSize = Math.min(maxBatchSize, SAFETY_LIMIT);

        if (messageIds.size() > actualBatchSize) {
            String errorMsg = String.format("Límite de seguridad: máximo %d mensajes por operación. Solicitados: %d",
                actualBatchSize, messageIds.size());
            log.warn(errorMsg);

            return RequeueResult.builder()
                .sourceQueue(sourceQueue)
                .targetQueue(targetQueue)
                .totalRequested(messageIds.size())
                .successfullyRequeued(0)
                .failed(messageIds.size())
                .failedMessageIds(messageIds)
                .durationMillis(System.currentTimeMillis() - startTime)
                .timestamp(Instant.now())
                .message("Operación rechazada por límite de seguridad")
                .success(false)
                .details(errorMsg)
                .build();
        }
        JMXConnector connector = getJmxConnector(connectionId);
        try  {
            MBeanServerConnection connection = connector.getMBeanServerConnection();

            // Verificar que ambas colas existen
            if (!queueExists(connectionId, sourceQueue)) {
                throw new BrokerOperationException("Cola origen no existe para el connectionID: "
                        + connectionId + " y la cola: " + sourceQueue);
            }
            if (!queueExists(connectionId, targetQueue)) {
                throw new BrokerOperationException("Cola destino no existe: " + targetQueue);
            }

            // Procesar mensajes en lotes pequeños para evitar timeouts
            final int MICRO_BATCH_SIZE = 10;
            List<List<String>> microBatches = partitionList(messageIds, MICRO_BATCH_SIZE);

            for (List<String> microBatch : microBatches) {
                try {
                    // Procesar micro-lote
                    for (String messageId : microBatch) {
                        try {
                            if (requeueSingleMessage(connection, sourceQueue, targetQueue, messageId)) {
                                successCount++;
                            } else {
                                failCount++;
                                failedIds.add(messageId);
                            }

                            // Pequeña pausa para no saturar el broker
                            Thread.sleep(10);

                        } catch (Exception e) {
                            log.warn("Error al reencolar mensaje {}: {}", messageId, e.getMessage());
                            failCount++;
                            failedIds.add(messageId);
                        }
                    }

                    // Pausa entre micro-lotes
                    Thread.sleep(50);

                } catch (Exception e) {
                    log.error("Error en micro-lote: {}", e.getMessage());
                    // Marcar todos los mensajes del micro-lote como fallidos
                    failCount += microBatch.size();
                    failedIds.addAll(microBatch);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            boolean isSuccess = failCount == 0;

            String message = String.format("Reencolamiento completado: %d exitosos, %d fallidos de %d total",
                successCount, failCount, messageIds.size());

            log.info("{} - Duración: {}ms", message, duration);

            return RequeueResult.builder()
                .sourceQueue(sourceQueue)
                .targetQueue(targetQueue)
                .totalRequested(messageIds.size())
                .successfullyRequeued(successCount)
                .failed(failCount)
                .failedMessageIds(failedIds)
                .durationMillis(duration)
                .timestamp(Instant.now())
                .message(message)
                .success(isSuccess)
                .details(isSuccess ? "Operación completada exitosamente" :
                    String.format("Algunos mensajes fallaron. Ver failedMessageIds para detalles."))
                .build();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error crítico durante reencolamiento: {}", e.getMessage(), e);

            return RequeueResult.builder()
                .sourceQueue(sourceQueue)
                .targetQueue(targetQueue)
                .totalRequested(messageIds.size())
                .successfullyRequeued(successCount)
                .failed(messageIds.size() - successCount)
                .failedMessageIds(messageIds.subList(successCount, messageIds.size()))
                .durationMillis(duration)
                .timestamp(Instant.now())
                .message("Error crítico durante reencolamiento")
                .success(false)
                .details(e.getMessage())
                .build();
        }
    }
    
    /**
     * Reencola un solo mensaje de forma segura
     */
    private boolean requeueSingleMessage(MBeanServerConnection connection, String sourceQueue, String targetQueue, String messageId) {
        try {
            // 1. Obtener el mensaje de la cola origen
            ObjectName sourceQueueObjName = getQueueObjectName(connection, sourceQueue);
            if (sourceQueueObjName == null) {
                return false;
            }
            
            // 2. Buscar el mensaje específico
            CompositeData[] messages = (CompositeData[]) connection.invoke(
                sourceQueueObjName, "browse", new Object[]{}, new String[]{});
            
            CompositeData targetMessage = null;
            for (CompositeData message : messages) {
                String msgId = (String) message.get("JMSMessageID");
                if (messageId.equals(msgId)) {
                    targetMessage = message;
                    break;
                }
            }
            
            if (targetMessage == null) {
                log.warn("Mensaje no encontrado: {}", messageId);
                return false;
            }
            
            // 3. Extraer contenido del mensaje
            String messageBody = (String) targetMessage.get("Text");
            if (messageBody == null) {
                messageBody = "";
            }
            
            // 4. Enviar a cola destino
            ObjectName targetQueueObjName = getQueueObjectName(connection, targetQueue);
            if (targetQueueObjName == null) {
                return false;
            }
            
            Object[] params = {messageBody};
            String[] signature = {"java.lang.String"};
            connection.invoke(targetQueueObjName, "sendTextMessage", params, signature);
            
            // 5. Eliminar de cola origen (solo si el envío fue exitoso)
            Object[] deleteParams = {messageId};
            String[] deleteSignature = {"java.lang.String"};
            connection.invoke(sourceQueueObjName, "removeMessage", deleteParams, deleteSignature);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error al reencolar mensaje {}: {}", messageId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene el ObjectName de una cola
     */
    private ObjectName getQueueObjectName(MBeanServerConnection connection, String queueName) throws Exception {
        ObjectName queueObjectName = new ObjectName(
            "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName);
        
        Set<ObjectInstance> queueInstances = connection.queryMBeans(queueObjectName, null);
        
        if (queueInstances.isEmpty()) {
            return null;
        }
        
        return queueInstances.iterator().next().getObjectName();
    }
    
    /**
     * Divide una lista en sublistas más pequeñas
     */
    private <T> List<List<T>> partitionList(List<T> list, int partitionSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += partitionSize) {
            partitions.add(list.subList(i, Math.min(i + partitionSize, list.size())));
        }
        return partitions;
    }
    
    // Métodos auxiliares
    private Queue buildQueueFromMBean(MBeanServerConnection connection, ObjectName queueName) throws Exception {
        String name = queueName.getKeyProperty("destinationName");
        
        // Usar métodos seguros para conversión
        Long queueSize = safeConvertToLong(connection.getAttribute(queueName, "QueueSize"));
        Long enqueueCount = safeConvertToLong(connection.getAttribute(queueName, "EnqueueCount"));
        Long dequeueCount = safeConvertToLong(connection.getAttribute(queueName, "DequeueCount"));
        Integer consumerCount = safeConvertToInteger(connection.getAttribute(queueName, "ConsumerCount"));
        Integer producerCount = safeConvertToInteger(connection.getAttribute(queueName, "ProducerCount"));
        Long memoryLimit = safeConvertToLong(connection.getAttribute(queueName, "MemoryLimit"));
        Integer memoryPercentUsage = safeConvertToInteger(connection.getAttribute(queueName, "MemoryPercentUsage"));
        
        return Queue.builder()
            .name(name)
            .type(Queue.QueueType.QUEUE)
            .status(Queue.QueueStatus.ACTIVE)
            .queueSize(queueSize != null ? queueSize : 0)
            .enqueueCount(enqueueCount != null ? enqueueCount : 0)
            .dequeueCount(dequeueCount != null ? dequeueCount : 0)
            .consumerCount(consumerCount)
            .producerCount(producerCount)
            .memoryLimit(memoryLimit != null ? memoryLimit : 0)
            .memoryPercentUsage(memoryPercentUsage != null ? memoryPercentUsage : 0)
            .paused(false)
            .createdAt(Instant.now())
            .build();
    }
    
    private Message buildMessageFromCompositeData(CompositeData compositeData) {
        String messageId = (String) compositeData.get("JMSMessageID");
        String correlationId = (String) compositeData.get("JMSCorrelationID");
        
        // Manejar timestamp que puede ser Date o Long
        Object timestampObj = compositeData.get("JMSTimestamp");
        Long timestamp = null;
        if (timestampObj instanceof Date) {
            timestamp = ((Date) timestampObj).getTime();
        } else if (timestampObj instanceof Long) {
            timestamp = (Long) timestampObj;
        }
        
        // Manejar priority que puede ser Integer o Long
        Object priorityObj = compositeData.get("JMSPriority");
        Integer priority = null;
        if (priorityObj instanceof Long) {
            priority = ((Long) priorityObj).intValue();
        } else if (priorityObj instanceof Integer) {
            priority = (Integer) priorityObj;
        }
        
        String text = (String) compositeData.get("Text");
        
        return Message.builder()
            .id(messageId)
            .correlationId(correlationId)
            .body(text != null ? text : "")
            .type(Message.MessageType.TEXT)
            .priority(priority != null ? priority : 4)
            .timestamp(timestamp != null ? Instant.ofEpochMilli(timestamp) : Instant.now())
            .status(Message.MessageStatus.PENDING)
            .build();
    }
    
    private int getActiveThreadCount(MBeanServerConnection connection) {
        try {
            ObjectName threadingObjectName = new ObjectName("java.lang:type=Threading");
            Integer threadCount = (Integer) connection.getAttribute(threadingObjectName, "ThreadCount");
            return threadCount != null ? threadCount : 0;
        } catch (Exception e) {
            log.warn("No se pudo obtener el número de threads", e);
            return 0;
        }
    }
    
    /**
     * Obtiene el uso de CPU del sistema donde corre el broker ActiveMQ
     */
    private double getCpuUsage(MBeanServerConnection connection) {
        try {
            log.debug("Intentando obtener métricas de CPU del broker");
            
            // Intentar obtener CPU del sistema operativo
            ObjectName osObjectName = new ObjectName("java.lang:type=OperatingSystem");
            
            // Estrategia 1: ProcessCpuLoad (método preferido)
            Double processCpuLoad = getAttributeSafely(connection, osObjectName, "ProcessCpuLoad", Double.class);
            if (processCpuLoad != null && processCpuLoad >= 0) {
                double cpuPercentage = processCpuLoad * 100.0;
                log.info("CPU del proceso obtenida exitosamente: {}%", cpuPercentage);
                return cpuPercentage;
            }
            
            // Estrategia 2: SystemCpuLoad
            Double systemCpuLoad = getAttributeSafely(connection, osObjectName, "SystemCpuLoad", Double.class);
            if (systemCpuLoad != null && systemCpuLoad >= 0) {
                double cpuPercentage = systemCpuLoad * 100.0;
                log.info("CPU del sistema obtenida exitosamente: {}%", cpuPercentage);
                return cpuPercentage;
            }
            
            // Estrategia 3: ProcessCpuTime
            Long processCpuTime = getAttributeSafely(connection, osObjectName, "ProcessCpuTime", Long.class);
            if (processCpuTime != null && processCpuTime > 0) {
                double approximateCpu = Math.min(processCpuTime / 1_000_000_000.0 / 60.0 * 100.0, 100.0);
                log.info("CPU aproximado calculado: {}%", approximateCpu);
                return approximateCpu;
            }
            
            // Estrategia 4: Estimación basada en threads
            String osName = getAttributeSafely(connection, osObjectName, "Name", String.class);
            Integer availableProcessors = getAttributeSafely(connection, osObjectName, "AvailableProcessors", Integer.class);
            
            if (osName != null && availableProcessors != null && availableProcessors > 0) {
                log.info("Sistema operativo del broker: {} con {} procesadores", osName, availableProcessors);
                int activeThreads = getActiveThreadCount(connection);
                double estimatedCpu = Math.min((double) activeThreads / availableProcessors * 10.0, 100.0);
                log.info("CPU estimado basado en threads/procesadores: {}%", estimatedCpu);
                return estimatedCpu;
            }
            
            log.warn("No se pudo obtener ninguna métrica de CPU válida del broker remoto");
            return 0.0;
            
        } catch (Exception e) {
            log.error("Error al obtener métricas de CPU del broker", e);
            return 0.0;
        }
    }
    
    /**
     * Obtiene un atributo de forma segura, retornando null si hay error
     */
    private <T> T getAttributeSafely(MBeanServerConnection connection, ObjectName objectName, 
                                    String attributeName, Class<T> type) {
        try {
            Object value = connection.getAttribute(objectName, attributeName);
            if (value != null && type.isAssignableFrom(value.getClass())) {
                return type.cast(value);
            }
            return null;
        } catch (Exception e) {
            log.debug("{} no disponible: {}", attributeName, e.getMessage());
            return null;
        }
    }
    
    private long getDiskUsage(MBeanServerConnection connection, ObjectName brokerName) {
        try {
            return safeConvertToLong(connection.getAttribute(brokerName, "StorePercentUsage"));
        } catch (Exception e) {
            log.warn("No se pudo obtener el uso de disco", e);
            return 0;
        }
    }
    
    private long getMaxDiskUsage(MBeanServerConnection connection, ObjectName brokerName) {
        try {
            Long result = safeConvertToLong(connection.getAttribute(brokerName, "StoreLimit"));
            return result != null && result > 0 ? result : 100;
        } catch (Exception e) {
            log.warn("No se pudo obtener el límite de disco", e);
            return 100;
        }
    }
    
    /**
     * Convierte un objeto Number (Integer o Long) a Long de manera segura
     */
    private Long safeConvertToLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return 0L;
    }
    
    /**
     * Convierte un objeto Number (Integer o Long) a Integer de manera segura
     */
    private Integer safeConvertToInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        return 0;
    }
}
