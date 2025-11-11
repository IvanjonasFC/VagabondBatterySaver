#!/system/bin/sh

#####################################################################
# Lock Screen Battery Saver - Script de Inicio Automático
# Ejecutado por Magisk al arrancar el sistema
#
# El script principal debe estar ya copiado por post-fs-data.sh
# Guarda el PID para verificación desde la app
#####################################################################

MODDIR=${0%/*}
PID_FILE="/data/adb/service.d/govbattery.pid"

# Esperar a que el sistema esté completamente inicializado
sleep 30

#####################################################################
# VERIFICAR QUE EL SCRIPT EXISTE
#####################################################################

if [ ! -f /data/adb/service.d/govbattery.sh ]; then
    # Si no existe, intentar copiarlo ahora (fallback de emergencia)
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  govbattery.sh no encontrado, intentando copiar..." >> /data/adb/service.d/govbattery_boot.log
    
    if [ -f "$MODDIR/service.d/govbattery.sh" ]; then
        mkdir -p /data/adb/service.d 2>/dev/null
        cp -f "$MODDIR/service.d/govbattery.sh" /data/adb/service.d/
        chmod 755 /data/adb/service.d/govbattery.sh
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ Script copiado exitosamente (fallback)" >> /data/adb/service.d/govbattery_boot.log
    else
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ ERROR CRÍTICO: govbattery.sh no encontrado en el módulo" >> /data/adb/service.d/govbattery_boot.log
        exit 1
    fi
fi

#####################################################################
# ASEGURAR PERMISOS Y EJECUTAR
#####################################################################

# Asegurarse de que el script tiene permisos correctos
chmod 755 /data/adb/service.d/govbattery.sh 2>/dev/null

# Verificar si ya está corriendo (evitar duplicados)
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE" 2>/dev/null)
    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  Script ya está corriendo (PID: $OLD_PID), no se inicia duplicado" >> /data/adb/service.d/govbattery_boot.log
        exit 0
    else
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  PID antiguo ($OLD_PID) no válido, reiniciando script..." >> /data/adb/service.d/govbattery_boot.log
        rm -f "$PID_FILE"
    fi
fi

# Iniciar el script de monitorización en background
sh /data/adb/service.d/govbattery.sh > /dev/null 2>&1 &

# Capturar el PID del proceso iniciado
SCRIPT_PID=$!

# Guardar el PID en archivo
echo "$SCRIPT_PID" > "$PID_FILE"
chmod 644 "$PID_FILE"

# Esperar un momento para verificar que se inició correctamente
sleep 2

# Verificar que el proceso sigue vivo
if kill -0 "$SCRIPT_PID" 2>/dev/null; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ Módulo iniciado - Script lanzado en background (PID: $SCRIPT_PID)" >> /data/adb/service.d/govbattery_boot.log
else
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ ERROR: Script no se inició correctamente (PID: $SCRIPT_PID no existe)" >> /data/adb/service.d/govbattery_boot.log
    rm -f "$PID_FILE"
    exit 1
fi

exit 0

exit 0
