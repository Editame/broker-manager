package com.editame.brokermanager.application.service;

import com.editame.brokermanager.application.port.out.BrokerAdminPort;
import com.editame.brokermanager.domain.exception.QueueNotFoundException;
import com.editame.brokermanager.domain.model.BrokerMetrics;
import com.editame.brokermanager.domain.model.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para BrokerManagementService
 */
@ExtendWith(MockitoExtension.class)
class BrokerManagementServiceTest {
    
    @Mock
    private BrokerAdminPort brokerAdminPort;
    
    @InjectMocks
    private BrokerManagementService brokerManagementService;
    
    private BrokerMetrics sampleMetrics;
    private Queue sampleQueue;
    
    @BeforeEach
    void setUp() {
        sampleMetrics = BrokerMetrics.builder()
            .brokerId("test-broker")
            .brokerName("TestBroker")
            .brokerVersion("5.18.0")
            .cpuUsage(45.0)
            .memoryUsage(2147483648L)
            .maxMemory(4294967296L)
            .totalConnections(10)
            .activeConnections(8)
            .status(BrokerMetrics.BrokerStatus.RUNNING)
            .timestamp(Instant.now())
            .build();
            
        sampleQueue = Queue.builder()
            .name("test.queue")
            .type(Queue.QueueType.QUEUE)
            .status(Queue.QueueStatus.ACTIVE)
            .queueSize(25)
            .enqueueCount(100)
            .dequeueCount(75)
            .consumerCount(2)
            .build();
    }
    
    @Test
    void getBrokerMetrics_ShouldReturnMetrics() {
        // Given
        when(brokerAdminPort.getBrokerMetrics()).thenReturn(sampleMetrics);
        
        // When
        BrokerMetrics result = brokerManagementService.getBrokerMetrics();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBrokerId()).isEqualTo("test-broker");
        assertThat(result.getCpuUsage()).isEqualTo(45.0);
        verify(brokerAdminPort).getBrokerMetrics();
    }
    
    @Test
    void getAllQueues_ShouldReturnQueueList() {
        // Given
        List<Queue> queues = List.of(sampleQueue);
        when(brokerAdminPort.listQueues()).thenReturn(queues);
        
        // When
        List<Queue> result = brokerManagementService.getAllQueues();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test.queue");
        verify(brokerAdminPort).listQueues();
    }
    
    @Test
    void getQueue_WhenExists_ShouldReturnQueue() {
        // Given
        when(brokerAdminPort.getQueueInfo("test.queue")).thenReturn(Optional.of(sampleQueue));
        
        // When
        Optional<Queue> result = brokerManagementService.getQueue("test.queue");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test.queue");
        verify(brokerAdminPort).getQueueInfo("test.queue");
    }
    
    @Test
    void getQueue_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(brokerAdminPort.getQueueInfo("nonexistent.queue")).thenReturn(Optional.empty());
        
        // When
        Optional<Queue> result = brokerManagementService.getQueue("nonexistent.queue");
        
        // Then
        assertThat(result).isEmpty();
        verify(brokerAdminPort).getQueueInfo("nonexistent.queue");
    }
    
    @Test
    void purgeQueue_WhenQueueNotExists_ShouldThrowException() {
        // Given
        when(brokerAdminPort.queueExists("nonexistent.queue")).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> brokerManagementService.purgeQueue("nonexistent.queue"))
            .isInstanceOf(QueueNotFoundException.class)
            .hasMessageContaining("La cola no existe: nonexistent.queue");
        
        verify(brokerAdminPort).queueExists("nonexistent.queue");
        verify(brokerAdminPort, never()).purgeQueue(anyString());
    }
    
    @Test
    void sendMessage_WithValidCommand_ShouldSucceed() {
        // Given
        var command = BrokerManagementService.SendMessageCommand.builder()
            .body("Test message")
            .priority(4)
            .persistent(true)
            .build();
        
        // When
        assertThatCode(() -> brokerManagementService.sendMessage("test.queue", command))
            .doesNotThrowAnyException();
        
        // Then
        verify(brokerAdminPort).sendMessage(eq("test.queue"), any());
    }
    
    @Test
    void sendMessage_WithInvalidBody_ShouldThrowException() {
        // Given
        var command = BrokerManagementService.SendMessageCommand.builder()
            .body("")
            .priority(4)
            .persistent(true)
            .build();
        
        // When & Then
        assertThatThrownBy(() -> brokerManagementService.sendMessage("test.queue", command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El cuerpo del mensaje no puede estar vacío");
    }
}
