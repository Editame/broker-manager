# 🚀 Broker Manager

**Alternativa profesional y moderna a QueueExplorer para gestión de ActiveMQ**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](Dockerfile)

## 📋 Descripción

Broker Manager es una herramienta de gestión web moderna para Apache ActiveMQ que combina:

- **🎯 Funcionalidad completa** - Todas las características de QueueExplorer
- **💰 Sin licencias** - Solo costo de deployment vs licencias anuales
- **🎨 UX moderna** - Interfaz intuitiva vs consola nativa obsoleta
- **🏗️ Arquitectura limpia** - Enterprise-ready, mantenible y escalable

## ✨ Características

### 🎛️ Gestión de Broker
- **Métricas en tiempo real** - CPU, memoria, conexiones, uptime
- **Monitoreo de salud** - Health checks automáticos
- **Dashboard ejecutivo** - Información clara para management

### 📊 Gestión de Colas
- **Vista compacta** - Optimizada para 200+ colas
- **Operaciones completas** - Crear, pausar, purgar, eliminar
- **Filtrado avanzado** - Búsqueda y ordenamiento
- **Estados visuales** - Indicadores de carga y actividad

### 📨 Gestión de Mensajes
- **Navegación eficiente** - Browse con paginación
- **Envío personalizado** - Headers, propiedades, prioridades
- **Eliminación selectiva** - Por ID o purga completa
- **Formato JSON** - Validación y formateo automático

### 🔧 Características Técnicas
- **API REST completa** - Documentada con OpenAPI/Swagger
- **Arquitectura hexagonal** - Clean Architecture + DDD
- **Caching inteligente** - Optimización de rendimiento
- **Observabilidad** - Métricas Prometheus + Health checks
- **Seguridad** - CORS configurado, preparado para autenticación

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21+
- Docker & Docker Compose
- ActiveMQ con JMX habilitado

### 1. Clonar y Ejecutar
```bash
git clone https://github.com/editame/broker-manager.git
cd broker-manager

# Ejecutar con Docker Compose
docker-compose up -d

# O ejecutar localmente
./gradlew bootRun
```

### 2. Acceder a la Aplicación
- **Frontend**: http://localhost:3000
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **ActiveMQ Console**: http://localhost:8161/admin

### 3. Configuración JMX
```yaml
# application.yml
activemq:
  jmx:
    url: service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
    username: # opcional
    password: # opcional
```

## 🏗️ Arquitectura

### Arquitectura Hexagonal (Clean Architecture)
```
🎯 Domain Layer (Core Business)
├── Models: BrokerMetrics, Queue, Message
├── Exceptions: Específicas del dominio
└── Business Logic: Reglas de negocio puras

🎮 Application Layer (Use Cases)
├── Ports IN: Interfaces de casos de uso
├── Ports OUT: Interfaces de infraestructura  
├── Services: Orquestación de lógica de negocio
└── Use Cases: Casos de uso específicos

🔌 Infrastructure Layer (Adapters)
├── Web Adapters: Controllers REST + DTOs
├── JMX Adapters: Conexión real con ActiveMQ
├── Persistence: Repositorios (opcional)
└── Config: Configuraciones y seguridad

🛠️ Shared Layer (Utilities)
├── Mappers: MapStruct para conversiones
├── Utils: Utilidades compartidas
└── Constants: Constantes de la aplicación
```

### Stack Tecnológico
- **Backend**: Spring Boot 3.5, Java 21, JMX
- **Frontend**: React 18, TypeScript, Tailwind CSS
- **Base de Datos**: PostgreSQL (opcional para métricas históricas)
- **Monitoreo**: Micrometer, Prometheus, Grafana
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Build**: Gradle, Docker, Docker Compose

## 📚 API Endpoints

### Broker Management
```http
GET    /api/broker/metrics     # Métricas del broker
GET    /api/broker/health      # Health check
```

### Queue Management
```http
GET    /api/queues             # Listar todas las colas
GET    /api/queues/{name}      # Información de cola específica
POST   /api/queues/{name}/messages    # Enviar mensaje
GET    /api/queues/{name}/messages    # Listar mensajes
DELETE /api/queues/{name}/messages    # Purgar cola
DELETE /api/queues/{name}/messages/{id} # Eliminar mensaje
PUT    /api/queues/{name}/pause       # Pausar cola
PUT    /api/queues/{name}/resume      # Reanudar cola
DELETE /api/queues/{name}             # Eliminar cola
```

## 🧪 Testing

```bash
# Tests unitarios
./gradlew test

# Tests de integración
./gradlew integrationTest

# Cobertura de código
./gradlew jacocoTestReport
```

## 📦 Deployment

### Docker
```bash
# Build imagen
docker build -t broker-manager:latest .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e ACTIVEMQ_JMX_URL=service:jmx:rmi:///jndi/rmi://activemq:1099/jmxrmi \
  broker-manager:latest
```

### Kubernetes
```yaml
# Ejemplo de deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: broker-manager
spec:
  replicas: 2
  selector:
    matchLabels:
      app: broker-manager
  template:
    metadata:
      labels:
        app: broker-manager
    spec:
      containers:
      - name: broker-manager
        image: broker-manager:latest
        ports:
        - containerPort: 8080
        env:
        - name: ACTIVEMQ_JMX_URL
          value: "service:jmx:rmi:///jndi/rmi://activemq:1099/jmxrmi"
```

## 📊 Comparación con Competencia

| Característica | Consola ActiveMQ | QueueExplorer | **Broker Manager** |
|----------------|-------------------|---------------|-------------------|
| **Costo** | Gratis | $299/año | **Solo deployment** |
| **UX/UI** | Obsoleta | Básica | **Moderna** |
| **Funcionalidad** | Limitada | Completa | **Completa+** |
| **Arquitectura** | Monolítica | Desktop | **Cloud-native** |
| **Customización** | No | Limitada | **Total** |
| **API REST** | No | No | **Sí** |
| **Monitoreo** | Básico | No | **Avanzado** |

## 🤝 Contribución

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 🆘 Soporte

- **Issues**: [GitHub Issues](https://github.com/editame/broker-manager/issues)
- **Documentación**: [Wiki](https://github.com/editame/broker-manager/wiki)
- **Email**: support@editame.com

## 🎯 Roadmap

- [ ] **v1.1**: Autenticación JWT + RBAC
- [ ] **v1.2**: Métricas históricas + Dashboards
- [ ] **v1.3**: Alertas y notificaciones
- [ ] **v1.4**: Soporte para múltiples brokers
- [ ] **v2.0**: Soporte para Apache Kafka

---

**¿Por qué pagar licencias cuando puedes tener una solución mejor?** 🚀
