package com.editame.brokermanager.infrastructure.adapter.in.web;

import com.editame.brokermanager.application.service.DiagnosticService;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.DiagnosticRequest;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.DiagnosticResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para diagnóstico de conectividad
 */
@RestController
@RequestMapping("/api/diagnostic")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Diagnostic", description = "Herramientas de diagnóstico de conectividad")
@CrossOrigin(origins = "*")
public class DiagnosticController {
    
    private final DiagnosticService diagnosticService;
    
    @PostMapping("/connection")
    @Operation(summary = "Diagnosticar conexión", 
               description = "Realiza un diagnóstico completo de conectividad a un broker")
    public ResponseEntity<DiagnosticResponse> diagnoseConnection(
            @Valid @RequestBody DiagnosticRequest request) {
        log.info("Iniciando diagnóstico de conexión para {}:{}", request.host(), request.port());
        
        DiagnosticResponse response = diagnosticService.diagnoseConnection(request);
        
        log.info("Diagnóstico completado para {}:{} - Éxito: {}", 
            request.host(), request.port(), response.overallSuccess());
        
        return ResponseEntity.ok(response);
    }
}
