package com.editame.brokermanager.infrastructure.adapter.in.web;

import com.editame.brokermanager.domain.exception.BrokerOperationException;
import com.editame.brokermanager.domain.exception.MessageNotFoundException;
import com.editame.brokermanager.domain.exception.QueueNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(QueueNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQueueNotFound(QueueNotFoundException ex, WebRequest request) {
        log.warn("Cola no encontrada: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Queue Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotFound(MessageNotFoundException ex, WebRequest request) {
        log.warn("Mensaje no encontrado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Message Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(BrokerOperationException.class)
    public ResponseEntity<ErrorResponse> handleBrokerOperation(BrokerOperationException ex, WebRequest request) {
        log.error("Error en operación del broker: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Broker Operation Error")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Invalid Argument")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Error de validación: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse error = ValidationErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Los datos enviados no son válidos")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(errors)
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Ha ocurrido un error interno del servidor")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * DTO para respuestas de error
     */
    public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
    ) {
        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }
        
        public static class ErrorResponseBuilder {
            private Instant timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            
            public ErrorResponseBuilder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public ErrorResponseBuilder status(int status) {
                this.status = status;
                return this;
            }
            
            public ErrorResponseBuilder error(String error) {
                this.error = error;
                return this;
            }
            
            public ErrorResponseBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public ErrorResponseBuilder path(String path) {
                this.path = path;
                return this;
            }
            
            public ErrorResponse build() {
                return new ErrorResponse(timestamp, status, error, message, path);
            }
        }
    }
    
    /**
     * DTO para errores de validación
     */
    public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
    ) {
        public static ValidationErrorResponseBuilder builder() {
            return new ValidationErrorResponseBuilder();
        }
        
        public static class ValidationErrorResponseBuilder {
            private Instant timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            private Map<String, String> validationErrors;
            
            public ValidationErrorResponseBuilder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public ValidationErrorResponseBuilder status(int status) {
                this.status = status;
                return this;
            }
            
            public ValidationErrorResponseBuilder error(String error) {
                this.error = error;
                return this;
            }
            
            public ValidationErrorResponseBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public ValidationErrorResponseBuilder path(String path) {
                this.path = path;
                return this;
            }
            
            public ValidationErrorResponseBuilder validationErrors(Map<String, String> validationErrors) {
                this.validationErrors = validationErrors;
                return this;
            }
            
            public ValidationErrorResponse build() {
                return new ValidationErrorResponse(timestamp, status, error, message, path, validationErrors);
            }
        }
    }
}
