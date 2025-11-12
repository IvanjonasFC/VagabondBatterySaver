#!/system/bin/sh

# Script Post FS Data para Lock Screen Battery Saver
# Funciones principales:
# 1. Crear la estructura de carpetas necesarias para el script
# 2. Copiar el script principal de monitorización
# 3. Conceder permisos especiales a la app si está instalada


MODDIR=${0%/*}  # Directorio donde se encuentra el script actual

# Esperar 30 segundos para asegurar que el sistema está completamente iniciado
sleep 30

# 1. Preparar estructura y copiar script de monitorización

# Crear carpeta para scripts si no existe, suprimiendo errores si ya existe
mkdir -p /data/adb/service.d 2>/dev/null 

# Copiar el script de monitorización desde el módulo a la ruta esperada de ejecución
if [ -f "$MODDIR/service.d/govbattery.sh" ]; then
    cp -f "$MODDIR/service.d/govbattery.sh" /data/adb/service.d/
    chmod 755 /data/adb/service.d/govbattery.sh  # Ajustar permisos ejecutables
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ✅ Script copiado a /data/adb/service.d/" >> /data/adb/batterysaver_install.log
else
    # Registrar error si el script no se encontró en la ruta esperada
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ✗ ERROR: govbattery.sh no encontrado en el módulo" >> /data/adb/batterysaver_install.log
fi

# 2. Conceder permisos a la app (si está instalada)

# Comprobar si la app está instalada buscando su paquete
if pm list packages | grep -q "com.batterysaver"; then
    # Intentar conceder permisos WRITE_SECURE_SETTINGS para funcionamiento avanzado de la app
    pm grant com.batterysaver android.permission.WRITE_SECURE_SETTINGS 2>/dev/null
    
    # Registrar resultado para facilitar diagnóstico
    if [ $? -eq 0 ]; then
        echo "$(date '+%Y-%m-%d %H:%M:%S'): ✅ Permisos concedidos a com.batterysaver" >> /data/adb/batterysaver_perms.log
    else
        # Advertencia común si la app no está instalada o permisos no se concedieron
        echo "$(date '+%Y-%m-%d %H:%M:%S'): ⚠️  ADVERTENCIA: Fallo al conceder permisos (puede ser normal si la app no está instalada)" >> /data/adb/batterysaver_perms.log
    fi
else
    # Informar si la app no está instalada, el script seguirá funcionando independientemente
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ℹ️  App com.batterysaver no instalada (solo script funcionará)" >> /data/adb/batterysaver_perms.log
fi

exit 0 
