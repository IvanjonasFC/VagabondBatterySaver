#!/system/bin/sh

# Script de desinstalación para Lock Screen Battery Saver
# Este script detiene procesos activos relacionados y limpia todos los archivos relacionados. 


MODDIR=${0%/*}  

# Archivo de log para registrar pasos de desinstalación
LOG_FILE="/data/adb/batterysaver_uninstall.log"

# Función para registrar eventos con timestamp en el archivo de log
log_uninstall() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

# Inicio del log con encabezados visuales para facilitar lectura
log_uninstall "════════════════════════════════════════════════"
log_uninstall "🗑️  Desinstalando Lock Screen Battery Saver"
log_uninstall "════════════════════════════════════════════════"

# Control de procesos activos: si hay un PID guardado, se intenta matar el proceso
PID_FILE="/data/adb/service.d/govbattery.pid"
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE" 2>/dev/null)  # Leer PID si existe
    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        kill -9 "$OLD_PID" 2>/dev/null  # Matar proceso forcefully
        log_uninstall "✅ Proceso detenido (PID: $OLD_PID)"
    fi
fi

# Eliminación de todos los archivos relacionados del script en /data/adb/service.d
if [ -d "/data/adb/service.d" ]; then
    rm -f /data/adb/service.d/govbattery.sh
    rm -f /data/adb/service.d/govbattery.conf
    rm -f /data/adb/service.d/govbattery.log
    rm -f /data/adb/service.d/govbattery.pid
    rm -f /data/adb/service.d/govbattery.state
    rm -f /data/adb/service.d/govbattery.reload
    rm -f /data/adb/service.d/govbattery.stats
    rm -f /data/adb/service.d/govbattery_boot.log
    log_uninstall "✅ Archivos del script eliminados"
    
    # Comprobar si la carpeta está vacía para eliminarla sin afectar otros scripts
    if [ -z "$(ls -A /data/adb/service.d 2>/dev/null)" ]; then
        rmdir /data/adb/service.d 2>/dev/null
        log_uninstall "✅ Carpeta service.d eliminada (estaba vacía)"
    else
        log_uninstall "ℹ️  Carpeta service.d conservada (contiene otros archivos)"
    fi
else
    log_uninstall "ℹ️  Carpeta service.d no existe"
fi

# También eliminamos logs específicos de instalación y permisos, sin mostrar errores si no existen
rm -f /data/adb/batterysaver_install.log 2>/dev/null
rm -f /data/adb/batterysaver_perms.log 2>/dev/null

# Desinstalación silenciosa de la app usando Package Manager, si está instalada
pm uninstall com.batterysaver 2>/dev/null
log_uninstall "✅ App desinstalada si existía"

# Finalización del log para indicar proceso completado correctamente
log_uninstall "════════════════════════════════════════════════"
log_uninstall "✅ Desinstalación completada"
log_uninstall "════════════════════════════════════════════════"

exit 0  
