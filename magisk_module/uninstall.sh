#!/system/bin/sh

#####################################################################
# Lock Screen Battery Saver - Script de Desinstalación
# Se ejecuta automáticamente al desinstalar el módulo desde Magisk
#####################################################################

MODDIR=${0%/*}

# Log de desinstalación
LOG_FILE="/data/adb/batterysaver_uninstall.log"

log_uninstall() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

log_uninstall "════════════════════════════════════════════════"
log_uninstall "🗑️  Desinstalando Lock Screen Battery Saver"
log_uninstall "════════════════════════════════════════════════"

# Matar procesos del script si están corriendo
PID_FILE="/data/adb/service.d/govbattery.pid"
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE" 2>/dev/null)
    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        kill -9 "$OLD_PID" 2>/dev/null
        log_uninstall "✅ Proceso detenido (PID: $OLD_PID)"
    fi
fi

# Eliminar TODOS los archivos del script
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
    
    # Eliminar carpeta si está vacía (para no afectar otros scripts)
    if [ -z "$(ls -A /data/adb/service.d 2>/dev/null)" ]; then
        rmdir /data/adb/service.d 2>/dev/null
        log_uninstall "✅ Carpeta service.d eliminada (estaba vacía)"
    else
        log_uninstall "ℹ️  Carpeta service.d conservada (contiene otros archivos)"
    fi
else
    log_uninstall "ℹ️  Carpeta service.d no existe"
fi

# Eliminar logs de instalación
rm -f /data/adb/batterysaver_install.log 2>/dev/null
rm -f /data/adb/batterysaver_perms.log 2>/dev/null

pm uninstall com.batterysaver 2>/dev/null
log_uninstall "✅ App desinstalada si existía"

log_uninstall "════════════════════════════════════════════════"
log_uninstall "✅ Desinstalación completada"
log_uninstall "════════════════════════════════════════════════"

exit 0
