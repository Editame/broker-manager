package com.editame.brokermanager.application.service.jmx;

import com.editame.brokermanager.application.service.BrokerAdminService;
import com.editame.brokermanager.domain.dto.MessageInfo;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.BrokerQueuesResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.QueueInfo;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class JmxBrokerAdminService implements BrokerAdminService {

    private final MBeanServerConnection mBeanServerConnection;

    @Override
    public BrokerQueuesResponse getAllQueuesInfo() {
        List<QueueInfo> queueInfos = new ArrayList<>();

        try {
            Set<ObjectName> queueMBeans = mBeanServerConnection.queryNames(
                    new ObjectName("org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=*"),
                    null
            );

            for (ObjectName objectName : queueMBeans) {
                String name = (String) mBeanServerConnection.getAttribute(objectName, "Name");
                long queueSize = (Long) mBeanServerConnection.getAttribute(objectName, "QueueSize");
                long enqueueCount = (Long) mBeanServerConnection.getAttribute(objectName, "EnqueueCount");
                long dequeueCount = (Long) mBeanServerConnection.getAttribute(objectName, "DequeueCount");
                int consumerCount = ((Long) mBeanServerConnection.getAttribute(objectName, "ConsumerCount")).intValue();

                // En esta etapa aún no traemos los grupos
                QueueInfo queueInfo = QueueInfo.builder()
                        .name(name)
                        .queueSize(queueSize)
                        .enqueueCount(enqueueCount)
                        .dequeueCount(dequeueCount)
                        .consumerCount(consumerCount)
                        .build();

                queueInfos.add(queueInfo);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving queues via JMX", e);
        }

        return new BrokerQueuesResponse(queueInfos);
    }

    @Override
    public List<MessageInfo> getQueueMessages(String queueName) {
        List<MessageInfo> messages = new ArrayList<>();
        
        try {
            log.info("Getting messages for queue: {}", queueName);
            
            // Buscar el MBean de la cola específica
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName
            );
            
            Set<ObjectName> queueMBeans = mBeanServerConnection.queryNames(queueObjectName, null);
            
            if (queueMBeans.isEmpty()) {
                log.warn("Queue not found: {}", queueName);
                return messages;
            }
            
            ObjectName objectName = queueMBeans.iterator().next();
            
            // Obtener los mensajes usando el método browse - devuelve CompositeData[]
            CompositeData[] messagesData = (CompositeData[]) mBeanServerConnection.invoke(
                objectName, 
                "browse", 
                new Object[]{}, 
                new String[]{}
            );
            
            if (messagesData != null) {
                for (CompositeData messageData : messagesData) {
                    MessageInfo messageInfo = new MessageInfo();
                    
                    // Log de las claves disponibles para debugging
                    log.debug("Available keys in CompositeData: {}", messageData.getCompositeType().keySet());
                    
                    messageInfo.setId((String) messageData.get("JMSMessageID"));
                    
                    // Convertir timestamp - puede ser Date o Long
                    Object timestampObj = messageData.get("JMSTimestamp");
                    if (timestampObj != null) {
                        long timestampMillis;
                        if (timestampObj instanceof Date) {
                            timestampMillis = ((Date) timestampObj).getTime();
                        } else if (timestampObj instanceof Long) {
                            timestampMillis = (Long) timestampObj;
                        } else {
                            timestampMillis = System.currentTimeMillis();
                        }
                        
                        messageInfo.setTimestamp(
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault())
                                .toString()
                        );
                    } else {
                        messageInfo.setTimestamp(LocalDateTime.now().toString());
                    }
                    
                    // Obtener el cuerpo del mensaje - puede estar en diferentes campos
                    Object text = null;
                    if (messageData.containsKey("Text")) {
                        text = messageData.get("Text");
                    } else if (messageData.containsKey("BodyPreview")) {
                        text = messageData.get("BodyPreview");
                    } else if (messageData.containsKey("Body")) {
                        text = messageData.get("Body");
                    }
                    messageInfo.setBody(text != null ? text.toString() : "");
                    
                    // Obtener headers personalizados - verificar si existe la clave
                    Map<String, Object> headers = new HashMap<>();
                    if (messageData.containsKey("Properties")) {
                        Object propertiesObj = messageData.get("Properties");
                        if (propertiesObj instanceof TabularData) {
                            TabularData properties = (TabularData) propertiesObj;
                            for (Object propObj : properties.values()) {
                                CompositeData prop = (CompositeData) propObj;
                                String key = (String) prop.get("key");
                                Object value = prop.get("value");
                                headers.put(key, value);
                            }
                        }
                    }
                    
                    // Agregar propiedades JMS estándar como headers
                    if (messageData.containsKey("JMSCorrelationID")) {
                        headers.put("JMSCorrelationID", messageData.get("JMSCorrelationID"));
                    }
                    if (messageData.containsKey("JMSReplyTo")) {
                        headers.put("JMSReplyTo", messageData.get("JMSReplyTo"));
                    }
                    if (messageData.containsKey("JMSDestination")) {
                        headers.put("JMSDestination", messageData.get("JMSDestination"));
                    }
                    
                    messageInfo.setHeaders(headers);
                    
                    // Otros campos - verificar si existen
                    if (messageData.containsKey("JMSType")) {
                        messageInfo.setType((String) messageData.get("JMSType"));
                    }
                    if (messageData.containsKey("JMSPriority")) {
                        Object priority = messageData.get("JMSPriority");
                        if (priority instanceof Integer) {
                            messageInfo.setPriority((Integer) priority);
                        } else if (priority instanceof Long) {
                            messageInfo.setPriority(((Long) priority).intValue());
                        }
                    }
                    if (messageData.containsKey("JMSExpiration")) {
                        Object expiration = messageData.get("JMSExpiration");
                        if (expiration instanceof Long) {
                            messageInfo.setExpiration((Long) expiration);
                        } else if (expiration instanceof Date) {
                            messageInfo.setExpiration(((Date) expiration).getTime());
                        }
                    }
                    
                    messages.add(messageInfo);
                }
            }
            
            log.info("Found {} messages in queue: {}", messages.size(), queueName);
            
        } catch (Exception e) {
            log.error("Error retrieving messages for queue: " + queueName, e);
            throw new RuntimeException("Error retrieving messages via JMX for queue: " + queueName, e);
        }
        
        return messages;
    }

    @Override
    public void sendMessage(String queueName, SendMessageRequest messageRequest) {
        try {
            log.info("Sending message to queue: {}", queueName);
            
            // Buscar el MBean de la cola específica
            ObjectName queueObjectName = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=" + queueName
            );
            
            Set<ObjectName> queueMBeans = mBeanServerConnection.queryNames(queueObjectName, null);
            
            if (queueMBeans.isEmpty()) {
                log.warn("Queue not found: {}", queueName);
                throw new RuntimeException("Queue not found: " + queueName);
            }
            
            ObjectName objectName = queueMBeans.iterator().next();
            
            // Preparar los parámetros del mensaje
            String messageBody = messageRequest.body() != null ? messageRequest.body() : "";
            
            // Crear el mapa de headers
            Map<String, Object> headers = messageRequest.headers() != null ?
                messageRequest.headers() : new HashMap<>();
            
            // Agregar propiedades JMS si están especificadas
            if (messageRequest.type() != null && !messageRequest.type().isEmpty()) {
                headers.put("JMSType", messageRequest.type());
            }
            
            // Enviar el mensaje usando el método sendTextMessage del MBean
            Object[] params = {
                headers,           // Map de headers
                messageBody,       // Cuerpo del mensaje
                "",               // Usuario (vacío)
                ""                // Password (vacío)
            };
            
            String[] signature = {
                "java.util.Map",
                "java.lang.String", 
                "java.lang.String",
                "java.lang.String"
            };
            
            String messageId = (String) mBeanServerConnection.invoke(
                objectName,
                "sendTextMessage",
                params,
                signature
            );
            
            log.info("Message sent successfully to queue: {} with ID: {}", queueName, messageId);
            
        } catch (Exception e) {
            log.error("Error sending message to queue: " + queueName, e);
            throw new RuntimeException("Error sending message to queue: " + queueName, e);
        }
    }
}
