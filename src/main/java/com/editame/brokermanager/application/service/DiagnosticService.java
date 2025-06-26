package com.editame.brokermanager.application.service;

import com.editame.brokermanager.infrastructure.adapter.in.web.dto.DiagnosticRequest;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.DiagnosticResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de diagnóstico de conectividad
 */
@Service
@Slf4j
public class DiagnosticService {
    
    public DiagnosticResponse diagnoseConnection(DiagnosticRequest request) {
        log.info("Iniciando diagnóstico para {}:{}", request.host(), request.port());
        
        Instant startTime = Instant.now();
        List<DiagnosticResponse.DiagnosticStep> steps = new ArrayList<>();
        boolean overallSuccess = true;
        String overallMessage = "Diagnóstico completado";
        
        // Paso 1: Resolución DNS
        if (request.testPing()) {
            DiagnosticResponse.DiagnosticStep dnsStep = testDnsResolution(request.host(), request.timeoutSeconds());
            steps.add(dnsStep);
            if (!dnsStep.success()) {
                overallSuccess = false;
                overallMessage = "Fallo en resolución DNS";
            }
        }
        
        // Paso 2: Conectividad de puerto
        if (request.testPort() && overallSuccess) {
            DiagnosticResponse.DiagnosticStep portStep = testPortConnectivity(request.host(), request.port(), request.timeoutSeconds());
            steps.add(portStep);
            if (!portStep.success()) {
                overallSuccess = false;
                overallMessage = "Puerto no accesible";
            }
        }
        
        // Paso 3: Conexión JMX
        if (request.testJmx() && overallSuccess) {
            DiagnosticResponse.DiagnosticStep jmxStep = testJmxConnection(request.host(), request.port(), 
                request.username(), request.password(), request.timeoutSeconds());
            steps.add(jmxStep);
            if (!jmxStep.success()) {
                overallSuccess = false;
                overallMessage = "Conexión JMX fallida";
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime.toEpochMilli();
        
        return DiagnosticResponse.builder()
            .host(request.host())
            .port(request.port())
            .overallSuccess(overallSuccess)
            .overallMessage(overallMessage)
            .steps(steps)
            .timestamp(Instant.now())
            .totalTimeMs(totalTime)
            .build();
    }
    
    private DiagnosticResponse.DiagnosticStep testDnsResolution(String host, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        
        try {
            InetAddress address = InetAddress.getByName(host);
            String resolvedIp = address.getHostAddress();
            long timeMs = System.currentTimeMillis() - startTime;
            
            return DiagnosticResponse.DiagnosticStep.builder()
                .name("DNS Resolution")
                .description("Resolución de nombre de host a IP")
                .success(true)
                .message("Host resuelto correctamente")
                .details(String.format("Host: %s -> IP: %s", host, resolvedIp))
                .timeMs(timeMs)
                .build();
                
        } catch (UnknownHostException e) {
            long timeMs = System.currentTimeMillis() - startTime;
            
            return DiagnosticResponse.DiagnosticStep.builder()
                .name("DNS Resolution")
                .description("Resolución de nombre de host a IP")
                .success(false)
                .message("No se pudo resolver el host")
                .details(String.format("Error: %s", e.getMessage()))
                .timeMs(timeMs)
                .build();
        }
    }
    
    private DiagnosticResponse.DiagnosticStep testPortConnectivity(String host, int port, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutSeconds * 1000);
            long timeMs = System.currentTimeMillis() - startTime;
            
            return DiagnosticResponse.DiagnosticStep.builder()
                .name("Port Connectivity")
                .description("Conectividad TCP al puerto")
                .success(true)
                .message("Puerto accesible")
                .details(String.format("Conexión TCP a %s:%d exitosa", host, port))
                .timeMs(timeMs)
                .build();
                
        } catch (IOException e) {
            long timeMs = System.currentTimeMillis() - startTime;
            
            return DiagnosticResponse.DiagnosticStep.builder()
                .name("Port Connectivity")
                .description("Conectividad TCP al puerto")
                .success(false)
                .message("Puerto no accesible")
                .details(String.format("Error conectando a %s:%d - %s", host, port, e.getMessage()))
                .timeMs(timeMs)
                .build();
        }
    }
    
    private DiagnosticResponse.DiagnosticStep testJmxConnection(String host, int port, String username, String password, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        
        try {
            String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port);
            JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
            
            Map<String, Object> environment = new HashMap<>();
            environment.put("jmx.remote.x.request.waiting.timeout", (long) timeoutSeconds * 1000);
            environment.put("jmx.remote.x.notification.fetch.timeout", (long) timeoutSeconds * 1000);
            
            if (username != null && !username.trim().isEmpty()) {
                environment.put(JMXConnector.CREDENTIALS, new String[]{username, password});
            }
            
            try (JMXConnector connector = JMXConnectorFactory.connect(serviceURL, environment)) {
                // Intentar obtener información básica
                int mbeanCount = connector.getMBeanServerConnection().getMBeanCount();
                long timeMs = System.currentTimeMillis() - startTime;
                
                return DiagnosticResponse.DiagnosticStep.builder()
                    .name("JMX Connection")
                    .description("Conexión JMX al broker ActiveMQ")
                    .success(true)
                    .message("Conexión JMX exitosa")
                    .details(String.format("Conectado a %s, MBeans disponibles: %d", jmxUrl, mbeanCount))
                    .timeMs(timeMs)
                    .build();
            }
            
        } catch (Exception e) {
            long timeMs = System.currentTimeMillis() - startTime;
            
            return DiagnosticResponse.DiagnosticStep.builder()
                .name("JMX Connection")
                .description("Conexión JMX al broker ActiveMQ")
                .success(false)
                .message("Conexión JMX fallida")
                .details(String.format("Error JMX: %s", e.getMessage()))
                .timeMs(timeMs)
                .build();
        }
    }
}
