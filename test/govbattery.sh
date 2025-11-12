#!/system/bin/sh

# Script de monitorización para Lock Screen Battery Saver
# Versión 2.0 con sistema de estadísticas para seguimiento de estados y cambios

# Definición de archivos y rutas claves utilizados por el script
CONFIG_FILE="/data/adb/service.d/govbattery.conf"             # Archivo de configuración
RELOAD_SIGNAL="/data/adb/service.d/govbattery.reload"         # Archivo señal para recargar configuración
LOG_FILE="/data/adb/service.d/govbattery.log"                 # Archivo de log principal
STATE_FILE="/data/adb/service.d/govbattery.state"             # Archivo que guarda estado actual
STATS_FILE="/data/adb/service.d/govbattery.stats"             # Archivo de estadísticas
BOOT_DELAY=20                                                 # Retardo inicial para esperar arranque completo

# Función para cargar configuración desde archivo (con valores por defecto)
load_config() {
    # Valores por defecto para gobernadores, intervalos y CPUs
    GOV_LOCKED="powersave"
    GOV_UNLOCKED="sched_pixel"
    CHECK_INTERVAL=1
    BATTERY_SAVER_ENABLED=1
    CPU_LIST="0 1 2 3 4 5 6 7 8"

    # Si el archivo de configuración existe, carga los valores personalizados
    if [ -f "$CONFIG_FILE" ]; then
        while IFS='=' read -r key value; do
            case "$key" in
                \#*|"") continue ;;                       # Ignorar comentarios y líneas vacías
                GOV_LOCKED) GOV_LOCKED=$(echo "$value" | tr -d '"' | tr -d ' ') ;;
                GOV_UNLOCKED) GOV_UNLOCKED=$(echo "$value" | tr -d '"' | tr -d ' ') ;;
                CHECK_INTERVAL) CHECK_INTERVAL=$(echo "$value" | tr -d ' ') ;;
                BATTERY_SAVER_ENABLED) BATTERY_SAVER_ENABLED=$(echo "$value" | tr -d ' ') ;;
                CPU_LIST) CPU_LIST=$(echo "$value" | tr -d '"') ;;
            esac
        done < "$CONFIG_FILE"
    fi
}

# Función para escribir mensajes en el log principal y truncar si excede 3000 líneas
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE" 2>/dev/null
    local line_count=$(wc -l < "$LOG_FILE" 2>/dev/null || echo 0)
    if [ "$line_count" -gt 3000 ]; then
        tail -n 2000 "$LOG_FILE" > "${LOG_FILE}.tmp"
        mv -f "${LOG_FILE}.tmp" "$LOG_FILE"
    fi
}

# Función para registrar cambios de estado y governor en archivo de estadísticas
log_state_change() {
    local state=$1
    local governor=$2
    local timestamp=$(date '+%s')
    
    # Registro con formato timestamp|estado|governor
    echo "$timestamp|$state|$governor" >> "$STATS_FILE"
    
    # Mantener solo últimas 2000 líneas (~datos últimos 7 días)
    local line_count=$(wc -l < "$STATS_FILE" 2>/dev/null || echo 0)
    if [ "$line_count" -gt 2000 ]; then
        tail -n 2000 "$STATS_FILE" > "${STATS_FILE}.tmp"
        mv "${STATS_FILE}.tmp" "$STATS_FILE"
        chmod 644 "$STATS_FILE"
    fi
}

# Función para aplicar un governor dado a los CPUs listados
set_governor() {
    local governor=$1
    for cpu in $CPU_LIST; do
        if [ -d "/sys/devices/system/cpu/cpu${cpu}/cpufreq" ]; then
            echo "$governor" > "/sys/devices/system/cpu/cpu${cpu}/cpufreq/scaling_governor" 2>/dev/null
        fi
    done
}

# Función para activar o desactivar Battery Saver vía settings global
set_battery_saver() {
    local state=$1
    if [ "$BATTERY_SAVER_ENABLED" = "1" ]; then
        settings put global low_power "$state" 2>/dev/null
    fi
}

# Función para detectar si la pantalla está bloqueada usando dumpsys
is_screen_locked() {
    dumpsys window | grep -q "mDreamingLockscreen=true"
    return $?
}

# Función que revisa si existe señal para recargar configuración y la aplica
check_reload_signal() {
    if [ -f "$RELOAD_SIGNAL" ]; then
        log_message "🔄 ═══ SEÑAL DE RECARGA DETECTADA ═══"
        load_config
        log_message "✅ Configuración recargada desde archivo"
        log_message "   • Governor bloqueado: $GOV_LOCKED"
        log_message "   • Governor desbloqueado: $GOV_UNLOCKED"
        log_message "   • Intervalo: ${CHECK_INTERVAL}s"
        log_message "   • CPUs: $CPU_LIST"
        log_message "   • Battery Saver: $([ "$BATTERY_SAVER_ENABLED" = "1" ] && echo "SÍ" || echo "NO")"
        
        # Aplicar configuración inmediatamente según estado actual guardado
        if [ "$CURRENT_STATE" = "LOCKED" ]; then
            set_governor "$GOV_LOCKED"
            log_message "✅ Governor actualizado a: $GOV_LOCKED"
        else
            set_governor "$GOV_UNLOCKED"
            log_message "✅ Governor actualizado a: $GOV_UNLOCKED"
        fi
        
        # Borrar archivo de señal para evitar recargas repetidas
        rm -f "$RELOAD_SIGNAL"
        log_message "🔄 Configuración aplicada exitosamente"
        log_message "════════════════════════════════════════════════"
    fi
}

# INICIO DEL SCRIPT PRINCIPAL

load_config  # Carga inicial de configuración

# Detección de dispositivos para información en log
DEVICE_MODEL=$(getprop ro.product.model)
DEVICE_SOC=$(getprop ro.hardware)

# Log de inicio con detalles
log_message "════════════════════════════════════════════════"
log_message "🔋 Lock Screen Battery Saver - Iniciado"
log_message "📱 Versión: 1.5 - Con Sistema de Estadísticas"
log_message "════════════════════════════════════════════════"
log_message "📲 Dispositivo: $DEVICE_MODEL ($DEVICE_SOC)"
log_message "════════════════════════════════════════════════"
log_message "⚙️  Configuración inicial:"
log_message "   • Governor bloqueado: $GOV_LOCKED"
log_message "   • Governor desbloqueado: $GOV_UNLOCKED"
log_message "   • CPUs gestionados: $CPU_LIST"
log_message "   • Intervalo de check: ${CHECK_INTERVAL}s"
log_message "   • Battery Saver automático: $([ "$BATTERY_SAVER_ENABLED" = "1" ] && echo "SÍ" || echo "NO")"
log_message "════════════════════════════════════════════════"
log_message "⏳ Esperando ${BOOT_DELAY}s para inicialización del sistema..."

sleep $BOOT_DELAY  # Espera para asegurar estabilidad del sistema

log_message "🔍 Detectando estado inicial del dispositivo..."

# Detectar estado inicial: bloqueado o desbloqueado
if is_screen_locked; then
    CURRENT_STATE="LOCKED"
    log_message "📊 Estado inicial: BLOQUEADO"
    log_message "🔒 ═══ DISPOSITIVO BLOQUEADO ═══"
    set_governor "$GOV_LOCKED"
    set_battery_saver 1
    log_message "✅ Governor: $GOV_LOCKED"
    log_message "✅ Battery Saver: $([ "$BATTERY_SAVER_ENABLED" = "1" ] && echo "ACTIVADO" || echo "Sin cambios")"
    log_state_change "LOCKED" "$GOV_LOCKED"
else
    CURRENT_STATE="UNLOCKED"
    log_message "📊 Estado inicial: DESBLOQUEADO"
    log_message "🔓 ═══ DISPOSITIVO DESBLOQUEADO ═══"
    set_governor "$GOV_UNLOCKED"
    set_battery_saver 0
    log_message "✅ Governor: $GOV_UNLOCKED"
    log_message "✅ Battery Saver: $([ "$BATTERY_SAVER_ENABLED" = "1" ] && echo "DESACTIVADO" || echo "Sin cambios")"
    log_state_change "UNLOCKED" "$GOV_UNLOCKED"
fi

# Guardar estado en archivo para referencia externa o monitoreo
echo "$CURRENT_STATE" > "$STATE_FILE"

log_message "✨ Monitorización activa - Configuración en tiempo real habilitada"
log_message "📊 Sistema de estadísticas activado"
log_message "════════════════════════════════════════════════"

# BUCLE PRINCIPAL: Ejecutar para siempre y reaccionar a cambios de estado
while true; do
    check_reload_signal  # Verificar si hay señal para recargar configuración
    
    if is_screen_locked; then
        if [ "$CURRENT_STATE" != "LOCKED" ]; then
            CURRENT_STATE="LOCKED"
            echo "$CURRENT_STATE" > "$STATE_FILE"
            
            log_message "🔒 ═══ DISPOSITIVO BLOQUEADO ═══"
            set_governor "$GOV_LOCKED"
            set_battery_saver 1
            log_message "✅ Governor: $GOV_LOCKED"
            log_message "✅ Battery Saver: $([ "$BATTERY_SAVER_ENABLED" = "1" ] && echo "ACTIVADO" || echo "Sin cambios")"
            log_state_change "LOCKED" "$GOV_LOCKED"
        fi
    else
        if [ "$CURRENT_STATE" != "UNLOCKED" ]; then
            CURRENT_STATE="UNLOCKED"
            echo "$CURRENT_STATE" > "$STATE_FILE"
            
            log_message "🔓 ═══ DISPOSITIVO DESBLOQUEADO ═══"
            set_governor "$GOV_UNLOCKED"
            set_battery_saver 0
            log_message "✅ Governor: $GOV_UNLOCKED"
            log_message "✅ Battery Saver: $([ "$BATTERY_SAVER_ENABLED" = "1" ] && echo "DESACTIVADO" || echo "Sin cambios")"
            log_state_change "UNLOCKED" "$GOV_UNLOCKED"
        fi
    fi
    
    sleep "$CHECK_INTERVAL"  # Esperar el intervalo configurado antes de volver a comprobar
done
