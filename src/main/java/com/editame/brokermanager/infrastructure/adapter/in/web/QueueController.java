package com.editame.brokermanager.infrastructure.adapter.in.web;

import com.editame.brokermanager.application.port.in.BrokerManagementUseCase;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.*;
import com.editame.brokermanager.shared.mapper.QueueMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
               description = "Crea una nueva cola en el broker ActiveMQ")
    public ResponseEntity<QueueOperationResponse> createQueue(
            @Parameter(description = "Nombre de la cola a crear", example = "nueva.cola")
            @RequestParam String queueName) {
        log.info("Creando cola: {}", queueName);
        
        brokerManagementUseCase.createQueue(queueName);
        
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
               description = "Obtiene la lista completa de colas del broker ActiveMQ")
    public ResponseEntity<List<QueueResponse>> getAllQueues(
            @Parameter(description = "Término de búsqueda para filtrar colas") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Número de página (para paginación futura)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (para paginación futura)")
            @RequestParam(defaultValue = "1000") int size) {
        log.info("Solicitando lista de todas las colas - search: {}, page: {}, size: {}", search, page, size);
        
        var queues = brokerManagementUseCase.getAllQueues();
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
               description = "Retorna la información detallada de una cola por su nombre")
    public ResponseEntity<QueueResponse> getQueue(
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Solicitando información de la cola: {}", queueName);
        
        var queue = brokerManagementUseCase.getQueue(queueName);
        
        if (queue.isEmpty()) {
            log.warn("Cola no encontrada: {}", queueName);
            return ResponseEntity.notFound().build();
        }
        
        var response = queueMapper.toResponse(queue.get());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{queueName}/messages")
    @Operation(summary = "Obtener mensajes de una cola",
               description = "Lista los mensajes de una cola específica con paginación")
    public ResponseEntity<List<MessageResponse>> getQueueMessages(
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName,
            @Parameter(description = "Número máximo de mensajes a retornar", example = "50")
            @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Número de mensajes a omitir", example = "0")
            @RequestParam(defaultValue = "0") int offset) {
        log.info("Solicitando mensajes de la cola: {} (limit: {}, offset: {})", queueName, limit, offset);
        
        var messages = brokerManagementUseCase.getQueueMessages(queueName, limit, offset);
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
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName,
            @Valid @RequestBody SendMessageRequest request) {
        log.info("Enviando mensaje a la cola: {}", queueName);
        
        var command = BrokerManagementUseCase.SendMessageCommand.builder()
            .body(request.body())
            .headers(request.headers())
            .properties(request.properties())
            .priority(request.priority())
            .timeToLive(request.timeToLive())
            .persistent(request.persistent())
            .build();
        
        brokerManagementUseCase.sendMessage(queueName, command);
        
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
               description = "Elimina un mensaje específico de la cola por su ID")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName,
            @Parameter(description = "ID del mensaje", example = "ID:localhost-12345-1234567890-1:1")
            @PathVariable String messageId) {
        log.info("Eliminando mensaje {} de la cola: {}", messageId, queueName);
        
        brokerManagementUseCase.deleteMessage(queueName, messageId);
        
        log.info("Mensaje {} eliminado de la cola: {}", messageId, queueName);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{queueName}/messages")
    @Operation(summary = "Purgar todos los mensajes de una cola",
               description = "Elimina todos los mensajes de la cola especificada")
    public ResponseEntity<PurgeResponse> purgeQueue(
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Purgando cola: {}", queueName);
        
        brokerManagementUseCase.purgeQueue(queueName);
        
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
               description = "Pausa el procesamiento de mensajes en la cola especificada")
    public ResponseEntity<QueueOperationResponse> pauseQueue(
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Pausando cola: {}", queueName);
        
        brokerManagementUseCase.pauseQueue(queueName);
        
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
               description = "Reanuda el procesamiento de mensajes en la cola especificada")
    public ResponseEntity<QueueOperationResponse> resumeQueue(
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Reanudando cola: {}", queueName);
        
        brokerManagementUseCase.resumeQueue(queueName);
        
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
               description = "Elimina completamente la cola especificada del broker")
    public ResponseEntity<QueueOperationResponse> deleteQueue(
            @Parameter(description = "Nombre de la cola", example = "test.queue")
            @PathVariable String queueName) {
        log.info("Eliminando cola: {}", queueName);
        
        brokerManagementUseCase.deleteQueue(queueName);
        
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
