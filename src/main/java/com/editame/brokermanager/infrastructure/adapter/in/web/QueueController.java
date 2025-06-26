package com.editame.brokermanager.infrastructure.adapter.in.web;

import com.editame.brokermanager.application.port.in.BrokerManagementUseCase;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.MessageResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.MessageSentResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.PurgeResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.QueueOperationResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.QueueResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.SendMessageRequest;
import com.editame.brokermanager.shared.mapper.QueueMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para operaciones de colas
 */
@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Queues", description = "Operaciones de gestión de colas ActiveMQ")
@CrossOrigin(origins = "*")
public class QueueController {
    
    private final BrokerManagementUseCase brokerManagementUseCase;
    private final QueueMapper queueMapper;
    
    @PostMapping
    @Operation(summary = "Crear una nueva cola",
               description = "Crea una nueva cola en el broker ActiveMQ especificado")
    public ResponseEntity<QueueOperationResponse> createQueue(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola a crear", example = "nueva.cola")
            @RequestParam String queueName) {
        log.info("Creando cola: {} para conexión: {}", queueName, connectionId);
        
        brokerManagementUseCase.createQueue(connectionId, queueName);
        
        var response = QueueOperationResponse.builder()
            .queueName(queueName)
            .operation("CREATE")
            .message("Cola creada exitosamente")
            .timestamp(java.time.Instant.now())
            .build();
        
        log.info("Cola creada exitosamente: {}", queueName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Listar todas las colas", 
               description = "Obtiene la lista completa de colas del broker ActiveMQ especificado")
    public ResponseEntity<List<QueueResponse>> getAllQueues(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Término de búsqueda para filtrar colas") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Número de página (para paginación futura)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (para paginación futura)")
            @RequestParam(defaultValue = "1000") int size) {
        log.info("Solicitando lista de todas las colas para conexión: {} - search: {}, page: {}, size: {}", 
                connectionId, search, page, size);
        
        var queues = brokerManagementUseCase.getAllQueues(connectionId);
        var response = queues.stream()
            .map(queueMapper::toResponse);
        
        // Aplicar filtro de búsqueda si se proporciona
        if (search != null && !search.trim().isEmpty()) {
            response = response.filter(queue -> 
                queue.getName().toLowerCase().contains(search.toLowerCase()));
        }
        
        var finalResponse = response.toList();
        
        log.info("Se encontraron {} colas", finalResponse.size());
        return ResponseEntity.ok(finalResponse);
    }
    
    @GetMapping("/{queueName}")
    @Operation(summary = "Obtener información de una cola específica",
               description = "Retorna la información detallada de una cola por su nombre del broker especificado")
    public ResponseEntity<QueueResponse> getQueue(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Solicitando información de la cola: {} para conexión: {}", queueName, connectionId);
        
        var queue = brokerManagementUseCase.getQueue(connectionId, queueName);
        
        if (queue.isEmpty()) {
            log.warn("Cola no encontrada: {}", queueName);
            return ResponseEntity.notFound().build();
        }
        
        var response = queueMapper.toResponse(queue.get());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{queueName}/messages")
    @Operation(summary = "Obtener mensajes de una cola",
               description = "Lista los mensajes de una cola específica con paginación del broker especificado")
    public ResponseEntity<List<MessageResponse>> getQueueMessages(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName,
            @Parameter(description = "Número máximo de mensajes a retornar", example = "50")
            @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Número de mensajes a omitir", example = "0")
            @RequestParam(defaultValue = "0") int offset) {
        log.info("Solicitando mensajes de la cola: {} para conexión: {} (limit: {}, offset: {})", queueName, connectionId, limit, offset);
        
        var messages = brokerManagementUseCase.getQueueMessages(connectionId, queueName, limit, offset);
        var response = messages.stream()
            .map(queueMapper::toMessageResponse)
            .toList();
        
        log.info("Se encontraron {} mensajes en la cola: {}", response.size(), queueName);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{queueName}/messages")
    @Operation(summary = "Enviar mensaje a una cola",
               description = "Envía un nuevo mensaje a la cola especificada")
    public ResponseEntity<MessageSentResponse> sendMessage(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName,
            @Valid @RequestBody SendMessageRequest request) {
        log.info("Enviando mensaje a la cola: {} para conexión: {}", queueName, connectionId);
        
        var command = BrokerManagementUseCase.SendMessageCommand.builder()
            .body(request.body())
            .headers(request.headers())
            .properties(request.properties())
            .priority(request.priority())
            .timeToLive(request.timeToLive())
            .persistent(request.persistent())
            .build();
        
        brokerManagementUseCase.sendMessage(connectionId, queueName, command);
        
        var response = MessageSentResponse.builder()
            .queueName(queueName)
            .message("Mensaje enviado exitosamente")
            .timestamp(java.time.Instant.now())
            .build();
        
        log.info("Mensaje enviado exitosamente a la cola: {}", queueName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{queueName}/messages/{messageId}")
    @Operation(summary = "Eliminar un mensaje específico",
               description = "Elimina un mensaje específico de la cola por su ID del broker especificado")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName,
            @Parameter(description = "ID del mensaje", example = "ID:localhost-12345-1234567890-1:1")
            @PathVariable String messageId) {
        log.info("Eliminando mensaje {} de la cola: {} para conexión: {}", messageId, queueName, connectionId);
        
        brokerManagementUseCase.deleteMessage(connectionId, queueName, messageId);
        
        log.info("Mensaje {} eliminado de la cola: {}", messageId, queueName);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{queueName}/messages")
    @Operation(summary = "Purgar todos los mensajes de una cola",
               description = "Elimina todos los mensajes de la cola especificada del broker especificado")
    public ResponseEntity<PurgeResponse> purgeQueue(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Purgando cola: {} para conexión: {}", queueName, connectionId);
        
        brokerManagementUseCase.purgeQueue(connectionId, queueName);
        
        var response = PurgeResponse.builder()
            .queueName(queueName)
            .message("Cola purgada exitosamente")
            .timestamp(java.time.Instant.now())
            .build();
        
        log.info("Cola purgada exitosamente: {}", queueName);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{queueName}/pause")
    @Operation(summary = "Pausar una cola",
               description = "Pausa el procesamiento de mensajes en la cola especificada del broker especificado")
    public ResponseEntity<QueueOperationResponse> pauseQueue(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Pausando cola: {} para conexión: {}", queueName, connectionId);
        
        brokerManagementUseCase.pauseQueue(connectionId, queueName);
        
        var response = QueueOperationResponse.builder()
            .queueName(queueName)
            .operation("PAUSE")
            .message("Cola pausada exitosamente")
            .timestamp(java.time.Instant.now())
            .build();
        
        log.info("Cola pausada exitosamente: {}", queueName);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{queueName}/resume")
    @Operation(summary = "Reanudar una cola",
               description = "Reanuda el procesamiento de mensajes en la cola especificada del broker especificado")
    public ResponseEntity<QueueOperationResponse> resumeQueue(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Reanudando cola: {} para conexión: {}", queueName, connectionId);
        
        brokerManagementUseCase.resumeQueue(connectionId, queueName);
        
        var response = QueueOperationResponse.builder()
            .queueName(queueName)
            .operation("RESUME")
            .message("Cola reanudada exitosamente")
            .timestamp(java.time.Instant.now())
            .build();
        
        log.info("Cola reanudada exitosamente: {}", queueName);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{queueName}")
    @Operation(summary = "Eliminar una cola",
               description = "Elimina completamente la cola especificada del broker especificado")
    public ResponseEntity<QueueOperationResponse> deleteQueue(
            @Parameter(description = "ID de la conexión del broker", required = true)
            @RequestParam String connectionId,
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Eliminando cola: {} para conexión: {}", queueName, connectionId);
        
        brokerManagementUseCase.deleteQueue(connectionId, queueName);
        
        var response = QueueOperationResponse.builder()
            .queueName(queueName)
            .operation("DELETE")
            .message("Cola eliminada exitosamente")
            .timestamp(java.time.Instant.now())
            .build();
        
        log.info("Cola eliminada exitosamente: {}", queueName);
        return ResponseEntity.ok(response);
    }
}
