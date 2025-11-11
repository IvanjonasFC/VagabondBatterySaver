#!/system/bin/sh

#####################################################################
# Lock Screen Battery Saver - Post FS Data
# Se ejecuta después de montar /data pero antes de iniciar servicios
#
# Funciones:
# 1. Crear estructura de carpetas necesaria
# 2. Copiar el script de monitorización
# 3. Conceder permisos a la app (si está instalada)
#####################################################################

MODDIR=${0%/*}

# Esperar a que el sistema esté completamente iniciado
sleep 30

#####################################################################
# 1. PREPARAR ESTRUCTURA Y COPIAR SCRIPT
#####################################################################

# Crear carpeta si no existe
mkdir -p /data/adb/service.d 2>/dev/null

# Copiar el script desde el módulo a la ubicación de ejecución
if [ -f "$MODDIR/service.d/govbattery.sh" ]; then
    cp -f "$MODDIR/service.d/govbattery.sh" /data/adb/service.d/
    chmod 755 /data/adb/service.d/govbattery.sh
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ✅ Script copiado a /data/adb/service.d/" >> /data/adb/batterysaver_install.log
else
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ✗ ERROR: govbattery.sh no encontrado en el módulo" >> /data/adb/batterysaver_install.log
fi

#####################################################################
# 2. CONCEDER PERMISOS A LA APP (si está instalada)
#####################################################################

# Verificar si la app está instalada
if pm list packages | grep -q "com.batterysaver"; then
    # Conceder permisos de forma garantizada
    pm grant com.batterysaver android.permission.WRITE_SECURE_SETTINGS 2>/dev/null
    
    # Log para verificación
    if [ $? -eq 0 ]; then
        echo "$(date '+%Y-%m-%d %H:%M:%S'): ✅ Permisos concedidos a com.batterysaver" >> /data/adb/batterysaver_perms.log
    else
        echo "$(date '+%Y-%m-%d %H:%M:%S'): ⚠️  ADVERTENCIA: Fallo al conceder permisos (puede ser normal si la app no está instalada)" >> /data/adb/batterysaver_perms.log
    fi
else
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ℹ️  App com.batterysaver no instalada (solo script funcionará)" >> /data/adb/batterysaver_perms.log
fi

exit 0
