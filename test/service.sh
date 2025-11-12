#!/system/bin/sh

# Script de inicio automático para Lock Screen Battery Saver
# Ejecutado por Magisk al arrancar el sistema.
# Nota para colaboradores:
# Este script debe estar previamente copiado en /data/adb/service.d/govbattery.sh 


MODDIR=${0%/*}  
# Guardamos el PID para comprobar el servicio activo y mostrarlo
PID_FILE="/data/adb/service.d/govbattery.pid"  
sleep 30

# Verificamos que el Script esta instalado
if [ ! -f /data/adb/service.d/govbattery.sh ]; then
    # Si no lo tenemos lo copiamos 
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  govbattery.sh no encontrado, intentando copiar..." >> /data/adb/service.d/govbattery_boot.log
    
    # Verificacion de que el modulo tiene el script y sino creamos la carpeta y el script
    if [ -f "$MODDIR/service.d/govbattery.sh" ]; then
        mkdir -p /data/adb/service.d 2>/dev/null  
        cp -f "$MODDIR/service.d/govbattery.sh" /data/adb/service.d/  
        #Le damos permisos
        chmod 755 /data/adb/service.d/govbattery.sh  
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ Script copiado exitosamente (fallback)" >> /data/adb/service.d/govbattery_boot.log
    else
        # Mostramos el error sino hay script en el modulo
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ ERROR CRÍTICO: govbattery.sh no encontrado en el módulo" >> /data/adb/service.d/govbattery_boot.log
        exit 1
    fi
fi

# Le damos permisos de ejecucion si se encuentra
chmod 755 /data/adb/service.d/govbattery.sh 2>/dev/null

# Comprobamos si hay un script y no sobrecargar
if [ -f "$PID_FILE" ]; then
    #Miramos el ultimo PID
    OLD_PID=$(cat "$PID_FILE" 2>/dev/null)  
    # Comprobamos que el pid es el actual
    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  Script ya está corriendo (PID: $OLD_PID), no se inicia duplicado" >> /data/adb/service.d/govbattery_boot.log
        exit 0  
    else
        # Si no es valido reiniciamos el script
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  PID antiguo ($OLD_PID) no válido, reiniciando script..." >> /data/adb/service.d/govbattery_boot.log
        rm -f "$PID_FILE"
    fi
fi

# Lanzamos el script 
sh /data/adb/service.d/govbattery.sh > /dev/null 2>&1 &

# GUardamos el PID
SCRIPT_PID=$!

echo "$SCRIPT_PID" > "$PID_FILE"
chmod 644 "$PID_FILE"

sleep 2

# Vemos que el proceso se ejecuta 
if kill -0 "$SCRIPT_PID" 2>/dev/null; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ Módulo iniciado - Script lanzado en background (PID: $SCRIPT_PID)" >> /data/adb/service.d/govbattery_boot.log
else
    # Sino se ejecuta borramos el pid y esperamos el siguiente reinicio
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ ERROR: Script no se inició correctamente (PID: $SCRIPT_PID no existe)" >> /data/adb/service.d/govbattery_boot.log
    rm -f "$PID_FILE"
    exit 1
fi

exit 0  
