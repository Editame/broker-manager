# Multi-stage build para optimizar tamaño
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src

# Build de la aplicación
RUN gradle build -x test --no-daemon

# Imagen final optimizada
FROM openjdk:21-jdk-slim

# Metadatos
LABEL maintainer="editame@company.com"
LABEL description="Broker Manager - ActiveMQ Management Tool"
LABEL version="1.0.0"

# Crear usuario no-root para seguridad
RUN groupadd -r brokerapp && useradd -r -g brokerapp brokerapp

# Instalar dependencias del sistema
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Directorio de trabajo
WORKDIR /app

# Copiar JAR desde builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Cambiar ownership
RUN chown -R brokerapp:brokerapp /app

# Cambiar a usuario no-root
USER brokerapp

# Configuración de JVM
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
