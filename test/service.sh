#!/system/bin/sh

# Script de inicio automático para Lock Screen Battery Saver
# Ejecutado por Magisk al arrancar el sistema.
# Nota para colaboradores:
# Este script debe estar previamente copiado en /data/adb/service.d/govbattery.sh 


MODDIR=${0%/*}  # Directorio donde se encuentra este script
PID_FILE="/data/adb/service.d/govbattery.pid"  # Archivo para guardar el PID del proceso en ejecución

# Esperar 30 segundos para asegurar que el sistema esté totalmente inicializado antes de continuar
sleep 30

# Verificación de existencia del script govbattery.sh principal en la ruta de ejecución
if [ ! -f /data/adb/service.d/govbattery.sh ]; then
    # Intento de recuperación: si no está, lo copiamos desde el módulo si existe
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  govbattery.sh no encontrado, intentando copiar..." >> /data/adb/service.d/govbattery_boot.log
    
    # Verificamos que el script existe dentro del módulo
    if [ -f "$MODDIR/service.d/govbattery.sh" ]; then
        mkdir -p /data/adb/service.d 2>/dev/null  # Creamos directorio si no existe, sin mostrar error
        cp -f "$MODDIR/service.d/govbattery.sh" /data/adb/service.d/  # Copiamos script
        chmod 755 /data/adb/service.d/govbattery.sh  # Ajustamos permisos ejecutables
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ Script copiado exitosamente (fallback)" >> /data/adb/service.d/govbattery_boot.log
    else
        # Error crítico: el script no está en el módulo ni en la ruta esperada, salir con error.
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ ERROR CRÍTICO: govbattery.sh no encontrado en el módulo" >> /data/adb/service.d/govbattery_boot.log
        exit 1
    fi
fi

# Aseguramos que el script tiene permisos de ejecución, no causar errores si no se puede cambiar permisos
chmod 755 /data/adb/service.d/govbattery.sh 2>/dev/null

# Evitar ejecuciones duplicadas: verificamos si un proceso previo está corriendo leyendo su PID
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE" 2>/dev/null)  # Leer PID antiguo guardado
    # Comprobamos si el PID corresponde a un proceso activo
    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  Script ya está corriendo (PID: $OLD_PID), no se inicia duplicado" >> /data/adb/service.d/govbattery_boot.log
        exit 0  # Salir con éxito porque el script ya está corriendo
    else
        # PID no válido o proceso terminado: eliminamos el archivo PID para reiniciar
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  PID antiguo ($OLD_PID) no válido, reiniciando script..." >> /data/adb/service.d/govbattery_boot.log
        rm -f "$PID_FILE"
    fi
fi

# Lanzamos el script de monitorización en segundo plano, redirigiendo salida a /dev/null para evitar ruidos
sh /data/adb/service.d/govbattery.sh > /dev/null 2>&1 &

# Guardamos el PID del proceso lanzado para control futuro
SCRIPT_PID=$!

# Guardamos el PID en el archivo para futuras verificaciones, con permisos de lectura para otros usuarios
echo "$SCRIPT_PID" > "$PID_FILE"
chmod 644 "$PID_FILE"

# Pausa breve para permitir que el proceso se estabilice
sleep 2

# Verificamos que el proceso sigue ejecutándose
if kill -0 "$SCRIPT_PID" 2>/dev/null; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ Módulo iniciado - Script lanzado en background (PID: $SCRIPT_PID)" >> /data/adb/service.d/govbattery_boot.log
else
    # Si el proceso murió, eliminamos el PID y registramos error para diagnóstico
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ ERROR: Script no se inició correctamente (PID: $SCRIPT_PID no existe)" >> /data/adb/service.d/govbattery_boot.log
    rm -f "$PID_FILE"
    exit 1
fi

exit 0  