#!/bin/bash

echo "🔍 DIAGNÓSTICO DE BROKER ACTIVEMQ"
echo "================================="
echo ""

if [ $# -lt 2 ]; then
    echo "Uso: $0 <host> <puerto> [usuario] [contraseña]"
    echo ""
    echo "Ejemplos:"
    echo "  $0 localhost 1099"
    echo "  $0 192.168.1.100 1099"
    echo "  $0 broker.example.com 1099 admin secret"
    exit 1
fi

HOST=$1
PORT=$2
USERNAME=${3:-""}
PASSWORD=${4:-""}

echo "📍 Probando conexión a: $HOST:$PORT"
echo "🔐 Usuario: ${USERNAME:-"(sin autenticación)"}"
echo ""

# Paso 1: Resolución DNS
echo "=== 1. RESOLUCIÓN DNS ==="
if nslookup $HOST > /dev/null 2>&1; then
    IP=$(nslookup $HOST | grep -A1 "Name:" | tail -1 | awk '{print $2}' 2>/dev/null || echo "N/A")
    echo "✅ Host resuelto: $HOST -> $IP"
else
    echo "❌ No se pudo resolver el host: $HOST"
    exit 1
fi
echo ""

# Paso 2: Conectividad de puerto
echo "=== 2. CONECTIVIDAD DE PUERTO ==="
if timeout 5 bash -c "</dev/tcp/$HOST/$PORT" 2>/dev/null; then
    echo "✅ Puerto $PORT accesible en $HOST"
else
    echo "❌ Puerto $PORT NO accesible en $HOST"
    echo "   - Verifica que el broker esté corriendo"
    echo "   - Verifica que JMX esté habilitado"
    echo "   - Verifica firewall/red"
    exit 1
fi
echo ""

# Paso 3: Prueba con la API
echo "=== 3. PRUEBA CON API DE CONEXIONES ==="
JSON_DATA="{
    \"name\": \"Test-$HOST-$(date +%s)\",
    \"host\": \"$HOST\",
    \"port\": $PORT,
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\",
    \"environment\": \"testing\",
    \"description\": \"Prueba de diagnóstico\"
}"

echo "Creando conexión de prueba..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/connections \
    -H "Content-Type: application/json" \
    -d "$JSON_DATA")

if echo "$RESPONSE" | grep -q '"id"'; then
    CONNECTION_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    echo "✅ Conexión creada: $CONNECTION_ID"
    
    # Probar la conexión
    echo "Probando conectividad JMX..."
    TEST_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/connections/$CONNECTION_ID/test")
    
    if echo "$TEST_RESPONSE" | grep -q '"lastTestStatus":"CONNECTED"'; then
        echo "🎉 ¡CONEXIÓN EXITOSA!"
        echo "   El broker está accesible y JMX funciona correctamente"
    else
        echo "❌ Conexión JMX falló"
        echo "   Respuesta: $TEST_RESPONSE"
        echo ""
        echo "💡 POSIBLES CAUSAS:"
        echo "   - JMX no está habilitado en el broker"
        echo "   - Puerto JMX incorrecto"
        echo "   - Credenciales incorrectas"
        echo "   - Configuración de seguridad JMX"
    fi
    
    # Limpiar - eliminar conexión de prueba
    echo ""
    echo "Limpiando conexión de prueba..."
    curl -s -X DELETE "http://localhost:8080/api/connections/$CONNECTION_ID" > /dev/null
    echo "✅ Conexión de prueba eliminada"
    
else
    echo "❌ Error creando conexión de prueba"
    echo "   Respuesta: $RESPONSE"
fi

echo ""
echo "=== DIAGNÓSTICO COMPLETADO ==="
