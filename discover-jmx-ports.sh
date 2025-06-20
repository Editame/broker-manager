#!/bin/bash

echo "🔍 DESCUBRIMIENTO DE PUERTOS JMX COMUNES"
echo "========================================"
echo ""

if [ $# -lt 1 ]; then
    echo "Uso: $0 <host>"
    echo "Ejemplo: $0 localhost"
    exit 1
fi

HOST=$1
COMMON_PORTS=(1099 1098 1100 9999 8999 11099 21099)

echo "🎯 Probando puertos JMX comunes en: $HOST"
echo ""

for PORT in "${COMMON_PORTS[@]}"; do
    echo -n "Puerto $PORT: "
    if timeout 3 bash -c "</dev/tcp/$HOST/$PORT" 2>/dev/null; then
        echo "✅ ABIERTO"
        
        # Si el puerto está abierto, probar JMX
        echo "   Probando JMX..."
        RESPONSE=$(timeout 10 curl -s -X POST http://localhost:8080/api/connections/test \
            -H "Content-Type: application/json" \
            -d "{\"host\":\"$HOST\",\"port\":$PORT}" 2>/dev/null)
        
        if echo "$RESPONSE" | grep -q '"success":true'; then
            echo "   🎉 ¡JMX FUNCIONA EN PUERTO $PORT!"
        else
            echo "   ❌ Puerto abierto pero JMX no responde"
        fi
    else
        echo "❌ cerrado"
    fi
done

echo ""
echo "=== PUERTOS ADICIONALES A PROBAR ==="
echo "Si ningún puerto común funcionó, prueba:"
echo "- 61616 (ActiveMQ Broker)"
echo "- 8161 (Web Console)"
echo "- Consulta la documentación del broker"
